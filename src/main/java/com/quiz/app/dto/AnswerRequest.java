package com.quiz.app.dto;

import lombok.Data;
import java.util.List;

@Data
public class AnswerRequest {
    private Long sessionId;
    private Long userId;
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        private Integer questionIndex;  // 0-9
        private String answer;           // A/B/C/D
        private Integer timeTaken;       // 秒
    }
}