package com.example.doublecross.dto.websocket;

import com.example.doublecross.domain.enums.ParticipantType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class GameEvent {
    private final GameEventType type;
    private final Object payload;
    private final Instant timestamp;

    public static GameEvent of(GameEventType type, Object payload) {
        return GameEvent.builder()
                .type(type)
                .payload(payload)
                .timestamp(Instant.now())
                .build();
    }

    // Payload 클래스들
    @Builder
    public record GameStartedPayload(
            String sessionId,
            String initialSituation,
            TurnInfo firstTurn
    ) {}

    @Builder
    public record AiTurnCompletedPayload(
            int turn,
            String content,
            TurnInfo nextTurn
    ) {}

    @Builder
    public record PlayerTurnCompletedPayload(
            int turn,
            String content,
            boolean keywordUsed,
            TurnInfo nextTurn
    ) {}

    @Builder
    public record GuessResultPayload(
            ParticipantType guesser,
            String guessWord,
            boolean correct,
            int points,
            int guessesRemaining,
            int totalScore
    ) {}

    @Builder
    public record TimerUpdatePayload(
            int remainingSeconds,
            int turn
    ) {}

    @Builder
    public record TimerExpiredPayload(
            int turn,
            ParticipantType author,
            String penalty
    ) {}

    @Builder
    public record GameFinishedPayload(
            String reason,
            boolean resultAvailable
    ) {}

    @Builder
    public record TurnInfo(
            int turn,
            ParticipantType author,
            Integer timeLimit,
            Long startTime
    ) {}

    @Builder
    public record ErrorPayload(
            String code,
            String message
    ) {}
}
