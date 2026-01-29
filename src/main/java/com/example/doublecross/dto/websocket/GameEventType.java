package com.example.doublecross.dto.websocket;

public enum GameEventType {
    // 게임 상태
    GAME_STARTED,
    GAME_FINISHED,

    // 턴 관련
    AI_TURN_COMPLETED,
    PLAYER_TURN_COMPLETED,
    TURN_SKIPPED,

    // 추측 관련
    GUESS_RESULT,

    // 타이머 관련
    TIMER_UPDATE,
    TIMER_EXPIRED,

    // 연결 관련
    PLAYER_CONNECTED,
    PLAYER_DISCONNECTED,

    // 에러
    ERROR
}
