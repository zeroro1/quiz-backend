package com.quiz.app.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String code;  // 微信登录凭证
}