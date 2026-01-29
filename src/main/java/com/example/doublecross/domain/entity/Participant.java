package com.example.doublecross.domain.entity;

import com.example.doublecross.domain.enums.Genre;
import com.example.doublecross.domain.enums.KeywordStatus;
import com.example.doublecross.domain.enums.ParticipantType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "participant", indexes = {
    @Index(name = "idx_participant_session", columnList = "session_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "idx_participant_session_type", columnNames = {"session_id", "type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "participant_id")
    private UUID participantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private GameSession session;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private ParticipantType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "secret_genre", nullable = false, length = 20)
    private Genre secretGenre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_keyword_id")
    private Keyword currentKeyword;

    @Enumerated(EnumType.STRING)
    @Column(name = "keyword_status", nullable = false, length = 20)
    @Builder.Default
    private KeywordStatus keywordStatus = KeywordStatus.PENDING;

    @Column(name = "guesses_remaining", nullable = false)
    @Builder.Default
    private Integer guessesRemaining = 3;

    @Column(name = "score", nullable = false)
    @Builder.Default
    private Integer score = 0;

    @Column(name = "keywords_used", nullable = false)
    @Builder.Default
    private Integer keywordsUsed = 0;

    @Column(name = "keywords_digested", nullable = false)
    @Builder.Default
    private Integer keywordsDigested = 0;

    @Column(name = "correct_guesses", nullable = false)
    @Builder.Default
    private Integer correctGuesses = 0;

    @Column(name = "wrong_guesses", nullable = false)
    @Builder.Default
    private Integer wrongGuesses = 0;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GuessAttempt> guessAttempts = new ArrayList<>();

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ScoreEvent> scoreEvents = new ArrayList<>();

    public void decrementGuesses() {
        if (this.guessesRemaining > 0) {
            this.guessesRemaining--;
        }
    }

    public void addScore(int points) {
        this.score += points;
    }

    public void incrementKeywordsUsed() {
        this.keywordsUsed++;
    }

    public void incrementKeywordsDigested() {
        this.keywordsDigested++;
    }

    public void incrementCorrectGuesses() {
        this.correctGuesses++;
    }

    public void incrementWrongGuesses() {
        this.wrongGuesses++;
    }
}
