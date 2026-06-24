package com.quiz.app.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String code;  // 微信登录凭证
}

@Data
public class LoginResponse {
    private Long userId;
    private String openid;
    private String nickname;
    private String avatar;
    private boolean isNewUser;
}
