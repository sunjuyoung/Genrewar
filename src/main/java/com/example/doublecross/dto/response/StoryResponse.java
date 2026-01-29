package com.example.doublecross.dto.response;

import com.example.doublecross.domain.enums.KeywordStatus;
import com.example.doublecross.domain.enums.ParticipantType;
import lombok.Builder;

@Builder
public record StoryResponse(
        boolean success,
        int turn,
        boolean keywordUsed,
        KeywordStatus keywordStatus,
        NextTurnInfo nextTurn
) {
    @Builder
    public record NextTurnInfo(
            int turn,
            ParticipantType author,
            Integer timeLimit
    ) {}

    public static StoryResponse success(int turn, boolean keywordUsed, KeywordStatus keywordStatus,
                                        int nextTurn, ParticipantType nextAuthor, Integer timeLimit) {
        return StoryResponse.builder()
                .success(true)
                .turn(turn)
                .keywordUsed(keywordUsed)
                .keywordStatus(keywordStatus)
                .nextTurn(NextTurnInfo.builder()
                        .turn(nextTurn)
                        .author(nextAuthor)
                        .timeLimit(timeLimit)
                        .build())
                .build();
    }
}
