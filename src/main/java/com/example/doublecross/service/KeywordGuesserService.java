package com.example.doublecross.service;


import com.example.doublecross.dto.KeywordGuessRequest;
import com.example.doublecross.dto.KeywordGuessResponse;
import com.example.doublecross.prompt.PromptTemplates;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordGuesserService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    /**
     * 상대의 제시어를 추측합니다.
     *
     * @param request 추측 요청
     * @return 추측 결과
     */
    public KeywordGuessResponse guessKeyword(KeywordGuessRequest request) {
        ChatClient chatClient = chatClientBuilder.build();

        // 사용자 프롬프트 구성
        String userPrompt = PromptTemplates.KEYWORD_GUESSER_USER_PROMPT
                .replace("{myGenre}", request.myGenre())
                .replace("{opponentGenreGuess}",
                        request.opponentGenreGuess() != null ? request.opponentGenreGuess() : "모름")
                .replace("{guessesRemaining}", String.valueOf(request.guessesRemaining()))
                .replace("{currentTurn}", String.valueOf(request.currentTurn()))
                .replace("{maxTurns}", String.valueOf(request.maxTurns()))
                .replace("{fullStory}", request.getStoryAsString())
                .replace("{opponentStory}", request.getOpponentStoryOnly());

        log.debug("User Prompt: {}", userPrompt);

        String response = chatClient.prompt()
                .system(PromptTemplates.KEYWORD_GUESSER_SYSTEM_PROMPT)
                .user(userPrompt)
                .call()
                .content();

        log.debug("AI Response: {}", response);

        return parseResponse(response);
    }

    /**
     * 간편 추측 메서드
     */
    public KeywordGuessResponse guessKeyword(
            String myGenre,
            String opponentGenreGuess,
            int guessesRemaining,
            int currentTurn,
            int maxTurns,
            List<KeywordGuessRequest.StoryEntry> fullStory
    ) {
        KeywordGuessRequest request = new KeywordGuessRequest(
                opponentGenreGuess,
                myGenre,
                guessesRemaining,
                currentTurn,
                maxTurns,
                fullStory
        );
        return guessKeyword(request);
    }

    /**
     * AI 응답을 KeywordGuessResponse 객체로 파싱
     */
    private KeywordGuessResponse parseResponse(String response) {
        try {
            String json = extractJson(response);
            return objectMapper.readValue(json, KeywordGuessResponse.class);
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

