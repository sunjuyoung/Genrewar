package com.example.doublecross.dto;


import java.util.List;

/**
 * Story Writer AI 요청 DTO
 *
 * @param aiGenre AI의 비밀 장르
 * @param aiKeyword AI의 제시어
 * @param keywordStatus 제시어 상태 (PENDING, USED)
 * @param currentTurn 현재 턴
 * @param maxTurns 최대 턴
 * @param storySoFar 지금까지의 스토리
 * @param shouldUseKeyword 이번 턴에 제시어를 사용해야 하는지 (테스트용)
 */
public record StoryWriteRequest(
        String aiGenre,
        String aiKeyword,
        String keywordStatus,
        int currentTurn,
        int maxTurns,
        List<StoryEntry> storySoFar,
        boolean shouldUseKeyword
) {

    /**
     * 스토리 항목
     */
    public record StoryEntry(
            int turn,
            String author,  // "PLAYER" or "AI"
            String content
    ) {}

    /**
     * 지금까지 스토리를 문자열로 변환
     */
    public String getStoryAsString() {
        if (storySoFar == null || storySoFar.isEmpty()) {
            return "(아직 스토리 없음)";
        }

        StringBuilder sb = new StringBuilder();
        for (StoryEntry entry : storySoFar) {
            String authorIcon = entry.author().equals("PLAYER") ? "👤" : "🤖";
            sb.append(String.format("%s Turn %d: %s\n\n", authorIcon, entry.turn(), entry.content()));
        }
        return sb.toString().trim();
    }
}

