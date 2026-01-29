package com.example.doublecross.controller;

import com.example.doublecross.domain.entity.GameResult;
import com.example.doublecross.domain.entity.GameSession;
import com.example.doublecross.domain.entity.Participant;
import com.example.doublecross.domain.entity.StoryEntry;
import com.example.doublecross.domain.enums.GameStatus;
import com.example.doublecross.domain.enums.ParticipantType;
import com.example.doublecross.domain.enums.ScoreEventType;
import com.example.doublecross.dto.GuessResult;
import com.example.doublecross.dto.StorySubmitResult;
import com.example.doublecross.dto.request.CreateGameRequest;
import com.example.doublecross.dto.request.GuessRequest;
import com.example.doublecross.dto.request.SubmitStoryRequest;
import com.example.doublecross.dto.response.GameResponse;
import com.example.doublecross.dto.response.GameResultResponse;
import com.example.doublecross.dto.response.GuessResponse;
import com.example.doublecross.dto.response.StoryResponse;
import com.example.doublecross.exception.GameException;
import com.example.doublecross.service.GameService;
import com.example.doublecross.service.GuessService;
import com.example.doublecross.service.ScoreService;
import com.example.doublecross.service.StoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameRestController {

    private final GameService gameService;
    private final StoryService storyService;
    private final GuessService guessService;
    private final ScoreService scoreService;

    /**
     * POST /api/games - 새 게임 생성
     */
    @PostMapping
    public ResponseEntity<GameResponse> createGame(@Valid @RequestBody(required = false) CreateGameRequest request) {
        if (request == null) {
            request = new CreateGameRequest(null, null, null);
        }

        log.info("Creating new game with difficulty: {}, maxTurns: {}, turnTimeLimit: {}",
                request.difficulty(), request.maxTurns(), request.turnTimeLimit());

        GameSession session = gameService.createGame(
                request.difficulty(),
                request.maxTurns(),
                request.turnTimeLimit()
        );

        Participant player = gameService.getParticipant(session.getSessionId(), ParticipantType.PLAYER);
        List<StoryEntry> storyEntries = storyService.getStoryEntries(session.getSessionId());

        GameResponse response = GameResponse.from(session, player, storyEntries);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/games/{sessionId} - 게임 정보 조회
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<GameResponse> getGame(@PathVariable UUID sessionId) {
        log.info("Getting game info for session: {}", sessionId);

        GameSession session = gameService.getSession(sessionId);
        Participant player = gameService.getParticipant(sessionId, ParticipantType.PLAYER);
        List<StoryEntry> storyEntries = storyService.getStoryEntries(sessionId);

        GameResponse response = GameResponse.from(session, player, storyEntries);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/games/{sessionId}/start - 게임 시작
     */
    @PostMapping("/{sessionId}/start")
    public ResponseEntity<GameResponse> startGame(@PathVariable UUID sessionId) {
        log.info("Starting game: {}", sessionId);

        GameSession session = gameService.startGame(sessionId);
        Participant player = gameService.getParticipant(sessionId, ParticipantType.PLAYER);
        List<StoryEntry> storyEntries = storyService.getStoryEntries(sessionId);

        GameResponse response = GameResponse.from(session, player, storyEntries);

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/games/{sessionId} - 게임 취소
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> cancelGame(@PathVariable UUID sessionId) {
        log.info("Cancelling game: {}", sessionId);

        gameService.cancelGame(sessionId);

        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/games/{sessionId}/story - 플레이어 스토리 제출 (api테스트용)
     */
    @PostMapping("/{sessionId}/story")
    public ResponseEntity<StoryResponse> submitStory(
            @PathVariable UUID sessionId,
            @Valid @RequestBody SubmitStoryRequest request
    ) {
        log.info("Player submitting story for session: {}", sessionId);

        GameSession session = gameService.getSession(sessionId);

        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            throw new GameException("진행 중인 게임이 아닙니다.");
        }

        // 플레이어 턴인지 확인
        ParticipantType currentAuthor = gameService.getCurrentTurnAuthor(session);
        if (currentAuthor != ParticipantType.PLAYER) {
            throw new GameException("플레이어 턴이 아닙니다.");
        }

        // 스토리 제출
        StorySubmitResult result = storyService.submitPlayerStory(
                session,
                request.content(),
                request.useKeyword(),
                request.timeSpent()
        );

        // 턴 진행
        boolean shouldFinish = gameService.advanceTurn(sessionId);

        // 게임 종료 처리
        if (shouldFinish) {
            gameService.finishGame(sessionId);
        }

        // 다음 턴 정보
        session = gameService.getSession(sessionId);
        int nextTurn = session.getCurrentTurn();
        ParticipantType nextAuthor = gameService.getCurrentTurnAuthor(session);
        Integer timeLimit = session.getStatus() == GameStatus.IN_PROGRESS ? session.getTurnTimeLimit() : null;

        StoryResponse response = StoryResponse.success(
                result.getTurn(),
                result.isKeywordUsed(),
                result.getKeywordStatus(),
                nextTurn,
                nextAuthor,
                timeLimit
        );

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/games/{sessionId}/guess - 제시어 추측
     */
    @PostMapping("/{sessionId}/guess")
    public ResponseEntity<GuessResponse> guess(
            @PathVariable UUID sessionId,
            @Valid @RequestBody GuessRequest request
    ) {
        log.info("Player guessing for session: {}, word: {}", sessionId, request.guessWord());

        GameSession session = gameService.getSession(sessionId);

        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            throw new GameException("진행 중인 게임이 아닙니다.");
        }

        GuessResult result = guessService.processGuess(session, ParticipantType.PLAYER, request.guessWord());

        GuessResponse response = GuessResponse.from(result);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/games/{sessionId}/result - 게임 결과 조회
     */
    @GetMapping("/{sessionId}/result")
    public ResponseEntity<GameResultResponse> getResult(@PathVariable UUID sessionId) {
        log.info("Getting result for session: {}", sessionId);

        GameSession session = gameService.getSession(sessionId);

        if (session.getStatus() != GameStatus.FINISHED) {
            throw new GameException("아직 종료되지 않은 게임입니다.");
        }

        GameResult result = gameService.getGameResult(sessionId);
        Participant player = gameService.getParticipant(sessionId, ParticipantType.PLAYER);
        Participant ai = gameService.getParticipant(sessionId, ParticipantType.AI);

        Map<ScoreEventType, Integer> playerBreakdown = scoreService.getScoreBreakdown(player.getParticipantId());
        Map<ScoreEventType, Integer> aiBreakdown = scoreService.getScoreBreakdown(ai.getParticipantId());

        String fullStory = storyService.buildFullStory(session);

        GameResultResponse response = GameResultResponse.from(result, player, ai, playerBreakdown, aiBreakdown, fullStory);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/games/{sessionId}/ai-turn - AI 턴 실행 (테스트/디버그용)
     */
    @PostMapping("/{sessionId}/ai-turn")
    public ResponseEntity<StoryResponse> executeAiTurn(
            @PathVariable UUID sessionId,
            @RequestParam(defaultValue = "false") boolean useKeyword
    ) {
        log.info("Executing AI turn for session: {}", sessionId);

        GameSession session = gameService.getSession(sessionId);

        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            throw new GameException("진행 중인 게임이 아닙니다.");
        }

        // AI 턴인지 확인
        ParticipantType currentAuthor = gameService.getCurrentTurnAuthor(session);
        if (currentAuthor != ParticipantType.AI) {
            throw new GameException("AI 턴이 아닙니다.");
        }

        // AI 스토리 생성
        StorySubmitResult result = storyService.generateAiStory(session, useKeyword);

        // 턴 진행
        boolean shouldFinish = gameService.advanceTurn(sessionId);

        // 게임 종료 처리
        if (shouldFinish) {
            gameService.finishGame(sessionId);
        }

        // 다음 턴 정보
        session = gameService.getSession(sessionId);
        int nextTurn = session.getCurrentTurn();
        ParticipantType nextAuthor = gameService.getCurrentTurnAuthor(session);
        Integer timeLimit = session.getStatus() == GameStatus.IN_PROGRESS ? session.getTurnTimeLimit() : null;

        StoryResponse response = StoryResponse.success(
                result.getTurn(),
                result.isKeywordUsed(),
                result.getKeywordStatus(),
                nextTurn,
                nextAuthor,
                timeLimit
        );

        return ResponseEntity.ok(response);
    }
}
