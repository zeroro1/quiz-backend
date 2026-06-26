package com.quiz.app.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("leaderboard")
public class Leaderboard {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String questionType;
    private Integer totalQuestions;
    private Integer correctCount;
    private Double accuracy;
    private Double avgTime;
    private Integer totalTime;
    private Integer rankAccuracy;
    private Integer rankSpeed;
}
