package com.quiz.app.controller;

import com.quiz.app.dto.*;
import com.quiz.app.entity.WxUser;
import com.quiz.app.mapper.WxUserMapper;
import com.quiz.app.service.QuizService;
import com.quiz.app.service.WxAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统一 API 控制器
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ApiController {
    
    private final WxAuthService wxAuthService;
    private final WxUserMapper wxUserMapper;
    private final QuizService quizService;
    
    /**
     * 微信登录
     * 前端通过 uni.login 获取 code，后端换取 openid
     * @param request 包含微信登录凭证 code
     * @return 用户信息，包含 userId、nickname、avatar
     */
    @PostMapping("/wx/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = wxAuthService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新用户信息（头像 + 昵称）
     * 前端通过 getUserProfile 获取用户授权信息后调用此接口同步到后端
     * @param request 包含 userId、nickname、avatar
     * @return 操作结果
     */
    @PostMapping("/wx/updateUserInfo")
    public ResponseEntity<?> updateUserInfo(@RequestBody UpdateUserInfoRequest request) {
        WxUser user = wxUserMapper.selectById(request.getUserId());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        wxUserMapper.updateUserInfo(request.getUserId(), request.getNickname(), request.getAvatar());
        return ResponseEntity.ok("Updated");
    }
    
    /**
     * 开始新答题
     * 调用 AI 生成 10 道题目（5 道常识 + 5 道逻辑推理）
     * AI 不可用时自动降级到本地固定题库
     * @param request 包含 userId
     * @return sessionId 和题目列表
     */
    @PostMapping("/quiz/start")
    public ResponseEntity<StartQuizResponse> startQuiz(@RequestBody StartQuizRequest request) {
        StartQuizResponse response = quizService.startQuiz(request.getUserId());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 提交答题答案
     * 校验答案、计算得分、保存记录、更新排行榜
     * @param request 包含 sessionId、userId、答案列表（每题含选项和用时）
     * @return 答题结果，包含正确数、正确率、每题详情
     */
    @PostMapping("/quiz/submit")
    public ResponseEntity<AnswerResponse> submitAnswers(@RequestBody AnswerRequest request) {
        AnswerResponse response = quizService.submitAnswers(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取指定题型排行榜
     * @param type 题型：COMMONSENSE（常识）或 LOGIC（逻辑推理）
     * @return 排行榜列表，按正确率降序、平均用时升序排列
     */
    @GetMapping("/leaderboard/{type}")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard(@PathVariable String type) {
        List<LeaderboardEntry> entries = quizService.getLeaderboard(type.toUpperCase());
        return ResponseEntity.ok(entries);
    }
    
    /**
     * 获取当前用户的答题记录
     * @param userId 用户 ID
     * @return 最近 20 条答题详情
     */
    @GetMapping("/user/records")
    public ResponseEntity<List<AnswerResponse.AnswerDetail>> getUserRecords(@RequestParam Long userId) {
        List<AnswerResponse.AnswerDetail> records = quizService.getUserRecords(userId);
        return ResponseEntity.ok(records);
    }
    
    /**
     * 测试用：手动创建用户（开发阶段使用）
     * 跳过微信登录流程，直接用 openid 创建用户记录
     * @param request 包含 openid、nickname、avatar
     * @return 创建成功的用户信息
     */
    @PostMapping("/admin/createUser")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        WxUser user = new WxUser();
        user.setOpenid(request.getOpenid());
        user.setNickname(request.getNickname() != null ? request.getNickname() : "Test User");
        user.setAvatar(request.getAvatar() != null ? request.getAvatar() : "");
        wxUserMapper.insert(user);
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setOpenid(user.getOpenid());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setNewUser(true);
        return ResponseEntity.ok(response);
    }
}
