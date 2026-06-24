package com.quiz.app.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("quiz_record")
public class QuizRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String questionType;  // COMMONSENSE or LOGIC
    private String questionContent;
    private String optionsJson;    // JSON array of options
    private String correctAnswer;  // A, B, C, or D
    private String userAnswer;
    private Boolean isCorrect;
    private Integer timeTakenSeconds;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
