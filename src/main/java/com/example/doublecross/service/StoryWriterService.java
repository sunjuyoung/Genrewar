package com.example.doublecross.service;


import com.example.doublecross.dto.StoryWriteRequest;
import com.example.doublecross.dto.StoryWriteResponse;
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
public class StoryWriterService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    /**
     * AI가 스토리를 작성합니다.
     *
     * @param request 스토리 작성 요청
     * @return 작성된 스토리 응답
     */
    public StoryWriteResponse writeStory(StoryWriteRequest request) {
        ChatClient chatClient = chatClientBuilder.build();

        // 시스템 프롬프트 구성
        String systemPrompt = PromptTemplates.STORY_WRITER_SYSTEM_PROMPT
                .replace("{aiGenre}", request.aiGenre())
                .replace("{aiKeyword}", request.aiKeyword())
                .replace("{keywordStatus}", request.keywordStatus());

        // 지시문 구성
        String instruction;
        if (request.shouldUseKeyword()) {
            instruction = PromptTemplates.INSTRUCTION_USE_KEYWORD
                    .replace("{keyword}", request.aiKeyword());
        } else {
            instruction = PromptTemplates.INSTRUCTION_NO_KEYWORD
                    .replace("{genre}", request.aiGenre());
        }

        // 사용자 프롬프트 구성
        String userPrompt = PromptTemplates.STORY_WRITER_USER_PROMPT
                .replace("{currentTurn}", String.valueOf(request.currentTurn()))
                .replace("{maxTurns}", String.valueOf(request.maxTurns()))
                .replace("{shouldUseKeyword}", request.shouldUseKeyword() ? "예 (이번 턴에 제시어 사용)" : "아니오")
                .replace("{storySoFar}", request.getStoryAsString())
                .replace("{instruction}", instruction);

        log.debug("System Prompt: {}", systemPrompt);
        log.debug("User Prompt: {}", userPrompt);

        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        log.debug("AI Response: {}", response);

        return parseResponse(response);
    }

    /**
     * 제시어 사용을 강제하는 스토리 작성
     */
    public StoryWriteResponse writeStoryWithKeyword(
            String genre,
            String keyword,
            int currentTurn,
            int maxTurns,
            java.util.List<StoryWriteRequest.StoryEntry> storySoFar
    ) {
        StoryWriteRequest request = new StoryWriteRequest(
                genre,
                keyword,
                "PENDING",
                currentTurn,
                maxTurns,
                storySoFar,
                true  // 제시어 사용 강제
        );
        return writeStory(request);
    }

    /**
     * 제시어 없이 스토리만 작성
     */
    public StoryWriteResponse writeStoryWithoutKeyword(
            String genre,
            String keyword,
            int currentTurn,
            int maxTurns,
            java.util.List<StoryWriteRequest.StoryEntry> storySoFar
    ) {
        StoryWriteRequest request = new StoryWriteRequest(
                genre,
                keyword,
                "PENDING",
                currentTurn,
                maxTurns,
                storySoFar,
                false  // 제시어 사용 안 함
        );
        return writeStory(request);
    }

    /**
     * AI 응답을 StoryWriteResponse 객체로 파싱
     */
    private StoryWriteResponse parseResponse(String response) {
        try {
            String json = extractJson(response);
            return objectMapper.readValue(json, StoryWriteResponse.class);
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
