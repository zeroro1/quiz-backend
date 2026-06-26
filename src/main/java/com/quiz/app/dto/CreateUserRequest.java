package com.quiz.app.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String openid;
    private String nickname;
    private String avatar;
}
