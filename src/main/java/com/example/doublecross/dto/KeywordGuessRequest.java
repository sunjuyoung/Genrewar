package com.example.doublecross.dto;


import java.util.List;

/**
 * Keyword Guesser AI 요청 DTO
 *
 * @param opponentGenreGuess 상대 장르 추정 (모르면 null)
 * @param myGenre 내 장르 (상대에게 안 어울리는 단어가 제시어)
 * @param guessesRemaining 남은 추측 기회
 * @param currentTurn 현재 턴
 * @param maxTurns 최대 턴
 * @param fullStory 지금까지의 전체 스토리
 */
public record KeywordGuessRequest(
        String opponentGenreGuess,
        String myGenre,
        int guessesRemaining,
        int currentTurn,
        int maxTurns,
        List<StoryEntry> fullStory
) {

    /**
     * 스토리 항목
     */
    public record StoryEntry(
            int turn,
            String author,  // "PLAYER" or "AI"  (AI 시점에서 PLAYER가 상대)
            String content
    ) {}

    /**
     * 전체 스토리를 문자열로 변환
     */
    public String getStoryAsString() {
        if (fullStory == null || fullStory.isEmpty()) {
            return "(아직 스토리 없음)";
        }

        StringBuilder sb = new StringBuilder();
        for (StoryEntry entry : fullStory) {
            String authorLabel = entry.author().equals("PLAYER") ? "[상대]" : "[나]";
            sb.append(String.format("Turn %d %s: %s\n\n", entry.turn(), authorLabel, entry.content()));
        }
        return sb.toString().trim();
    }

    /**
     * 상대(PLAYER)의 문장만 추출
     */
    public String getOpponentStoryOnly() {
        if (fullStory == null || fullStory.isEmpty()) {
            return "(상대 스토리 없음)";
        }

        StringBuilder sb = new StringBuilder();
        for (StoryEntry entry : fullStory) {
            if (entry.author().equals("PLAYER")) {
                sb.append(String.format("Turn %d: %s\n\n", entry.turn(), entry.content()));
            }
        }
        return sb.toString().trim();
    }
}
