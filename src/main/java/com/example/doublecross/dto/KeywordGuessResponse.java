package com.example.doublecross.dto;


import java.util.List;

/**
 * Keyword Guesser AI 응답 DTO
 *
 * @param decision 결정 ("GUESS" 또는 "PASS")
 * @param guessWord 추측한 단어 (PASS면 null)
 * @param confidence 확신도 (0-100)
 * @param suspiciousWords 의심스러운 단어 목록
 * @param reasoning 추론 근거
 */
public record KeywordGuessResponse(
        String decision,
        String guessWord,
        int confidence,
        List<SuspiciousWord> suspiciousWords,
        String reasoning
) {

    /**
     * 의심스러운 단어 정보
     */
    public record SuspiciousWord(
            int turn,
            String word,
            int suspicionLevel,  // 0-100
            String reason
    ) {}

    /**
     * 추측 결정인지 확인
     */
    public boolean isGuess() {
        return "GUESS".equalsIgnoreCase(decision);
    }

    /**
     * 패스 결정인지 확인
     */
    public boolean isPass() {
        return "PASS".equalsIgnoreCase(decision);
    }

    /**
     * 높은 확신도인지 확인 (70% 이상)
     */
    public boolean isHighConfidence() {
        return confidence >= 70;
    }

    /**
     * 가장 의심스러운 단어 반환
     */
    public SuspiciousWord getMostSuspiciousWord() {
        if (suspiciousWords == null || suspiciousWords.isEmpty()) {
            return null;
        }
        return suspiciousWords.stream()
                .max((a, b) -> Integer.compare(a.suspicionLevel(), b.suspicionLevel()))
                .orElse(null);
    }
}

