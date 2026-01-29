package com.example.doublecross.controller;

import com.example.doublecross.domain.entity.GameSession;
import com.example.doublecross.domain.enums.GameStatus;
import com.example.doublecross.domain.enums.ParticipantType;
import com.example.doublecross.dto.GuessResult;
import com.example.doublecross.dto.StorySubmitResult;
import com.example.doublecross.dto.websocket.GameEvent;
import com.example.doublecross.dto.websocket.GameEventType;
import com.example.doublecross.dto.websocket.WebSocketMessage;
import com.example.doublecross.exception.GameException;
import com.example.doublecross.service.GameService;
import com.example.doublecross.service.GuessService;
import com.example.doublecross.service.StoryService;
import com.example.doublecross.service.TimerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GameWebSocketController {

    private final GameService gameService;
    private final StoryService storyService;
    private final GuessService guessService;
    private final TimerService timerService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 게임 준비 완료
     * Client -> /app/game/{sessionId}/ready
     */
    @MessageMapping("/game/{sessionId}/ready")
    public void handleReady(@DestinationVariable String sessionId) {
        log.info("Player ready for game: {}", sessionId);

        try {
            UUID uuid = UUID.fromString(sessionId);
            GameSession session = gameService.getSession(uuid);

            if (session.getStatus() != GameStatus.WAITING) {
                sendError(sessionId, "INVALID_STATE", "게임을 시작할 수 없는 상태입니다.");
                return;
            }

            // 게임 시작
            session = gameService.startGame(uuid);

            // 타이머 시작
            timerService.startTurnTimer(uuid, session.getTurnTimeLimit());
            timerService.updatePlayerConnection(uuid, true);

            // 게임 시작 이벤트 전송
            GameEvent.TurnInfo firstTurn = GameEvent.TurnInfo.builder()
                    .turn(1)
                    .author(gameService.getCurrentTurnAuthor(session))
                    .timeLimit(session.getTurnTimeLimit())
                    .startTime(Instant.now().toEpochMilli())
                    .build();

            GameEvent.GameStartedPayload payload = GameEvent.GameStartedPayload.builder()
                    .sessionId(sessionId)
                    .initialSituation(session.getInitialSituation())
                    .firstTurn(firstTurn)
                    .build();

            sendEvent(sessionId, GameEventType.GAME_STARTED, payload);

            // AI 첫 턴이면 AI 스토리 생성
            if (gameService.getCurrentTurnAuthor(session) == ParticipantType.AI) {
                executeAiTurn(uuid, sessionId);
            }

        } catch (Exception e) {
            log.error("Error handling ready for session {}: {}", sessionId, e.getMessage(), e);
            sendError(sessionId, "ERROR", e.getMessage());
        }
    }

    /**
     * 스토리 제출
     * Client -> /app/game/{sessionId}/story
     */
    @MessageMapping("/game/{sessionId}/story")
    public void handleStory(
            @DestinationVariable String sessionId,
            @Payload WebSocketMessage.StoryMessage message
    ) {
        log.info("Player submitting story for game: {}", sessionId);

        try {
            UUID uuid = UUID.fromString(sessionId);
            GameSession session = gameService.getSession(uuid);

            if (session.getStatus() != GameStatus.IN_PROGRESS) {
                sendError(sessionId, "INVALID_STATE", "진행 중인 게임이 아닙니다.");
                return;
            }

            // 플레이어 턴인지 확인
            if (gameService.getCurrentTurnAuthor(session) != ParticipantType.PLAYER) {
                sendError(sessionId, "INVALID_TURN", "플레이어 턴이 아닙니다.");
                return;
            }

            // 타이머 취소
            timerService.cancelTurnTimer(uuid);

            // 스토리 제출
            StorySubmitResult result = storyService.submitPlayerStory(
                    session,
                    message.content(),
                    message.useKeyword(),
                    message.timeSpent()
            );

            // 턴 진행
            boolean shouldFinish = gameService.advanceTurn(uuid);

            // 플레이어 턴 완료 이벤트 전송
            GameEvent.TurnInfo nextTurn = null;
            if (!shouldFinish) {
                session = gameService.getSession(uuid);
                nextTurn = GameEvent.TurnInfo.builder()
                        .turn(session.getCurrentTurn())
                        .author(gameService.getCurrentTurnAuthor(session))
                        .timeLimit(session.getTurnTimeLimit())
                        .startTime(Instant.now().toEpochMilli())
                        .build();
            }

            GameEvent.PlayerTurnCompletedPayload payload = GameEvent.PlayerTurnCompletedPayload.builder()
                    .turn(result.getTurn())
                    .content(message.content())
                    .keywordUsed(result.isKeywordUsed())
                    .nextTurn(nextTurn)
                    .build();

            sendEvent(sessionId, GameEventType.PLAYER_TURN_COMPLETED, payload);

            // 게임이 종료되어야 하면 종료 처리
            if (shouldFinish) {
                gameService.finishGame(uuid);
                sendGameFinished(sessionId, "ALL_TURNS_COMPLETED");
            } else {
                // AI 턴이면 AI 스토리 생성
                if (gameService.getCurrentTurnAuthor(session) == ParticipantType.AI) {
                    executeAiTurn(uuid, sessionId);
                }
            }

        } catch (Exception e) {
            log.error("Error handling story for session {}: {}", sessionId, e.getMessage(), e);
            sendError(sessionId, "ERROR", e.getMessage());
        }
    }

    /**
     * 제시어 추측
     * Client -> /app/game/{sessionId}/guess
     */
    @MessageMapping("/game/{sessionId}/guess")
    public void handleGuess(
            @DestinationVariable String sessionId,
            @Payload WebSocketMessage.GuessMessage message
    ) {
        log.info("Player guessing for game: {}, word: {}", sessionId, message.guessWord());

        try {
            UUID uuid = UUID.fromString(sessionId);
            GameSession session = gameService.getSession(uuid);

            if (session.getStatus() != GameStatus.IN_PROGRESS) {
                sendError(sessionId, "INVALID_STATE", "진행 중인 게임이 아닙니다.");
                return;
            }

            // 추측 처리
            GuessResult result = guessService.processGuess(session, ParticipantType.PLAYER, message.guessWord());

            // 추측 결과 이벤트 전송
            GameEvent.GuessResultPayload payload = GameEvent.GuessResultPayload.builder()
                    .guesser(ParticipantType.PLAYER)
                    .guessWord(message.guessWord())
                    .correct(result.isCorrect())
                    .points(result.getPoints())
                    .guessesRemaining(result.getGuessesRemaining())
                    .totalScore(result.getTotalScore())
                    .build();

            sendEvent(sessionId, GameEventType.GUESS_RESULT, payload);

        } catch (Exception e) {
            log.error("Error handling guess for session {}: {}", sessionId, e.getMessage(), e);
            sendError(sessionId, "ERROR", e.getMessage());
        }
    }

    /**
     * AI 턴 실행
     */
    private void executeAiTurn(UUID sessionId, String sessionIdStr) {
        try {
            GameSession session = gameService.getSession(sessionId);

            // AI 스토리 생성
            timerService.updateAiThinking(sessionId, true);
            StorySubmitResult result = storyService.generateAiStory(session, true);
            timerService.updateAiThinking(sessionId, false);

            // 턴 진행
            boolean shouldFinish = gameService.advanceTurn(sessionId);

            // AI 턴 완료 이벤트 전송
            GameEvent.TurnInfo nextTurn = null;
            if (!shouldFinish) {
                session = gameService.getSession(sessionId);
                timerService.startTurnTimer(sessionId, session.getTurnTimeLimit());

                nextTurn = GameEvent.TurnInfo.builder()
                        .turn(session.getCurrentTurn())
                        .author(gameService.getCurrentTurnAuthor(session))
                        .timeLimit(session.getTurnTimeLimit())
                        .startTime(Instant.now().toEpochMilli())
                        .build();
            }

            GameEvent.AiTurnCompletedPayload payload = GameEvent.AiTurnCompletedPayload.builder()
                    .turn(result.getTurn())
                    .content(result.getContent())
                    .nextTurn(nextTurn)
                    .build();

            sendEvent(sessionIdStr, GameEventType.AI_TURN_COMPLETED, payload);

            // 게임이 종료되어야 하면 종료 처리
            if (shouldFinish) {
                gameService.finishGame(sessionId);
                sendGameFinished(sessionIdStr, "ALL_TURNS_COMPLETED");
            }

        } catch (Exception e) {
            log.error("Error executing AI turn for session {}: {}", sessionId, e.getMessage(), e);
            sendError(sessionIdStr, "AI_ERROR", "AI 스토리 생성 중 오류가 발생했습니다.");
        }
    }

    /**
     * 게임 종료 이벤트 전송
     */
    private void sendGameFinished(String sessionId, String reason) {
        GameEvent.GameFinishedPayload payload = GameEvent.GameFinishedPayload.builder()
                .reason(reason)
                .resultAvailable(true)
                .build();

        sendEvent(sessionId, GameEventType.GAME_FINISHED, payload);
    }

    /**
     * 이벤트 전송
     */
    private void sendEvent(String sessionId, GameEventType type, Object payload) {
        GameEvent event = GameEvent.of(type, payload);
        String destination = "/topic/game/" + sessionId;
        messagingTemplate.convertAndSend(destination, event);
        log.debug("Sent event {} to {}", type, destination);
    }

    /**
     * 에러 전송
     */
    private void sendError(String sessionId, String code, String message) {
        GameEvent.ErrorPayload payload = GameEvent.ErrorPayload.builder()
                .code(code)
                .message(message)
                .build();

        sendEvent(sessionId, GameEventType.ERROR, payload);
    }
}
