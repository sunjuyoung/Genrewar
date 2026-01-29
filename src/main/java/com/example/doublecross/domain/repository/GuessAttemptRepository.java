package com.example.doublecross.domain.repository;

import com.example.doublecross.domain.entity.GuessAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuessAttemptRepository extends JpaRepository<GuessAttempt, UUID> {

    List<GuessAttempt> findByParticipantParticipantId(UUID participantId);

    List<GuessAttempt> findByParticipantParticipantIdOrderByCreatedAtAsc(UUID participantId);

    @Query("SELECT ga FROM GuessAttempt ga WHERE ga.participant.participantId = :participantId ORDER BY ga.turn ASC")
    List<GuessAttempt> findAllByParticipantIdOrderByTurn(@Param("participantId") UUID participantId);

    @Query("SELECT ga FROM GuessAttempt ga WHERE ga.participant.session.sessionId = :sessionId ORDER BY ga.turn ASC, ga.createdAt ASC")
    List<GuessAttempt> findAllBySessionIdOrderByTurn(@Param("sessionId") UUID sessionId);

    @Query("SELECT COUNT(ga) FROM GuessAttempt ga WHERE ga.participant.participantId = :participantId AND ga.isCorrect = true")
    long countCorrectByParticipantId(@Param("participantId") UUID participantId);

    @Query("SELECT COUNT(ga) FROM GuessAttempt ga WHERE ga.participant.participantId = :participantId AND ga.isCorrect = false")
    long countWrongByParticipantId(@Param("participantId") UUID participantId);

    @Query("SELECT COUNT(ga) FROM GuessAttempt ga WHERE ga.participant.participantId = :participantId")
    long countByParticipantId(@Param("participantId") UUID participantId);

    @Query("SELECT ga FROM GuessAttempt ga WHERE ga.participant.participantId = :participantId AND ga.turn = :turn")
    List<GuessAttempt> findByParticipantIdAndTurn(@Param("participantId") UUID participantId, @Param("turn") Integer turn);
}
