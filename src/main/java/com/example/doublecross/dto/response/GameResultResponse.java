package com.example.doublecross.dto.response;

import com.example.doublecross.domain.entity.GameResult;
import com.example.doublecross.domain.entity.Participant;
import com.example.doublecross.domain.enums.Genre;
import com.example.doublecross.domain.enums.KeywordStatus;
import com.example.doublecross.domain.enums.ScoreEventType;
import com.example.doublecross.domain.enums.Winner;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
public record GameResultResponse(
        UUID sessionId,
        Winner winner,
        Map<String, Integer> genreAnalysis,
        double qualityFactor,
        FinalScores finalScores,
        ScoreInfo scores,
        RevealedInfo revealed,
        List<UnnaturalElementInfo> unnaturalElements,
        String fullStory
) {
    @Builder
    public record FinalScores(
            double romanceScore,
            double thrillerScore
    ) {}

    @Builder
    public record ScoreInfo(
            PlayerScoreInfo player,
            PlayerScoreInfo ai
    ) {}

    @Builder
    public record PlayerScoreInfo(
            int total,
            ScoreBreakdown breakdown
    ) {}

    @Builder
    public record ScoreBreakdown(
            int genreWin,
            int genreBonus,
            int guessCorrect,
            int guessWrong,
            int digestSuccess
    ) {
        public static ScoreBreakdown from(Map<ScoreEventType, Integer> breakdown) {
            return ScoreBreakdown.builder()
                    .genreWin(breakdown.getOrDefault(ScoreEventType.GENRE_WIN, 0))
                    .genreBonus(breakdown.getOrDefault(ScoreEventType.GENRE_BONUS, 0))
                    .guessCorrect(breakdown.getOrDefault(ScoreEventType.GUESS_CORRECT, 0))
                    .guessWrong(breakdown.getOrDefault(ScoreEventType.GUESS_WRONG, 0))
                    .digestSuccess(breakdown.getOrDefault(ScoreEventType.DIGEST_SUCCESS, 0))
                    .build();
        }
    }

    @Builder
    public record RevealedInfo(
            Genre playerGenre,
            Genre aiGenre,
            List<KeywordUsageInfo> playerKeywords,
            List<KeywordUsageInfo> aiKeywords
    ) {}

    @Builder
    public record KeywordUsageInfo(
            String word,
            KeywordStatus status,
            Integer usedAtTurn,
            Integer caughtAtTurn
    ) {}

    @Builder
    public record UnnaturalElementInfo(
            int turn,
            String element,
            String reason
    ) {
        public static UnnaturalElementInfo from(GameResult.UnnaturalElement ue) {
            return UnnaturalElementInfo.builder()
                    .turn(ue.getTurn())
                    .element(ue.getElement())
                    .reason(ue.getReason())
                    .build();
        }
    }

    public static GameResultResponse from(GameResult result, Participant player, Participant ai,
                                          Map<ScoreEventType, Integer> playerBreakdown,
                                          Map<ScoreEventType, Integer> aiBreakdown,
                                          String fullStory) {
        return GameResultResponse.builder()
                .sessionId(result.getSession().getSessionId())
                .winner(result.getWinner())
                .genreAnalysis(result.getGenreAnalysis())
                .qualityFactor(result.getQualityFactor().doubleValue())
                .finalScores(FinalScores.builder()
                        .romanceScore(result.getGenreAnalysis().getOrDefault("ROMANCE", 0) * result.getQualityFactor().doubleValue())
                        .thrillerScore(result.getGenreAnalysis().getOrDefault("THRILLER", 0) * result.getQualityFactor().doubleValue())
                        .build())
                .scores(ScoreInfo.builder()
                        .player(PlayerScoreInfo.builder()
                                .total(result.getPlayerScore())
                                .breakdown(ScoreBreakdown.from(playerBreakdown))
                                .build())
                        .ai(PlayerScoreInfo.builder()
                                .total(result.getAiScore())
                                .breakdown(ScoreBreakdown.from(aiBreakdown))
                                .build())
                        .build())
                .revealed(RevealedInfo.builder()
                        .playerGenre(player.getSecretGenre())
                        .aiGenre(ai.getSecretGenre())
                        .playerKeywords(List.of(KeywordUsageInfo.builder()
                                .word(player.getCurrentKeyword() != null ? player.getCurrentKeyword().getWord() : null)
                                .status(player.getKeywordStatus())
                                .build()))
                        .aiKeywords(List.of(KeywordUsageInfo.builder()
                                .word(ai.getCurrentKeyword() != null ? ai.getCurrentKeyword().getWord() : null)
                                .status(ai.getKeywordStatus())
                                .build()))
                        .build())
                .unnaturalElements(result.getUnnaturalElements() != null ?
                        result.getUnnaturalElements().stream()
                                .map(UnnaturalElementInfo::from)
                                .toList() : List.of())
                .fullStory(fullStory)
                .build();
    }
}
