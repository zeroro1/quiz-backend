package com.quiz.app.controller;

import com.quiz.app.dto.*;
import com.quiz.app.entity.WxUser;
import com.quiz.app.mapper.WxUserMapper;
import com.quiz.app.service.AiQuizGeneratorService;
import com.quiz.app.service.QuizService;
import com.quiz.app.service.WxAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     */
    @PostMapping("/wx/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = wxAuthService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新用户信息（头像+昵称）
     */
    @PostMapping("/wx/updateUserInfo")
    public ResponseEntity<?> updateUserInfo(@RequestBody UpdateUserInfoRequest request) {
        WxUser user = wxUserMapper.selectById(request.getUserId());
        if (user == null) {
            return ResponseEntity.badRequest().body("用户不存在");
        }
        wxUserMapper.updateUserInfo(request.getUserId(), request.getNickname(), request.getAvatar());
        return ResponseEntity.ok("更新成功");
    }
    
    /**
     * 开始新答题
     */
    @PostMapping("/quiz/start")
    public ResponseEntity<StartQuizResponse> startQuiz(@RequestBody StartQuizRequest request) {
        StartQuizResponse response = quizService.startQuiz(request.getUserId());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 提交答案
     */
    @PostMapping("/quiz/submit")
    public ResponseEntity<AnswerResponse> submitAnswers(@RequestBody AnswerRequest request) {
        AnswerResponse response = quizService.submitAnswers(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取排行榜
     */
    @GetMapping("/leaderboard/{type}")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard(
            @PathVariable String type) {
        List<LeaderboardEntry> entries = quizService.getLeaderboard(type.toUpperCase());
        return ResponseEntity.ok(entries);
    }
    
    /**
     * 获取我的答题记录
     */
    @GetMapping("/user/records")
    public ResponseEntity<List<AnswerResponse.AnswerDetail>> getUserRecords(
            @RequestParam Long userId) {
        List<AnswerResponse.AnswerDetail> records = quizService.getUserRecords(userId);
        return ResponseEntity.ok(records);
    }
}
