package com.example.doublecross.domain.repository;

import com.example.doublecross.domain.entity.ScoreEvent;
import com.example.doublecross.domain.enums.ScoreEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScoreEventRepository extends JpaRepository<ScoreEvent, UUID> {

    List<ScoreEvent> findByParticipantParticipantId(UUID participantId);

    List<ScoreEvent> findByParticipantParticipantIdOrderByTurnAsc(UUID participantId);

    List<ScoreEvent> findByParticipantParticipantIdAndEventType(UUID participantId, ScoreEventType eventType);

    @Query("SELECT se FROM ScoreEvent se WHERE se.participant.participantId = :participantId ORDER BY se.turn ASC, se.createdAt ASC")
    List<ScoreEvent> findAllByParticipantIdOrderByTurn(@Param("participantId") UUID participantId);

    @Query("SELECT se FROM ScoreEvent se WHERE se.participant.session.sessionId = :sessionId ORDER BY se.turn ASC, se.createdAt ASC")
    List<ScoreEvent> findAllBySessionIdOrderByTurn(@Param("sessionId") UUID sessionId);

    @Query("SELECT COALESCE(SUM(se.points), 0) FROM ScoreEvent se WHERE se.participant.participantId = :participantId")
    Integer sumPointsByParticipantId(@Param("participantId") UUID participantId);

    @Query("SELECT COALESCE(SUM(se.points), 0) FROM ScoreEvent se WHERE se.participant.participantId = :participantId AND se.eventType = :eventType")
    Integer sumPointsByParticipantIdAndEventType(@Param("participantId") UUID participantId, @Param("eventType") ScoreEventType eventType);

    @Query("SELECT se.eventType, COALESCE(SUM(se.points), 0) FROM ScoreEvent se WHERE se.participant.participantId = :participantId GROUP BY se.eventType")
    List<Object[]> getScoreBreakdownByParticipantId(@Param("participantId") UUID participantId);

    @Query("SELECT COUNT(se) FROM ScoreEvent se WHERE se.participant.participantId = :participantId AND se.eventType = :eventType")
    long countByParticipantIdAndEventType(@Param("participantId") UUID participantId, @Param("eventType") ScoreEventType eventType);
}
