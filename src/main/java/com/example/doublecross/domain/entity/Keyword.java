package com.example.doublecross.domain.entity;

import com.example.doublecross.domain.enums.Difficulty;
import com.example.doublecross.domain.enums.Genre;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "keyword", indexes = {
    @Index(name = "idx_keyword_genre", columnList = "target_genre"),
    @Index(name = "idx_keyword_difficulty", columnList = "difficulty")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "keyword_id")
    private UUID keywordId;

    @Column(name = "word", nullable = false, length = 50)
    private String word;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_genre", nullable = false, length = 20)
    private Genre targetGenre;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 10)
    @Builder.Default
    private Difficulty difficulty = Difficulty.NORMAL;
}
