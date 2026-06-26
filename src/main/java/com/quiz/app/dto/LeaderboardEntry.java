package com.quiz.app.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LeaderboardEntry {
    private Long userId;
    private String nickname;
    private String avatar;
    private int totalQuestions;
    private int correctCount;
    private double accuracy;
    private double avgTime;
    private int totalTime;
    private int rank;
}
