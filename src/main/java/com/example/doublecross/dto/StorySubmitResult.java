package com.example.doublecross.dto;

import com.example.doublecross.domain.enums.KeywordStatus;
import com.example.doublecross.domain.enums.ParticipantType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StorySubmitResult {

    private final int turn;
    private final ParticipantType author;
    private final String content;
    private final boolean keywordUsed;
    private final KeywordStatus keywordStatus;
}
