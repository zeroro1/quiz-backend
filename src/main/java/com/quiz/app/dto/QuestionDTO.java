package com.quiz.app.dto;

import lombok.Data;

@Data
public class QuestionDTO {
    private Long id;          // 题目ID（会话级别）
    private String type;      // COMMONSENSE 或 LOGIC
    private String content;   // 题干
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;  // A/B/C/D
}
