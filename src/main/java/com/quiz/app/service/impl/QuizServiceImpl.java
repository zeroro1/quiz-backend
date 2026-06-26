package com.quiz.app.service.impl;

import com.quiz.app.dto.*;
import com.quiz.app.entity.Leaderboard;
import com.quiz.app.entity.QuizRecord;
import com.quiz.app.entity.WxUser;
import com.quiz.app.mapper.LeaderboardMapper;
import com.quiz.app.mapper.QuizRecordMapper;
import com.quiz.app.mapper.WxUserMapper;
import com.quiz.app.service.AiQuizGeneratorService;
import com.quiz.app.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {
    
    private final AiQuizGeneratorService aiQuizGenerator;
    private final QuizRecordMapper quizRecordMapper;
    private final LeaderboardMapper leaderboardMapper;
    private final WxUserMapper wxUserMapper;
    
    // 内存存储会话数据（生产环境建议用 Redis）
    private static final Map<Long, List<QuestionDTO>> sessionQuestions = new ConcurrentHashMap<>();
    private static final Map<Long, Long> sessionUserMap = new ConcurrentHashMap<>();
    private static long sessionIdCounter = 0;
    
    @Override
    @Transactional
    public StartQuizResponse startQuiz(Long userId) {
        // 生成新会话ID
        long sessionId = ++sessionIdCounter;
        sessionUserMap.put(sessionId, userId);
        
        // 调用 AI 生成题目
        List<QuestionDTO> questions = aiQuizGenerator.generateQuestions();
        sessionQuestions.put(sessionId, questions);
        
        log.info("Session {} started for user {}, {} questions generated", 
                 sessionId, userId, questions.size());
        
        StartQuizResponse response = new StartQuizResponse();
        response.setSessionId(sessionId);
        response.setQuestions(questions);
        
        return response;
    }
    
    @Override
    @Transactional
    public AnswerResponse submitAnswers(AnswerRequest request) {
        Long sessionId = request.getSessionId();
        Long userId = request.getUserId();
        List<AnswerRequest.AnswerItem> answers = request.getAnswers();
        
        // 获取题目
        List<QuestionDTO> questions = sessionQuestions.get(sessionId);
        if (questions == null) {
            throw new RuntimeException("会话不存在或已过期");
        }
        
        AnswerResponse response = new AnswerResponse();
        response.setSessionId(sessionId);
        response.setUserId(userId);
        response.setTotalQuestions(questions.size());
        
        int correctCount = 0;
        int totalTime = 0;
        List<AnswerResponse.AnswerDetail> details = new ArrayList<>();
        
        // 按题型分组统计
        Map<String, Integer> typeCorrect = new HashMap<>();
        Map<String, Integer> typeTotal = new HashMap<>();
        Map<String, Integer> typeTime = new HashMap<>();
        
        for (AnswerRequest.AnswerItem item : answers) {
            int idx = item.getQuestionIndex();
            QuestionDTO question = questions.get(idx);
            boolean isCorrect = item.getAnswer().equals(question.getCorrectAnswer());
            
            if (isCorrect) correctCount++;
            
            totalTime += item.getTimeTaken();
            
            // 保存答题记录
            QuizRecord record = new QuizRecord();
            record.setUserId(userId);
            record.setQuestionType(question.getType());
            record.setQuestionContent(question.getContent());
            record.setOptionsJson(buildOptionsJson(question));
            record.setCorrectAnswer(question.getCorrectAnswer());
            record.setUserAnswer(item.getAnswer());
            record.setIsCorrect(isCorrect);
            record.setTimeTakenSeconds(item.getTimeTaken());
            quizRecordMapper.insert(record);
            
            // 统计
            String type = question.getType();
            typeCorrect.merge(type, isCorrect ? 1 : 0, Integer::sum);
            typeTotal.merge(type, 1, Integer::sum);
            typeTime.merge(type, item.getTimeTaken(), Integer::sum);
            
            // 构建详情
            AnswerResponse.AnswerDetail detail = new AnswerResponse.AnswerDetail();
            detail.setQuestionIndex(idx);
            detail.setType(type);
            detail.setContent(question.getContent());
            detail.setCorrectAnswer(question.getCorrectAnswer());
            detail.setUserAnswer(item.getAnswer());
            detail.setIsCorrect(isCorrect);
            detail.setTimeTaken(item.getTimeTaken());
            details.add(detail);
        }
        
        // 计算正确率
        double accuracy = (double) correctCount / questions.size() * 100;
        double avgTime = (double) totalTime / answers.size();
        
        response.setCorrectCount(correctCount);
        response.setAccuracy(accuracy);
        response.setTotalTime(totalTime);
        response.setDetails(details);
        
        // 更新排行榜
        updateLeaderboard(userId, typeCorrect, typeTotal, typeTime);
        
        // 清理会话数据
        sessionQuestions.remove(sessionId);
        sessionUserMap.remove(sessionId);
        
        log.info("Session {} submitted: {}/{} correct, {:.1f}s avg", 
                 sessionId, correctCount, questions.size(), avgTime);
        
        return response;
    }
    
    @Override
    public List<LeaderboardEntry> getLeaderboard(String type) {
        List<Map<String, Object>> rows = leaderboardMapper.selectWithUserInfo(type);
        List<LeaderboardEntry> entries = new ArrayList<>();
        
        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            LeaderboardEntry entry = new LeaderboardEntry();
            entry.setUserId(((Number) row.get("userId")).longValue());
            entry.setNickname((String) row.get("nickname"));
            entry.setAvatar((String) row.get("avatar"));
            entry.setTotalQuestions(((Number) row.get("total_questions")).intValue());
            entry.setCorrectCount(((Number) row.get("correct_count")).intValue());
            entry.setAccuracy(((Number) row.get("accuracy")).doubleValue());
            entry.setAvgTime(((Number) row.get("avg_time")).doubleValue());
            entry.setTotalTime(((Number) row.get("total_time")).intValue());
            entry.setRank(i + 1);
            entries.add(entry);
        }
        
        return entries;
    }
    
    @Override
    public List<AnswerResponse.AnswerDetail> getUserRecords(Long userId) {
        List<QuizRecord> records = quizRecordMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<QuizRecord>()
                .eq(QuizRecord::getUserId, userId)
                .orderByDesc(QuizRecord::getCreatedAt)
                .last("LIMIT 20")
        );
        
        List<AnswerResponse.AnswerDetail> details = new ArrayList<>();
        for (QuizRecord r : records) {
            AnswerResponse.AnswerDetail d = new AnswerResponse.AnswerDetail();
            d.setType(r.getQuestionType());
            d.setContent(r.getQuestionContent());
            d.setCorrectAnswer(r.getCorrectAnswer());
            d.setUserAnswer(r.getUserAnswer());
            d.setIsCorrect(r.getIsCorrect());
            d.setTimeTaken(r.getTimeTakenSeconds());
            details.add(d);
        }
        return details;
    }
    
    private void updateLeaderboard(Long userId, Map<String, Integer> typeCorrect,
                                    Map<String, Integer> typeTotal,
                                    Map<String, Integer> typeTime) {
        for (String type : typeTotal.keySet()) {
            int total = typeTotal.get(type);
            int correct = typeCorrect.getOrDefault(type, 0);
            int time = typeTime.getOrDefault(type, 0);
            double accuracy = (double) correct / total * 100;
            double avgTime = (double) time / total;
            
            Leaderboard existing = leaderboardMapper.findByUserIdAndType(userId, type);
            
            if (existing == null) {
                Leaderboard lb = new Leaderboard();
                lb.setUserId(userId);
                lb.setQuestionType(type);
                lb.setTotalQuestions(total);
                lb.setCorrectCount(correct);
                lb.setAccuracy(accuracy);
                lb.setAvgTime(avgTime);
                lb.setTotalTime(time);
                leaderboardMapper.insert(lb);
            } else {
                // 重新计算累计数据
                int newTotal = existing.getTotalQuestions() + total;
                int newCorrect = existing.getCorrectCount() + correct;
                double newAccuracy = (double) newCorrect / newTotal * 100;
                int newTime = existing.getTotalTime() + time;
                double newAvgTime = (double) newTime / newTotal;
                
                leaderboardMapper.updateRecord(userId, type, total, correct, 
                    newAccuracy, newAvgTime, time);
            }
        }
    }
    
    private String buildOptionsJson(QuestionDTO q) {
        try {
            org.json.JSONArray arr = new org.json.JSONArray();
            arr.put(buildOptionObj("A", q.getOptionA()));
            arr.put(buildOptionObj("B", q.getOptionB()));
            arr.put(buildOptionObj("C", q.getOptionC()));
            arr.put(buildOptionObj("D", q.getOptionD()));
            return arr.toString();
        } catch (Exception e) {
            return "[]";
        }
    }
    
    private JSONObject buildOptionObj(String key, String value) {
        JSONObject obj = new JSONObject();
        obj.put("option", key);
        obj.put("content", value);
        return obj;
    }
}
