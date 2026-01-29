package com.example.doublecross.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GuessRequest(
        @NotBlank(message = "추측 단어는 필수입니다")
        @Size(min = 2, max = 50, message = "추측 단어는 2-50자 사이여야 합니다")
        String guessWord
) {}
