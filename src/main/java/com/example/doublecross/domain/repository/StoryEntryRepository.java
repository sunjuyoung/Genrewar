package com.example.doublecross.domain.repository;

import com.example.doublecross.domain.entity.StoryEntry;
import com.example.doublecross.domain.enums.ParticipantType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoryEntryRepository extends JpaRepository<StoryEntry, UUID> {

    List<StoryEntry> findBySessionSessionIdOrderByTurnAsc(UUID sessionId);

    List<StoryEntry> findBySessionSessionIdAndAuthorOrderByTurnAsc(UUID sessionId, ParticipantType author);

    Optional<StoryEntry> findBySessionSessionIdAndTurn(UUID sessionId, Integer turn);

    @Query("SELECT se FROM StoryEntry se WHERE se.session.sessionId = :sessionId ORDER BY se.turn ASC")
    List<StoryEntry> findAllBySessionIdOrderByTurn(@Param("sessionId") UUID sessionId);

    @Query("SELECT se FROM StoryEntry se LEFT JOIN FETCH se.keywordUsed WHERE se.session.sessionId = :sessionId ORDER BY se.turn ASC")
    List<StoryEntry> findAllBySessionIdWithKeywordOrderByTurn(@Param("sessionId") UUID sessionId);

    @Query("SELECT se FROM StoryEntry se WHERE se.session.sessionId = :sessionId AND se.turn = (SELECT MAX(se2.turn) FROM StoryEntry se2 WHERE se2.session.sessionId = :sessionId)")
    Optional<StoryEntry> findLatestBySessionId(@Param("sessionId") UUID sessionId);

    @Query("SELECT COUNT(se) FROM StoryEntry se WHERE se.session.sessionId = :sessionId")
    long countBySessionId(@Param("sessionId") UUID sessionId);

    @Query("SELECT COUNT(se) FROM StoryEntry se WHERE se.session.sessionId = :sessionId AND se.author = :author")
    long countBySessionIdAndAuthor(@Param("sessionId") UUID sessionId, @Param("author") ParticipantType author);

    @Query("SELECT se FROM StoryEntry se WHERE se.session.sessionId = :sessionId AND se.keywordUsed IS NOT NULL ORDER BY se.turn ASC")
    List<StoryEntry> findEntriesWithKeywordUsed(@Param("sessionId") UUID sessionId);

    @Query("SELECT COALESCE(MAX(se.turn), 0) FROM StoryEntry se WHERE se.session.sessionId = :sessionId")
    Integer findMaxTurnBySessionId(@Param("sessionId") UUID sessionId);
}
