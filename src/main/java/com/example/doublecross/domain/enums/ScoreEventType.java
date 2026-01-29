package com.example.doublecross.domain.enums;

public enum ScoreEventType {
    GUESS_CORRECT,    // 상대 제시어 맞춤 (+5)
    GUESS_WRONG,      // 상대 제시어 틀림 (-1)
    DIGEST_SUCCESS,   // 제시어 소화 성공 (+2)
    GENRE_WIN,        // 장르 싸움 승리 (+10)
    GENRE_BONUS       // 압도적 승리 보너스 (+5, 70% 이상)
}
