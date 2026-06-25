package com.quiz.app.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.app.dto.QuestionDTO;
import com.quiz.app.service.AiQuizGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgnesAiQuizGeneratorServiceImpl implements AiQuizGeneratorService {
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    @Value("${agnes.ai.api-key}")
    private String apiKey;
    
    @Value("${agnes.ai.api-url}")
    private String apiUrl;
    
    @Value("${agnes.ai.model}")
    private String model;
    
    private static final String PROMPT = """
        请生成10道选择题，分为两类：
        1. 常识题（5道）- 类型标识为 COMMONSENSE
        2. 逻辑推理题（5道）- 类型标识为 LOGIC
        
        要求：
        - 每道题包含题干和4个选项（A/B/C/D）
        - 标注正确答案
        - 难度适中，适合大众
        - 题目之间不要重复
        - 必须严格按照以下JSON格式返回，不要添加任何其他文字：
        
        [
          {
            "type": "COMMONSENSE",
            "content": "题目内容",
            "optionA": "选项A内容",
            "optionB": "选项B内容",
            "optionC": "选项C内容",
            "optionD": "选项D内容",
            "correctAnswer": "A"
          },
          ...
        ]
        
        请直接返回JSON数组，不要包含markdown代码块标记。
        """;
    
    @Override
    public List<QuestionDTO> generateQuestions() {
        try {
            List<QuestionDTO> questions = generateFromAi();
            if (questions != null && questions.size() == 10) {
                return questions;
            }
            log.warn("AI generated {} questions, falling back to fixed pool", 
                     questions != null ? questions.size() : 0);
        } catch (Exception e) {
            log.error("AI generation failed, falling back to fixed pool", e);
        }
        return generateFromFixedPool();
    }
    
    private List<QuestionDTO> generateFromAi() throws IOException {
        RequestBody body = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse("application/json");
            }
            
            @Override
            public void writeTo(okio.BufferedSink sink) throws IOException {
                JSONObject json = new JSONObject();
                json.put("model", model);
                
                JSONArray messages = new JSONArray();
                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", PROMPT);
                messages.put(userMsg);
                
                json.put("messages", messages);
                json.put("temperature", 0.8);
                json.put("max_tokens", 4000);
                
                try {
                    sink.write(json.toString().getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        
        Request request = new Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer " + apiKey)
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("AI API 调用失败: " + response.code());
            }
            
            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);
            String content = jsonResponse.getJSONObject("choices")
                .getJSONObject("0")
                .getJSONObject("message")
                .getString("content");
            
            log.info("AI generated content length: {}", content.length());
            
            // 清理可能的 markdown 标记
            content = content.replaceAll("^```json\\s*", "").replaceAll("\\s*```$", "").trim();
            
            // 解析 JSON
            JSONArray questionsArray = new JSONArray(content);
            List<QuestionDTO> questions = new ArrayList<>();
            
            for (int i = 0; i < questionsArray.length(); i++) {
                JSONObject q = questionsArray.getJSONObject(i);
                QuestionDTO dto = new QuestionDTO();
                dto.setType(q.getString("type"));
                dto.setContent(q.getString("content"));
                dto.setOptionA(q.getString("optionA"));
                dto.setOptionB(q.getString("optionB"));
                dto.setOptionC(q.getString("optionC"));
                dto.setOptionD(q.getString("optionD"));
                dto.setCorrectAnswer(q.getString("correctAnswer"));
                questions.add(dto);
            }
            
            log.info("AI generated {} questions successfully", questions.size());
            return questions;
            
        } catch (Exception e) {
            throw new IOException("AI 出题失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<QuestionDTO> generateFromFixedPool() {
        try {
            // 加载固定题库
            ClassPathResource resource = new ClassPathResource("data/fixed_questions.json");
            InputStream is = resource.getInputStream();
            String jsonStr = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            List<QuestionDTO> allQuestions = objectMapper.readValue(
                jsonStr, new TypeReference<List<QuestionDTO>>() {});
            
            // 按类型分组并随机打乱
            List<QuestionDTO> commonsense = allQuestions.stream()
                .filter(q -> "COMMONSENSE".equals(q.getType()))
                .collect(Collectors.toList());
            List<QuestionDTO> logic = allQuestions.stream()
                .filter(q -> "LOGIC".equals(q.getType()))
                .collect(Collectors.toList());
            
            Collections.shuffle(commonsense);
            Collections.shuffle(logic);
            
            // 各取5道
            List<QuestionDTO> result = new ArrayList<>();
            result.addAll(commonsense.subList(0, Math.min(5, commonsense.size())));
            result.addAll(logic.subList(0, Math.min(5, logic.size())));
            
            // 打乱顺序
            Collections.shuffle(result);
            
            log.info("Fixed pool generated {} questions (COMMONSENSE: {}, LOGIC: {})", 
                     result.size(), 
                     (int)result.stream().filter(q -> "COMMONSENSE".equals(q.getType())).count(),
                     (int)result.stream().filter(q -> "LOGIC".equals(q.getType())).count());
            
            return result;
            
        } catch (Exception e) {
            log.error("Failed to load fixed question pool", e);
            throw new RuntimeException("加载题库失败: " + e.getMessage());
        }
    }
}
