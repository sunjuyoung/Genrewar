package com.example.doublecross.domain.enums;

public enum KeywordStatus {
    PENDING,    // 아직 사용하지 않음
    USED,       // 스토리에 사용함
    DIGESTED,   // 사용했지만 들키지 않음 (게임 종료 시 보너스)
    CAUGHT      // 사용했고 상대가 맞춤
}
