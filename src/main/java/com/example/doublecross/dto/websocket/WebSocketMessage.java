package com.example.doublecross.dto.websocket;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WebSocketMessage<T> {
    private final String action;
    private final T data;

    public static <T> WebSocketMessage<T> of(String action, T data) {
        return WebSocketMessage.<T>builder()
                .action(action)
                .data(data)
                .build();
    }

    // 클라이언트 -> 서버 메시지 타입
    public record ReadyMessage() {}

    public record StoryMessage(
            String content,
            boolean useKeyword,
            Integer timeSpent
    ) {}

    public record GuessMessage(
            String guessWord
    ) {}
}
