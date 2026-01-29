package com.example.doublecross.domain.repository;

import com.example.doublecross.domain.entity.Participant;
import com.example.doublecross.domain.enums.ParticipantType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, UUID> {

    List<Participant> findBySessionSessionId(UUID sessionId);

    Optional<Participant> findBySessionSessionIdAndType(UUID sessionId, ParticipantType type);

    @Query("SELECT p FROM Participant p WHERE p.session.sessionId = :sessionId AND p.type = :type")
    Optional<Participant> findBySessionIdAndType(@Param("sessionId") UUID sessionId, @Param("type") ParticipantType type);

    @Query("SELECT p FROM Participant p LEFT JOIN FETCH p.currentKeyword WHERE p.session.sessionId = :sessionId AND p.type = :type")
    Optional<Participant> findBySessionIdAndTypeWithKeyword(@Param("sessionId") UUID sessionId, @Param("type") ParticipantType type);

    @Query("SELECT p FROM Participant p LEFT JOIN FETCH p.currentKeyword WHERE p.session.sessionId = :sessionId")
    List<Participant> findBySessionIdWithKeyword(@Param("sessionId") UUID sessionId);

    @Query("SELECT p FROM Participant p " +
           "LEFT JOIN FETCH p.guessAttempts " +
           "WHERE p.session.sessionId = :sessionId AND p.type = :type")
    Optional<Participant> findBySessionIdAndTypeWithGuessAttempts(@Param("sessionId") UUID sessionId, @Param("type") ParticipantType type);

    @Query("SELECT p FROM Participant p " +
           "LEFT JOIN FETCH p.scoreEvents " +
           "WHERE p.session.sessionId = :sessionId AND p.type = :type")
    Optional<Participant> findBySessionIdAndTypeWithScoreEvents(@Param("sessionId") UUID sessionId, @Param("type") ParticipantType type);

    @Query("SELECT COALESCE(SUM(p.score), 0) FROM Participant p WHERE p.session.sessionId = :sessionId AND p.type = :type")
    Integer getTotalScoreBySessionIdAndType(@Param("sessionId") UUID sessionId, @Param("type") ParticipantType type);
}
