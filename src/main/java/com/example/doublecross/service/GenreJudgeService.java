package com.example.doublecross.service;


import com.example.doublecross.dto.GenreAnalysis;
import com.example.doublecross.prompt.PromptTemplates;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreJudgeService {
    
    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;
    
    /**
     * 스토리를 분석하여 장르와 품질을 판정합니다.
     *
     * @param story 분석할 스토리 텍스트
     * @return 장르 분석 결과
     */
    public GenreAnalysis analyzeStory(String story) {
        ChatClient chatClient = chatClientBuilder.build();
        
        String userPrompt = PromptTemplates.GENRE_JUDGE_USER_PROMPT
            .replace("{story}", story);
        
        log.debug("Analyzing story: {}", story);
        
        String response = chatClient.prompt()
            .system(PromptTemplates.GENRE_JUDGE_SYSTEM_PROMPT)
            .user(userPrompt)
            .call()
            .content();
        
        log.debug("AI Response: {}", response);
        
        return parseResponse(response);
    }
    
    /**
     * AI 응답을 GenreAnalysis 객체로 파싱
     */
    private GenreAnalysis parseResponse(String response) {
        try {
            // JSON 블록 추출 (```json ... ``` 형식 처리)
            String json = extractJson(response);
            return objectMapper.readValue(json, GenreAnalysis.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response: {}", response, e);
            throw new RuntimeException("AI 응답 파싱 실패", e);
        }
    }
    
    /**
     * 응답에서 JSON 부분만 추출
     */
    private String extractJson(String response) {
        // ```json 으로 시작하는 경우 처리
        if (response.contains("```json")) {
            int start = response.indexOf("```json") + 7;
            int end = response.lastIndexOf("```");
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }
        
        // ``` 으로 시작하는 경우 처리
        if (response.contains("```")) {
            int start = response.indexOf("```") + 3;
            int end = response.lastIndexOf("```");
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }
        
        // 그냥 JSON인 경우
        return response.trim();
    }
}
