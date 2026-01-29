package com.example.doublecross.domain.entity;

import com.example.doublecross.domain.enums.Winner;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "game_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "result_id")
    private UUID resultId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private GameSession session;

    @Column(name = "player_score", nullable = false)
    private Integer playerScore;

    @Column(name = "ai_score", nullable = false)
    private Integer aiScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "winner", nullable = false, length = 10)
    private Winner winner;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "genre_analysis", nullable = false, columnDefinition = "jsonb")
    private Map<String, Integer> genreAnalysis;

    @Column(name = "quality_factor", nullable = false, precision = 3, scale = 2)
    private BigDecimal qualityFactor;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "unnatural_elements", columnDefinition = "jsonb")
    private List<UnnaturalElement> unnaturalElements;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UnnaturalElement {
        @JsonProperty("turn")      // ✅ 추가
        private Integer turn;

        @JsonProperty("element")   // ✅ 추가
        private String element;

        @JsonProperty("reason")    // ✅ 추가
        private String reason;
    }
}
