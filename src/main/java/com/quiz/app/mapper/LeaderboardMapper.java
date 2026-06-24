package com.quiz.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quiz.app.entity.Leaderboard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface LeaderboardMapper extends BaseMapper<Leaderboard> {
    
    @Select("SELECT * FROM leaderboard WHERE question_type = #{type} ORDER BY accuracy DESC, avg_time ASC")
    List<Leaderboard> selectByTypeOrderByRank(@Param("type") String type);
    
    @Select("SELECT * FROM leaderboard WHERE user_id = #{userId} AND question_type = #{type}")
    Leaderboard findByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);
    
    @Update("""
        UPDATE leaderboard SET
            total_questions = total_questions + #{totalQuestions},
            correct_count = correct_count + #{correctCount},
            accuracy = #{accuracy},
            avg_time = #{avgTime},
            total_time = total_time + #{totalTime}
        WHERE user_id = #{userId} AND question_type = #{type}
        """)
    void updateRecord(@Param("userId") Long userId, @Param("type") String type,
                      @Param("totalQuestions") int totalQuestions, @Param("correctCount") int correctCount,
                      @Param("accuracy") double accuracy, @Param("avgTime") double avgTime,
                      @Param("totalTime") int totalTime);
    
    @Select("""
        SELECT u.id as userId, u.nickname, u.avatar,
               l.total_questions, l.correct_count, l.accuracy, l.avg_time, l.total_time
        FROM leaderboard l
        LEFT JOIN wx_user u ON l.user_id = u.id
        WHERE l.question_type = #{type}
        ORDER BY l.accuracy DESC, l.avg_time ASC
        LIMIT 50
        """)
    List<Map<String, Object>> selectWithUserInfo(@Param("type") String type);
}
