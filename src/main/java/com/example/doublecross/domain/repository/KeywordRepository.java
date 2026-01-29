package com.example.doublecross.domain.repository;

import com.example.doublecross.domain.entity.Keyword;
import com.example.doublecross.domain.enums.Difficulty;
import com.example.doublecross.domain.enums.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, UUID> {

    List<Keyword> findByTargetGenre(Genre targetGenre);

    List<Keyword> findByDifficulty(Difficulty difficulty);

    List<Keyword> findByTargetGenreAndDifficulty(Genre targetGenre, Difficulty difficulty);

    @Query("SELECT k FROM Keyword k WHERE k.targetGenre = :genre ORDER BY FUNCTION('RANDOM')")
    List<Keyword> findRandomByGenre(@Param("genre") Genre genre);

    @Query("SELECT k FROM Keyword k WHERE k.targetGenre = :genre AND k.difficulty = :difficulty ORDER BY FUNCTION('RANDOM')")
    List<Keyword> findRandomByGenreAndDifficulty(@Param("genre") Genre genre, @Param("difficulty") Difficulty difficulty);

    @Query(value = "SELECT * FROM keyword WHERE target_genre = :genre ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Keyword findOneRandomByGenre(@Param("genre") String genre);

    @Query(value = "SELECT * FROM keyword WHERE target_genre = :genre AND difficulty = :difficulty ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Keyword findOneRandomByGenreAndDifficulty(@Param("genre") String genre, @Param("difficulty") String difficulty);

    boolean existsByWord(String word);

    List<Keyword> findByWordContaining(String word);
}
