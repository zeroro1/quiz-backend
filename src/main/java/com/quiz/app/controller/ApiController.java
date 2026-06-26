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

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ApiController {
    
    private final WxAuthService wxAuthService;
    private final WxUserMapper wxUserMapper;
    private final QuizService quizService;
    
    @PostMapping("/wx/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = wxAuthService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/wx/updateUserInfo")
    public ResponseEntity<?> updateUserInfo(@RequestBody UpdateUserInfoRequest request) {
        WxUser user = wxUserMapper.selectById(request.getUserId());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        wxUserMapper.updateUserInfo(request.getUserId(), request.getNickname(), request.getAvatar());
        return ResponseEntity.ok("Updated");
    }
    
    @PostMapping("/quiz/start")
    public ResponseEntity<StartQuizResponse> startQuiz(@RequestBody StartQuizRequest request) {
        StartQuizResponse response = quizService.startQuiz(request.getUserId());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/quiz/submit")
    public ResponseEntity<AnswerResponse> submitAnswers(@RequestBody AnswerRequest request) {
        AnswerResponse response = quizService.submitAnswers(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/leaderboard/{type}")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard(@PathVariable String type) {
        List<LeaderboardEntry> entries = quizService.getLeaderboard(type.toUpperCase());
        return ResponseEntity.ok(entries);
    }
    
    @GetMapping("/user/records")
    public ResponseEntity<List<AnswerResponse.AnswerDetail>> getUserRecords(@RequestParam Long userId) {
        List<AnswerResponse.AnswerDetail> records = quizService.getUserRecords(userId);
        return ResponseEntity.ok(records);
    }
    
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
