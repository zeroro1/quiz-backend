package com.quiz.app.dto;

import lombok.Data;
import java.util.List;

@Data
public class AnswerResponse {
    private Long sessionId;
    private Long userId;
    private int totalQuestions;
    private int correctCount;
    private double accuracy;
    private int totalTime;
    private List<AnswerDetail> details;

    @Data
    public static class AnswerDetail {
        private Integer questionIndex;
        private String type;
        private String content;
        private String correctAnswer;
        private String userAnswer;
        private boolean isCorrect;
        private Integer timeTaken;
    }
}