package com.example.doublecross.dto;

import java.util.List;
import java.util.Map;

/**
 * AI 장르 판정 결과
 *
 * @param genreAnalysis 장르별 비율 (0-100)
 * @param qualityFactor 품질 계수 (0.4-1.0)
 * @param unnaturalElements 뜬금없는 요소들
 * @param finalScores 최종 점수 (비율 × 품질계수)
 * @param primaryGenre 주요 장르
 * @param reasoning 판정 근거
 */
public record GenreAnalysis(
    Map<String, Integer> genreAnalysis,
    double qualityFactor,
    List<UnnaturalElement> unnaturalElements,
    Map<String, Double> finalScores,
    String primaryGenre,
    String reasoning
) {
    
    /**
     * 특정 장르의 최종 점수 반환
     */
    public double getFinalScore(String genre) {
        return finalScores.getOrDefault(genre, 0.0);
    }
    
    /**
     * 품질 계수가 감점되었는지 확인
     */
    public boolean hasQualityPenalty() {
        return qualityFactor < 1.0;
    }
    
    /**
     * 뜬금없는 요소 개수
     */
    public int getUnnaturalCount() {
        return unnaturalElements != null ? unnaturalElements.size() : 0;
    }
}
