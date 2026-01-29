package com.example.doublecross.dto.response;

import com.example.doublecross.domain.entity.GameSession;
import com.example.doublecross.domain.entity.Participant;
import com.example.doublecross.domain.entity.StoryEntry;
import com.example.doublecross.domain.enums.GameStatus;
import com.example.doublecross.domain.enums.Genre;
import com.example.doublecross.domain.enums.KeywordStatus;
import com.example.doublecross.domain.enums.ParticipantType;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record GameResponse(
        UUID sessionId,
        GameStatus status,
        int currentTurn,
        PlayerInfo player,
        List<StoryEntryInfo> story,
        GameSettings settings,
        OffsetDateTime createdAt
) {
    @Builder
    public record PlayerInfo(
            UUID participantId,
            Genre secretGenre,
            KeywordInfo keyword,
            int guessesRemaining,
            int score
    ) {
        public static PlayerInfo from(Participant participant) {
            return PlayerInfo.builder()
                    .participantId(participant.getParticipantId())
                    .secretGenre(participant.getSecretGenre())
                    .keyword(participant.getCurrentKeyword() != null ?
                            new KeywordInfo(
                                    participant.getCurrentKeyword().getWord(),
                                    participant.getKeywordStatus()
                            ) : null)
                    .guessesRemaining(participant.getGuessesRemaining())
                    .score(participant.getScore())
                    .build();
        }
    }

    public record KeywordInfo(
            String word,
            KeywordStatus status
    ) {}

    @Builder
    public record StoryEntryInfo(
            int turn,
            ParticipantType author,
            String content
    ) {
        public static StoryEntryInfo from(StoryEntry entry) {
            return StoryEntryInfo.builder()
                    .turn(entry.getTurn())
                    .author(entry.getAuthor())
                    .content(entry.getContent())
                    .build();
        }
    }

    @Builder
    public record GameSettings(
            int maxTurns,
            int turnTimeLimit
    ) {}

    public static GameResponse from(GameSession session, Participant player, List<StoryEntry> storyEntries) {
        List<StoryEntryInfo> storyInfos = storyEntries.stream()
                .map(StoryEntryInfo::from)
                .toList();

        // 초기 상황도 스토리에 포함
        if (session.getInitialSituation() != null) {
            StoryEntryInfo initialEntry = StoryEntryInfo.builder()
                    .turn(0)
                    .author(null)  // SYSTEM
                    .content(session.getInitialSituation())
                    .build();

            storyInfos = new java.util.ArrayList<>();
            storyInfos.add(initialEntry);
            storyInfos.addAll(storyEntries.stream().map(StoryEntryInfo::from).toList());
        }

        return GameResponse.builder()
                .sessionId(session.getSessionId())
                .status(session.getStatus())
                .currentTurn(session.getCurrentTurn())
                .player(PlayerInfo.from(player))
                .story(storyInfos)
                .settings(GameSettings.builder()
                        .maxTurns(session.getMaxTurns())
                        .turnTimeLimit(session.getTurnTimeLimit())
                        .build())
                .createdAt(session.getCreatedAt())
                .build();
    }
}
