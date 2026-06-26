package com.quiz.app.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wx_user")
public class WxUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String openid;
    private String nickname;
    private String avatar;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
