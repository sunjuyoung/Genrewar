package com.example.doublecross.domain.repository;

import com.example.doublecross.domain.entity.GameResult;
import com.example.doublecross.domain.enums.Winner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameResultRepository extends JpaRepository<GameResult, UUID> {

    Optional<GameResult> findBySessionSessionId(UUID sessionId);

    @Query("SELECT gr FROM GameResult gr LEFT JOIN FETCH gr.session WHERE gr.session.sessionId = :sessionId")
    Optional<GameResult> findBySessionIdWithSession(@Param("sessionId") UUID sessionId);

    List<GameResult> findByWinner(Winner winner);

    @Query("SELECT gr FROM GameResult gr ORDER BY gr.createdAt DESC")
    List<GameResult> findAllOrderByCreatedAtDesc();

    @Query("SELECT COUNT(gr) FROM GameResult gr WHERE gr.winner = :winner")
    long countByWinner(@Param("winner") Winner winner);

    @Query("SELECT AVG(gr.playerScore) FROM GameResult gr")
    Double getAveragePlayerScore();

    @Query("SELECT AVG(gr.aiScore) FROM GameResult gr")
    Double getAverageAiScore();

    @Query("SELECT gr.winner, COUNT(gr) FROM GameResult gr GROUP BY gr.winner")
    List<Object[]> getWinnerStatistics();

    boolean existsBySessionSessionId(UUID sessionId);
}
