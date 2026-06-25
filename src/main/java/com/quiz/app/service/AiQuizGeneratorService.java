package com.quiz.app.service;

import com.quiz.app.dto.QuestionDTO;
import java.util.List;

public interface AiQuizGeneratorService {
    /**
     * 生成10道题目（5道常识 + 5道逻辑）
     * 优先使用AI生成，失败时降级使用固定题库
     */
    List<QuestionDTO> generateQuestions();
    
    /**
     * 从固定题库随机选取10题（5常识+5逻辑）
     */
    List<QuestionDTO> generateFromFixedPool();
}
