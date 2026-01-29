package com.example.doublecross.domain.entity;

import com.example.doublecross.domain.enums.GameStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "game_session", indexes = {
    @Index(name = "idx_game_session_status", columnList = "status"),
    @Index(name = "idx_game_session_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "session_id")
    private UUID sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private GameStatus status = GameStatus.WAITING;

    @Column(name = "max_turns", nullable = false)
    @Builder.Default
    private Integer maxTurns = 10;

    @Column(name = "turn_time_limit", nullable = false)
    @Builder.Default
    private Integer turnTimeLimit = 90;

    @Column(name = "current_turn", nullable = false)
    @Builder.Default
    private Integer currentTurn = 0;

    @Column(name = "initial_situation", columnDefinition = "TEXT")
    private String initialSituation;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Participant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("turn ASC")
    @Builder.Default
    private List<StoryEntry> storyEntries = new ArrayList<>();

    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private GameResult gameResult;

    public void addParticipant(Participant participant) {
        participants.add(participant);
        participant.setSession(this);
    }

    public void addStoryEntry(StoryEntry storyEntry) {
        storyEntries.add(storyEntry);
        storyEntry.setSession(this);
    }
}
