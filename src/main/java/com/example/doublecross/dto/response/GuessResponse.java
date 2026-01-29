package com.example.doublecross.dto.response;

import com.example.doublecross.dto.GuessResult;
import lombok.Builder;

@Builder
public record GuessResponse(
        boolean correct,
        int points,
        int guessesRemaining,
        int totalScore,
        String message
) {
    public static GuessResponse from(GuessResult result) {
        return GuessResponse.builder()
                .correct(result.isCorrect())
                .points(result.getPoints())
                .guessesRemaining(result.getGuessesRemaining())
                .totalScore(result.getTotalScore())
                .message(result.getMessage())
                .build();
    }
}
