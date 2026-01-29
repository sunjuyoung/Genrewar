package com.example.doublecross.service;

import com.example.doublecross.domain.entity.Keyword;
import com.example.doublecross.domain.entity.Participant;
import com.example.doublecross.domain.enums.Difficulty;
import com.example.doublecross.domain.enums.Genre;
import com.example.doublecross.domain.enums.KeywordStatus;
import com.example.doublecross.domain.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final Random random = new Random();

    /**
     * 텍스트에 제시어가 포함되어 있는지 확인
     * - 조사 결합 허용: "좀비가", "좀비를" 등
     * - 합성어 허용: "좀비영화", "좀비처럼" 등
     */
    public boolean containsKeyword(String text, String keyword) {
        if (text == null || keyword == null) {
            return false;
        }
        return text.contains(keyword);
    }

    /**
     * 참가자에게 새로운 제시어 배정
     * 상대방 장르에서 랜덤으로 선택
     */
    @Transactional
    public Keyword assignNewKeyword(Participant participant, Genre opponentGenre, Difficulty difficulty) {
        Keyword keyword = findRandomKeyword(opponentGenre, difficulty);

        participant.setCurrentKeyword(keyword);
        participant.setKeywordStatus(KeywordStatus.PENDING);

        log.info("Assigned new keyword '{}' to participant {} (target genre: {})",
                keyword.getWord(), participant.getParticipantId(), opponentGenre);

        return keyword;
    }

    /**
     * 특정 장르와 난이도에서 랜덤 제시어 조회
     */
    public Keyword findRandomKeyword(Genre genre, Difficulty difficulty) {
        List<Keyword> keywords = keywordRepository.findByTargetGenreAndDifficulty(genre, difficulty);

        if (keywords.isEmpty()) {
            // 해당 난이도에 제시어가 없으면 장르만으로 조회
            keywords = keywordRepository.findByTargetGenre(genre);
        }

        if (keywords.isEmpty()) {
            throw new IllegalStateException("No keywords found for genre: " + genre);
        }

        return keywords.get(random.nextInt(keywords.size()));
    }

    /**
     * 특정 장르에서 랜덤 제시어 조회
     */
    public Keyword findRandomKeyword(Genre genre) {
        List<Keyword> keywords = keywordRepository.findByTargetGenre(genre);

        if (keywords.isEmpty()) {
            throw new IllegalStateException("No keywords found for genre: " + genre);
        }

        return keywords.get(random.nextInt(keywords.size()));
    }

    /**
     * 제시어 사용 처리
     */
    @Transactional
    public void markKeywordUsed(Participant participant) {
        if (participant.getKeywordStatus() == KeywordStatus.PENDING) {
            participant.setKeywordStatus(KeywordStatus.USED);
            participant.incrementKeywordsUsed();
            log.info("Keyword '{}' marked as USED for participant {}",
                    participant.getCurrentKeyword().getWord(), participant.getParticipantId());
        }
    }

    /**
     * 제시어가 들킨 경우 처리
     */
    @Transactional
    public void markKeywordCaught(Participant participant) {
        participant.setKeywordStatus(KeywordStatus.CAUGHT);
        log.info("Keyword '{}' was CAUGHT for participant {}",
                participant.getCurrentKeyword().getWord(), participant.getParticipantId());
    }

    /**
     * 제시어 소화 성공 처리 (게임 종료 시)
     */
    @Transactional
    public void markKeywordDigested(Participant participant) {
        if (participant.getKeywordStatus() == KeywordStatus.USED) {
            participant.setKeywordStatus(KeywordStatus.DIGESTED);
            participant.incrementKeywordsDigested();
            log.info("Keyword '{}' was DIGESTED for participant {}",
                    participant.getCurrentKeyword().getWord(), participant.getParticipantId());
        }
    }

    /**
     * 모든 제시어 조회
     */
    public List<Keyword> findAllKeywords() {
        return keywordRepository.findAll();
    }

    /**
     * 특정 장르의 모든 제시어 조회
     */
    public List<Keyword> findKeywordsByGenre(Genre genre) {
        return keywordRepository.findByTargetGenre(genre);
    }
}
