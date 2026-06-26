package com.quiz.app.dto;

import lombok.Data;
import java.util.List;

@Data
public class StartQuizResponse {
    private Long sessionId;
    private List<QuestionDTO> questions;
}