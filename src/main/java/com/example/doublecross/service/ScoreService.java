package com.example.doublecross.service;

import com.example.doublecross.domain.entity.Participant;
import com.example.doublecross.domain.entity.ScoreEvent;
import com.example.doublecross.domain.enums.ScoreEventType;
import com.example.doublecross.domain.repository.ScoreEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreEventRepository scoreEventRepository;

    // 점수 상수
    public static final int GUESS_CORRECT_POINTS = 5;
    public static final int GUESS_WRONG_POINTS = -1;
    public static final int DIGEST_SUCCESS_POINTS = 2;
    public static final int GENRE_WIN_POINTS = 10;
    public static final int GENRE_BONUS_POINTS = 5;
    public static final double GENRE_BONUS_THRESHOLD = 70.0;

    /**
     * 점수 추가 및 이벤트 기록
     */
    @Transactional
    public ScoreEvent addScore(Participant participant, ScoreEventType eventType, int points, int turn) {
        return addScore(participant, eventType, points, turn, null);
    }

    /**
     * 점수 추가 및 이벤트 기록 (설명 포함)
     */
    @Transactional
    public ScoreEvent addScore(Participant participant, ScoreEventType eventType, int points, int turn, String description) {
        // 참가자 점수 업데이트
        participant.addScore(points);

        // 점수 이벤트 생성
        ScoreEvent scoreEvent = ScoreEvent.builder()
                .participant(participant)
                .turn(turn)
                .eventType(eventType)
                .points(points)
                .description(description)
                .build();

        ScoreEvent saved = scoreEventRepository.save(scoreEvent);

        log.info("Score event: {} {} points for participant {} (turn {}, total: {})",
                eventType, points >= 0 ? "+" + points : points,
                participant.getParticipantId(), turn, participant.getScore());

        return saved;
    }

    /**
     * 추측 정답 점수 추가
     */
    @Transactional
    public ScoreEvent addGuessCorrectScore(Participant participant, int turn, String guessedWord) {
        participant.incrementCorrectGuesses();
        return addScore(participant, ScoreEventType.GUESS_CORRECT, GUESS_CORRECT_POINTS, turn,
                "제시어 '" + guessedWord + "' 맞춤");
    }

    /**
     * 추측 오답 점수 차감
     */
    @Transactional
    public ScoreEvent addGuessWrongScore(Participant participant, int turn, String guessedWord) {
        participant.incrementWrongGuesses();
        return addScore(participant, ScoreEventType.GUESS_WRONG, GUESS_WRONG_POINTS, turn,
                "제시어 '" + guessedWord + "' 틀림");
    }

    /**
     * 제시어 소화 성공 점수 추가
     */
    @Transactional
    public ScoreEvent addDigestSuccessScore(Participant participant, int turn, String keyword) {
        return addScore(participant, ScoreEventType.DIGEST_SUCCESS, DIGEST_SUCCESS_POINTS, turn,
                "제시어 '" + keyword + "' 소화 성공");
    }

    /**
     * 장르 승리 점수 추가
     */
    @Transactional
    public ScoreEvent addGenreWinScore(Participant participant, int turn, String genre, double percentage) {
        return addScore(participant, ScoreEventType.GENRE_WIN, GENRE_WIN_POINTS, turn,
                String.format("장르 '%s' 승리 (%.1f%%)", genre, percentage));
    }

    /**
     * 장르 압도적 승리 보너스 점수 추가
     */
    @Transactional
    public ScoreEvent addGenreBonusScore(Participant participant, int turn, String genre, double percentage) {
        return addScore(participant, ScoreEventType.GENRE_BONUS, GENRE_BONUS_POINTS, turn,
                String.format("장르 '%s' 압도적 승리 보너스 (%.1f%%)", genre, percentage));
    }

    /**
     * 참가자의 총 점수 조회
     */
    public int getTotalScore(Participant participant) {
        return participant.getScore();
    }

    /**
     * 참가자의 점수 이벤트 목록 조회
     */
    public List<ScoreEvent> getScoreEvents(UUID participantId) {
        return scoreEventRepository.findAllByParticipantIdOrderByTurn(participantId);
    }

    /**
     * 참가자의 점수 breakdown 조회
     */
    public Map<ScoreEventType, Integer> getScoreBreakdown(UUID participantId) {
        List<Object[]> results = scoreEventRepository.getScoreBreakdownByParticipantId(participantId);

        Map<ScoreEventType, Integer> breakdown = new EnumMap<>(ScoreEventType.class);
        for (Object[] result : results) {
            ScoreEventType eventType = (ScoreEventType) result[0];
            Integer points = ((Number) result[1]).intValue();
            breakdown.put(eventType, points);
        }

        return breakdown;
    }

    /**
     * 특정 이벤트 타입의 총 점수 조회
     */
    public int getScoreByEventType(UUID participantId, ScoreEventType eventType) {
        Integer score = scoreEventRepository.sumPointsByParticipantIdAndEventType(participantId, eventType);
        return score != null ? score : 0;
    }
}
