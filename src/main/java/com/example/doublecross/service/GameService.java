package com.example.doublecross.service;

import com.example.doublecross.domain.entity.GameResult;
import com.example.doublecross.domain.entity.GameSession;
import com.example.doublecross.domain.entity.Keyword;
import com.example.doublecross.domain.entity.Participant;
import com.example.doublecross.domain.enums.*;
import com.example.doublecross.domain.repository.GameResultRepository;
import com.example.doublecross.domain.repository.GameSessionRepository;
import com.example.doublecross.domain.repository.ParticipantRepository;
import com.example.doublecross.dto.GenreAnalysis;
import com.example.doublecross.exception.GameException;
import com.example.doublecross.exception.GameNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameSessionRepository gameSessionRepository;
    private final ParticipantRepository participantRepository;
    private final GameResultRepository gameResultRepository;
    private final KeywordService keywordService;
    private final ScoreService scoreService;
    private final StoryService storyService;
    private final GenreJudgeService genreJudgeService;

    private final Random random = new Random();

    /**
     * 새 게임 생성
     */
    @Transactional
    public GameSession createGame(Difficulty difficulty, int maxTurns, int turnTimeLimit) {
        // 1. 게임 세션 생성
        GameSession session = GameSession.builder()
                .status(GameStatus.WAITING)
                .maxTurns(maxTurns)
                .turnTimeLimit(turnTimeLimit)
                .currentTurn(0)
                .build();

        session = gameSessionRepository.save(session);

        // 2. 장르 배정 (모든 장르 중 랜덤으로 2개 선택)
        Genre[] genres = Genre.values();
        int playerIndex = random.nextInt(genres.length);
        int aiIndex;
        do {
            aiIndex = random.nextInt(genres.length);
        } while (aiIndex == playerIndex);

        Genre playerGenre = genres[playerIndex];
        Genre aiGenre = genres[aiIndex];

        // 3. 플레이어 생성 및 제시어 배정 (상대 장르에서)
        Participant player = createParticipant(session, ParticipantType.PLAYER, playerGenre, aiGenre, difficulty);

        // 4. AI 생성 및 제시어 배정 (상대 장르에서)
        Participant ai = createParticipant(session, ParticipantType.AI, aiGenre, playerGenre, difficulty);

        log.info("Game created: {} (Player: {}, AI: {})",
                session.getSessionId(), playerGenre, aiGenre);

        return session;
    }

    /**
     * 참가자 생성
     */
    private Participant createParticipant(GameSession session, ParticipantType type,
                                          Genre secretGenre, Genre opponentGenre, Difficulty difficulty) {
        // 상대 장르에서 제시어 배정
        Keyword keyword = keywordService.findRandomKeyword(opponentGenre, difficulty);

        Participant participant = Participant.builder()
                .session(session)
                .type(type)
                .secretGenre(secretGenre)
                .currentKeyword(keyword)
                .keywordStatus(KeywordStatus.PENDING)
                .guessesRemaining(3)
                .score(0)
                .build();

        participant = participantRepository.save(participant);
        session.addParticipant(participant);

        log.info("Created {} with genre {} and keyword '{}'",
                type, secretGenre, keyword.getWord());

        return participant;
    }

    /**
     * 게임 시작
     */
    @Transactional
    public GameSession startGame(UUID sessionId) {
        GameSession session = getSession(sessionId);

        if (session.getStatus() != GameStatus.WAITING) {
            throw new GameException("게임을 시작할 수 없는 상태입니다: " + session.getStatus());
        }

        // 초기 상황 생성
        storyService.generateInitialSituation(session);

        // 게임 상태 변경
        session.setStatus(GameStatus.IN_PROGRESS);
        session.setCurrentTurn(1);

        log.info("Game started: {}", sessionId);

        return gameSessionRepository.save(session);
    }

    /**
     * 턴 진행
     * @return 게임이 종료되어야 하면 true, 아니면 false
     */
    @Transactional
    public boolean advanceTurn(UUID sessionId) {
        GameSession session = getSession(sessionId);

        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            throw new GameException("진행 중인 게임이 아닙니다.");
        }

        int nextTurn = session.getCurrentTurn() + 1;

        if (nextTurn > session.getMaxTurns()) {
            // 마지막 턴 완료 -> 게임 종료 필요
            log.info("All turns completed for session {}, game should finish", sessionId);
            return true;
        } else {
            session.setCurrentTurn(nextTurn);
            gameSessionRepository.save(session);
            log.info("Advanced to turn {} for session {}", nextTurn, sessionId);
            return false;
        }
    }

    /**
     * 게임 종료 및 결과 계산
     */
    @Transactional
    public GameResult finishGame(UUID sessionId) {
        GameSession session = gameSessionRepository.findByIdWithParticipants(sessionId)
                .orElseThrow(() -> new GameNotFoundException(sessionId));

        if (session.getStatus() == GameStatus.FINISHED) {
            return gameResultRepository.findBySessionSessionId(sessionId)
                    .orElseThrow(() -> new GameException("게임 결과를 찾을 수 없습니다."));
        }

        // 1. 전체 스토리 조합
        String fullStory = storyService.buildFullStory(session);

        // 2. 장르 판정 (AI 호출)
        GenreAnalysis analysis = genreJudgeService.analyzeStory(fullStory);

        // 3. 참가자 조회
        Participant player = getParticipant(sessionId, ParticipantType.PLAYER);
        Participant ai = getParticipant(sessionId, ParticipantType.AI);

        int finalTurn = session.getCurrentTurn();

        // 4. 소화 성공 보너스 계산
        calculateDigestBonus(player, finalTurn);
        calculateDigestBonus(ai, finalTurn);

        // 5. 장르 승리 점수 계산
        calculateGenreScore(player, ai, analysis, finalTurn);

        // 6. 승자 결정
        Winner winner = determineWinner(player.getScore(), ai.getScore());

        // 7. 결과 저장
        GameResult result = GameResult.builder()
                .session(session)
                .playerScore(player.getScore())
                .aiScore(ai.getScore())
                .winner(winner)
                .genreAnalysis(analysis.genreAnalysis())
                .qualityFactor(BigDecimal.valueOf(analysis.qualityFactor()))
                .unnaturalElements(
                        analysis.unnaturalElements() != null ?
                                analysis.unnaturalElements().stream()
                                        .map(ue -> GameResult.UnnaturalElement.builder()
                                                .turn(ue.turn())
                                                .element(ue.element())
                                                .reason(ue.reason())
                                                .build())
                                        .toList() : null
                )
                .build();

        result = gameResultRepository.save(result);

        // 8. 세션 상태 변경
        session.setStatus(GameStatus.FINISHED);
        session.setFinishedAt(OffsetDateTime.now());
        session.setGameResult(result);
        gameSessionRepository.save(session);

        log.info("Game finished: {} (Winner: {}, Player: {}, AI: {})",
                sessionId, winner, player.getScore(), ai.getScore());

        return result;
    }

    /**
     * 소화 성공 보너스 계산
     */
    private void calculateDigestBonus(Participant participant, int turn) {
        if (participant.getKeywordStatus() == KeywordStatus.USED) {
            keywordService.markKeywordDigested(participant);
            scoreService.addDigestSuccessScore(participant, turn,
                    participant.getCurrentKeyword().getWord());
        }
    }

    /**
     * 장르 승리 점수 계산
     */
    private void calculateGenreScore(Participant player, Participant ai,
                                     GenreAnalysis analysis, int turn) {
        double playerGenreScore = analysis.getFinalScore(player.getSecretGenre().name());
        double aiGenreScore = analysis.getFinalScore(ai.getSecretGenre().name());

        if (playerGenreScore > aiGenreScore) {
            // 플레이어 장르 승리
            scoreService.addGenreWinScore(player, turn, player.getSecretGenre().name(), playerGenreScore);

            // 압도적 승리 보너스 (70% 이상)
            if (playerGenreScore >= ScoreService.GENRE_BONUS_THRESHOLD) {
                scoreService.addGenreBonusScore(player, turn, player.getSecretGenre().name(), playerGenreScore);
            }
        } else if (aiGenreScore > playerGenreScore) {
            // AI 장르 승리
            scoreService.addGenreWinScore(ai, turn, ai.getSecretGenre().name(), aiGenreScore);

            if (aiGenreScore >= ScoreService.GENRE_BONUS_THRESHOLD) {
                scoreService.addGenreBonusScore(ai, turn, ai.getSecretGenre().name(), aiGenreScore);
            }
        }
        // 동점이면 장르 점수 없음
    }

    /**
     * 승자 결정
     */
    private Winner determineWinner(int playerScore, int aiScore) {
        if (playerScore > aiScore) {
            return Winner.PLAYER;
        } else if (aiScore > playerScore) {
            return Winner.AI;
        } else {
            return Winner.DRAW;
        }
    }

    /**
     * 게임 취소
     */
    @Transactional
    public void cancelGame(UUID sessionId) {
        GameSession session = getSession(sessionId);

        if (session.getStatus() == GameStatus.FINISHED) {
            throw new GameException("이미 종료된 게임은 취소할 수 없습니다.");
        }

        session.setStatus(GameStatus.CANCELLED);
        session.setFinishedAt(OffsetDateTime.now());
        gameSessionRepository.save(session);

        log.info("Game cancelled: {}", sessionId);
    }

    /**
     * 게임 세션 조회
     */
    public GameSession getSession(UUID sessionId) {
        return gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new GameNotFoundException(sessionId));
    }

    /**
     * 게임 세션 조회 (참가자 포함)
     */
    public GameSession getSessionWithParticipants(UUID sessionId) {
        return gameSessionRepository.findByIdWithParticipants(sessionId)
                .orElseThrow(() -> new GameNotFoundException(sessionId));
    }

    /**
     * 게임 세션 조회 (모든 연관 데이터 포함)
     */
    public GameSession getSessionWithAll(UUID sessionId) {
        return gameSessionRepository.findByIdWithParticipantsAndStoryEntries(sessionId)
                .orElseThrow(() -> new GameNotFoundException(sessionId));
    }

    /**
     * 참가자 조회
     */
    public Participant getParticipant(UUID sessionId, ParticipantType type) {
        return participantRepository.findBySessionIdAndTypeWithKeyword(sessionId, type)
                .orElseThrow(() -> new GameException("참가자를 찾을 수 없습니다."));
    }

    /**
     * 현재 턴의 작성자 확인
     */
    public ParticipantType getCurrentTurnAuthor(GameSession session) {
        // 홀수 턴: AI, 짝수 턴: PLAYER (또는 반대로 설정 가능)
        return session.getCurrentTurn() % 2 == 1 ? ParticipantType.AI : ParticipantType.PLAYER;
    }

    /**
     * 게임 진행 가능 여부 확인
     */
    public boolean isGameInProgress(UUID sessionId) {
        return gameSessionRepository.findById(sessionId)
                .map(s -> s.getStatus() == GameStatus.IN_PROGRESS)
                .orElse(false);
    }

    /**
     * 게임 결과 조회
     */
    public GameResult getGameResult(UUID sessionId) {
        return gameResultRepository.findBySessionSessionId(sessionId)
                .orElseThrow(() -> new GameException("게임 결과를 찾을 수 없습니다."));
    }
}
