package com.example.doublecross.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GuessResult {

    private final boolean correct;
    private final String guessWord;
    private final int points;
    private final int guessesRemaining;
    private final int totalScore;
    private final String message;
    private final boolean hasNoChances;

    public static GuessResult correct(String guessWord, int points, int guessesRemaining,
                                      int totalScore, String message) {
        return GuessResult.builder()
                .correct(true)
                .guessWord(guessWord)
                .points(points)
                .guessesRemaining(guessesRemaining)
                .totalScore(totalScore)
                .message(message)
                .hasNoChances(false)
                .build();
    }

    public static GuessResult wrong(String guessWord, int points, int guessesRemaining,
                                    int totalScore, String message) {
        return GuessResult.builder()
                .correct(false)
                .guessWord(guessWord)
                .points(points)
                .guessesRemaining(guessesRemaining)
                .totalScore(totalScore)
                .message(message)
                .hasNoChances(guessesRemaining <= 0)
                .build();
    }

    public static GuessResult noChances(int totalScore) {
        return GuessResult.builder()
                .correct(false)
                .guessWord(null)
                .points(0)
                .guessesRemaining(0)
                .totalScore(totalScore)
                .message("추측 기회가 없습니다.")
                .hasNoChances(true)
                .build();
    }
}
