package com.quiz.app.service;

import com.quiz.app.dto.QuestionDTO;
import java.util.List;

public interface AiQuizGeneratorService {
    List<QuestionDTO> generateQuestions();
}
