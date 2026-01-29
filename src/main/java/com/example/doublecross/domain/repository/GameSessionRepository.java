package com.example.doublecross.domain.repository;

import com.example.doublecross.domain.entity.GameSession;
import com.example.doublecross.domain.enums.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, UUID> {

    List<GameSession> findByStatus(GameStatus status);

    List<GameSession> findByStatusOrderByCreatedAtDesc(GameStatus status);

    List<GameSession> findByStatusIn(List<GameStatus> statuses);

    @Query("SELECT gs FROM GameSession gs WHERE gs.status = :status AND gs.createdAt > :since ORDER BY gs.createdAt DESC")
    List<GameSession> findRecentByStatus(@Param("status") GameStatus status, @Param("since") OffsetDateTime since);

    @Query("SELECT gs FROM GameSession gs LEFT JOIN FETCH gs.participants WHERE gs.sessionId = :sessionId")
    Optional<GameSession> findByIdWithParticipants(@Param("sessionId") UUID sessionId);

    @Query("SELECT gs FROM GameSession gs LEFT JOIN FETCH gs.storyEntries WHERE gs.sessionId = :sessionId")
    Optional<GameSession> findByIdWithStoryEntries(@Param("sessionId") UUID sessionId);

    @Query("SELECT gs FROM GameSession gs " +
           "LEFT JOIN FETCH gs.participants " +
           "LEFT JOIN FETCH gs.storyEntries " +
           "WHERE gs.sessionId = :sessionId")
    Optional<GameSession> findByIdWithParticipantsAndStoryEntries(@Param("sessionId") UUID sessionId);

    long countByStatus(GameStatus status);
}
