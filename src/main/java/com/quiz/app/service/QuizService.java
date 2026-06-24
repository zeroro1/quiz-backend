package com.quiz.app.service;

import com.quiz.app.dto.*;
import java.util.List;

public interface QuizService {
    StartQuizResponse startQuiz(Long userId);
    AnswerResponse submitAnswers(AnswerRequest request);
    List<LeaderboardEntry> getLeaderboard(String type);
    List<AnswerResponse.AnswerDetail> getUserRecords(Long userId);
}
