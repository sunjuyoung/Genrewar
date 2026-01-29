package com.example.doublecross.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "guess_attempt", indexes = {
    @Index(name = "idx_guess_attempt_participant", columnList = "participant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuessAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "guess_id")
    private UUID guessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @Column(name = "turn", nullable = false)
    private Integer turn;

    @Column(name = "guessed_word", nullable = false, length = 50)
    private String guessedWord;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
