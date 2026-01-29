package com.example.doublecross.dto;

/**
 * Story Writer AI 응답 DTO
 *
 * @param content 작성된 스토리 내용 (1-3문장)
 * @param keywordUsed 제시어 사용 여부
 * @param bluffWord 블러핑 단어 (있으면)
 * @param reasoning AI의 전략적 판단 근거
 */
public record StoryWriteResponse(
        String content,
        boolean keywordUsed,
        String bluffWord,
        String reasoning
) {

    /**
     * 제시어가 실제로 포함되어 있는지 확인
     */
    public boolean containsKeyword(String keyword) {
        return content != null && content.contains(keyword);
    }

    /**
     * 블러핑 단어가 있는지 확인
     */
    public boolean hasBluff() {
        return bluffWord != null && !bluffWord.isBlank();
    }
}
