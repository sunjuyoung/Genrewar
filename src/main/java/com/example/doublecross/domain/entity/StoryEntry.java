package com.example.doublecross.domain.entity;

import com.example.doublecross.domain.enums.ParticipantType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "story_entry", indexes = {
    @Index(name = "idx_story_entry_session", columnList = "session_id"),
    @Index(name = "idx_story_entry_session_turn", columnList = "session_id, turn")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "entry_id")
    private UUID entryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private GameSession session;

    @Column(name = "turn", nullable = false)
    private Integer turn;

    @Enumerated(EnumType.STRING)
    @Column(name = "author", nullable = false, length = 10)
    private ParticipantType author;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_used_id")
    private Keyword keywordUsed;

    @Column(name = "time_spent")
    private Integer timeSpent;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
