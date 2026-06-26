package com.quiz.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quiz.app.entity.QuizRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuizRecordMapper extends BaseMapper<QuizRecord> {
}
