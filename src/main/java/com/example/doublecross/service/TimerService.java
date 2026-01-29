package com.example.doublecross.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimerService {

    private final StringRedisTemplate redisTemplate;

    private static final String TIMER_KEY_PREFIX = "game:timer:";
    private static final String SESSION_KEY_PREFIX = "game:session:";

    /**
     * 턴 타이머 시작
     */
    public void startTurnTimer(UUID sessionId, int turnTimeLimit) {
        String timerKey = TIMER_KEY_PREFIX + sessionId;
        String startTime = String.valueOf(Instant.now().toEpochMilli());

        redisTemplate.opsForValue().set(timerKey, startTime, Duration.ofSeconds(turnTimeLimit));

        log.info("Turn timer started for session {} ({} seconds)", sessionId, turnTimeLimit);
    }

    /**
     * 턴 타이머 취소
     */
    public void cancelTurnTimer(UUID sessionId) {
        String timerKey = TIMER_KEY_PREFIX + sessionId;
        redisTemplate.delete(timerKey);

        log.info("Turn timer cancelled for session {}", sessionId);
    }

    /**
     * 남은 시간 조회 (초)
     */
    public long getRemainingTime(UUID sessionId) {
        String timerKey = TIMER_KEY_PREFIX + sessionId;
        Long ttl = redisTemplate.getExpire(timerKey, TimeUnit.SECONDS);

        return ttl != null && ttl > 0 ? ttl : 0;
    }

    /**
     * 타이머 만료 여부 확인
     */
    public boolean isTimerExpired(UUID sessionId) {
        String timerKey = TIMER_KEY_PREFIX + sessionId;
        return Boolean.FALSE.equals(redisTemplate.hasKey(timerKey));
    }

    /**
     * 게임 세션 상태 저장 (Redis)
     */
    public void saveSessionState(UUID sessionId, String status, int currentTurn, boolean playerConnected) {
        String sessionKey = SESSION_KEY_PREFIX + sessionId;

        redisTemplate.opsForHash().put(sessionKey, "status", status);
        redisTemplate.opsForHash().put(sessionKey, "currentTurn", String.valueOf(currentTurn));
        redisTemplate.opsForHash().put(sessionKey, "playerConnected", String.valueOf(playerConnected));
        redisTemplate.opsForHash().put(sessionKey, "turnStartTime", String.valueOf(Instant.now().toEpochMilli()));

        // 2시간 후 자동 삭제
        redisTemplate.expire(sessionKey, Duration.ofHours(2));

        log.debug("Session state saved for {}", sessionId);
    }

    /**
     * 게임 세션 상태 조회
     */
    public String getSessionStatus(UUID sessionId) {
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        Object status = redisTemplate.opsForHash().get(sessionKey, "status");
        return status != null ? status.toString() : null;
    }

    /**
     * 플레이어 연결 상태 업데이트
     */
    public void updatePlayerConnection(UUID sessionId, boolean connected) {
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        redisTemplate.opsForHash().put(sessionKey, "playerConnected", String.valueOf(connected));

        log.info("Player connection updated for session {}: {}", sessionId, connected);
    }

    /**
     * AI 처리 중 상태 업데이트
     */
    public void updateAiThinking(UUID sessionId, boolean thinking) {
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        redisTemplate.opsForHash().put(sessionKey, "aiThinking", String.valueOf(thinking));

        log.debug("AI thinking status updated for session {}: {}", sessionId, thinking);
    }

    /**
     * 추측 기회 저장 (빠른 조회용)
     */
    public void saveGuessesRemaining(UUID sessionId, String participantType, int guesses) {
        String key = "game:guesses:" + sessionId + ":" + participantType.toLowerCase();
        redisTemplate.opsForValue().set(key, String.valueOf(guesses), Duration.ofHours(2));
    }

    /**
     * 추측 기회 조회
     */
    public int getGuessesRemaining(UUID sessionId, String participantType) {
        String key = "game:guesses:" + sessionId + ":" + participantType.toLowerCase();
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value) : 3;
    }

    /**
     * 게임 세션 데이터 삭제
     */
    public void clearSessionData(UUID sessionId) {
        String timerKey = TIMER_KEY_PREFIX + sessionId;
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        String playerGuessKey = "game:guesses:" + sessionId + ":player";
        String aiGuessKey = "game:guesses:" + sessionId + ":ai";

        redisTemplate.delete(timerKey);
        redisTemplate.delete(sessionKey);
        redisTemplate.delete(playerGuessKey);
        redisTemplate.delete(aiGuessKey);

        log.info("Session data cleared for {}", sessionId);
    }
}
