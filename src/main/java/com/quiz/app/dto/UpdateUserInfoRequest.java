package com.quiz.app.dto;

import lombok.Data;

@Data
public class UpdateUserInfoRequest {
    private Long userId;
    private String nickname;
    private String avatar;
}
