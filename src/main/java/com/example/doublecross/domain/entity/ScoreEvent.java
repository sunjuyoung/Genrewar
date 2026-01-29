package com.example.doublecross.domain.entity;

import com.example.doublecross.domain.enums.ScoreEventType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "score_event", indexes = {
    @Index(name = "idx_score_event_participant", columnList = "participant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id")
    private UUID eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @Column(name = "turn", nullable = false)
    private Integer turn;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private ScoreEventType eventType;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
