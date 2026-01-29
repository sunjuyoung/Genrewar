package com.example.doublecross.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitStoryRequest(
        @NotBlank(message = "스토리 내용은 필수입니다")
        @Size(max = 500, message = "스토리는 500자를 초과할 수 없습니다")
        String content,

        boolean useKeyword,

        Integer timeSpent
) {}
