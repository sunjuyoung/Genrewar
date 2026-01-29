package com.example.doublecross.dto.request;

import com.example.doublecross.domain.enums.Difficulty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;

@Builder
public record CreateGameRequest(
        Difficulty difficulty,

        @Min(value = 5, message = "최소 턴 수는 5입니다")
        @Max(value = 20, message = "최대 턴 수는 20입니다")
        Integer maxTurns,

        @Min(value = 30, message = "최소 턴 제한 시간은 30초입니다")
        @Max(value = 180, message = "최대 턴 제한 시간은 180초입니다")
        Integer turnTimeLimit
) {
    public CreateGameRequest {
        if (difficulty == null) {
            difficulty = Difficulty.NORMAL;
        }
        if (maxTurns == null) {
            maxTurns = 10;
        }
        if (turnTimeLimit == null) {
            turnTimeLimit = 90;
        }
    }
}
