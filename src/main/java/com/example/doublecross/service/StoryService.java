package com.example.doublecross.service;

import com.example.doublecross.domain.entity.GameSession;
import com.example.doublecross.domain.entity.Keyword;
import com.example.doublecross.domain.entity.Participant;
import com.example.doublecross.domain.entity.StoryEntry;
import com.example.doublecross.domain.enums.KeywordStatus;
import com.example.doublecross.domain.enums.ParticipantType;
import com.example.doublecross.domain.repository.ParticipantRepository;
import com.example.doublecross.domain.repository.StoryEntryRepository;
import com.example.doublecross.dto.StorySubmitResult;
import com.example.doublecross.dto.StoryWriteRequest;
import com.example.doublecross.dto.StoryWriteResponse;
import com.example.doublecross.exception.GameException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryEntryRepository storyEntryRepository;
    private final ParticipantRepository participantRepository;
    private final KeywordService keywordService;
    private final StoryWriterService storyWriterService;

    /**
     * 플레이어 스토리 제출
     */
    @Transactional
    public StorySubmitResult submitPlayerStory(GameSession session, String content, boolean useKeyword, Integer timeSpent) {
        UUID sessionId = session.getSessionId();
        int turn = session.getCurrentTurn();

        // 플레이어 조회
        Participant player = participantRepository
                .findBySessionIdAndTypeWithKeyword(sessionId, ParticipantType.PLAYER)
                .orElseThrow(() -> new GameException("플레이어를 찾을 수 없습니다."));

        // 제시어 포함 여부 체크
        Keyword currentKeyword = player.getCurrentKeyword();
        boolean keywordUsed = false;

        if (currentKeyword != null && useKeyword) {
            keywordUsed = keywordService.containsKeyword(content, currentKeyword.getWord());
            if (keywordUsed) {
                keywordService.markKeywordUsed(player);
            }
        }

        // 스토리 항목 저장
        StoryEntry entry = StoryEntry.builder()
                .session(session)
                .turn(turn)
                .author(ParticipantType.PLAYER)
                .content(content)
                .keywordUsed(keywordUsed ? currentKeyword : null)
                .timeSpent(timeSpent)
                .build();

        storyEntryRepository.save(entry);

        log.info("Player submitted story for turn {} (keyword used: {})", turn, keywordUsed);

        return StorySubmitResult.builder()
                .turn(turn)
                .author(ParticipantType.PLAYER)
                .keywordUsed(keywordUsed)
                .keywordStatus(player.getKeywordStatus())
                .build();
    }

    /**
     * AI 스토리 생성 및 저장
     */
    @Transactional
    public StorySubmitResult generateAiStory(GameSession session, boolean shouldUseKeyword) {
        UUID sessionId = session.getSessionId();
        int turn = session.getCurrentTurn();

        // AI 참가자 조회
        Participant ai = participantRepository
                .findBySessionIdAndTypeWithKeyword(sessionId, ParticipantType.AI)
                .orElseThrow(() -> new GameException("AI를 찾을 수 없습니다."));

        // 기존 스토리 조회
        List<StoryEntry> previousEntries = storyEntryRepository.findAllBySessionIdOrderByTurn(sessionId);
        List<StoryWriteRequest.StoryEntry> storySoFar = convertToRequestEntries(previousEntries, session);

        // AI 스토리 생성
        Keyword currentKeyword = ai.getCurrentKeyword();
        StoryWriteRequest request = new StoryWriteRequest(
                ai.getSecretGenre().name(),
                currentKeyword != null ? currentKeyword.getWord() : "",
                ai.getKeywordStatus().name(),
                turn,
                session.getMaxTurns(),
                storySoFar,
                shouldUseKeyword && ai.getKeywordStatus() == KeywordStatus.PENDING
        );

        StoryWriteResponse response = storyWriterService.writeStory(request);

        // 제시어 사용 여부 확인
        boolean keywordUsed = false;
        if (currentKeyword != null && response.keywordUsed()) {
            keywordUsed = keywordService.containsKeyword(response.content(), currentKeyword.getWord());
            if (keywordUsed) {
                keywordService.markKeywordUsed(ai);
            }
        }

        // 스토리 항목 저장
        StoryEntry entry = StoryEntry.builder()
                .session(session)
                .turn(turn)
                .author(ParticipantType.AI)
                .content(response.content())
                .keywordUsed(keywordUsed ? currentKeyword : null)
                .build();

        storyEntryRepository.save(entry);

        log.info("AI generated story for turn {} (keyword used: {})", turn, keywordUsed);

        return StorySubmitResult.builder()
                .turn(turn)
                .author(ParticipantType.AI)
                .content(response.content())
                .keywordUsed(keywordUsed)
                .keywordStatus(ai.getKeywordStatus())
                .build();
    }

    /**
     * 초기 상황 생성
     */
    @Transactional
    public String generateInitialSituation(GameSession session) {
        // 간단한 초기 상황 템플릿 사용 (AI 호출 없이)
        String[] initialSituations = {
                "10년 만에 고향에 돌아온 민수는 낯익은 거리를 걸으며 옛 추억에 잠겼다.",
                "수진은 오래된 카페의 문을 열며 깊은 한숨을 내쉬었다. 오늘따라 마음이 무거웠다.",
                "비가 내리는 저녁, 지훈은 우연히 버스 정류장에서 낯선 사람과 마주쳤다.",
                "대학 동창회 초대장을 받은 영희는 고민 끝에 참석하기로 결심했다.",
                "새로운 도시로 이사 온 첫날, 준혁은 이웃집 문 앞에 놓인 이상한 소포를 발견했다.",
                "자정이 가까운 시각, 서연은 회사에 홀로 남아 야근을 하고 있었다. 갑자기 복도에서 발소리가 들렸다.",
                "졸업 후 처음으로 재회한 두 사람은 어색한 침묵 속에서 서로를 바라보았다.",
                "할머니의 유품을 정리하던 하나는 오래된 상자 속에서 한 통의 편지를 발견했다.",
                "출장길에 오른 태민은 기차에서 우연히 옆자리에 앉은 사람과 눈이 마주쳤다.",
                "폭우가 쏟아지는 밤, 산장에 고립된 다섯 명의 여행객들은 서로를 경계하기 시작했다.",
                "오랜 연인과 헤어진 다음 날, 지은은 익숙한 카페에서 예상치 못한 사람을 만났다.",
                "폐교된 학교를 탐험하던 청년들은 지하실에서 이상한 문을 발견했다.",
                "신입 형사 도윤은 첫 사건 현장에서 믿기 어려운 광경을 목격했다.",
                "결혼식 전날 밤, 신부는 과거의 비밀이 담긴 사진 한 장을 받았다.",
                "해외여행 중 여권을 잃어버린 소희는 낯선 도시에서 길을 헤매고 있었다.",
                "아버지의 장례식에서 처음 만난 이복형제는 서로를 복잡한 눈빛으로 바라보았다.",
                "심야 라디오 DJ인 현우는 매일 밤 같은 시간에 걸려오는 익명의 전화에 호기심을 느꼈다.",
                "오래된 저택을 상속받은 유나는 이사 첫날 밤, 벽 안에서 들려오는 소리를 들었다.",
                "퇴사를 결심한 날, 승아는 엘리베이터에서 10년 전 헤어진 첫사랑과 마주쳤다.",
                "태풍 경보가 내린 섬마을, 마지막 배를 놓친 여행자는 유일하게 불이 켜진 민박집으로 향했다."
        };

        String initialSituation = initialSituations[new java.util.Random().nextInt(initialSituations.length)];
        session.setInitialSituation(initialSituation);

        log.info("Generated initial situation for session {}", session.getSessionId());

        return initialSituation;
    }

    /**
     * StoryEntry를 StoryWriteRequest.StoryEntry로 변환
     */
    private List<StoryWriteRequest.StoryEntry> convertToRequestEntries(List<StoryEntry> entries, GameSession session) {
        List<StoryWriteRequest.StoryEntry> result = new java.util.ArrayList<>();

        // 초기 상황 추가 (턴 0)
        if (session.getInitialSituation() != null) {
            result.add(new StoryWriteRequest.StoryEntry(0, "SYSTEM", session.getInitialSituation()));
        }

        // 각 스토리 항목 추가
        for (StoryEntry entry : entries) {
            result.add(new StoryWriteRequest.StoryEntry(
                    entry.getTurn(),
                    entry.getAuthor().name(),
                    entry.getContent()
            ));
        }

        return result;
    }

    /**
     * 세션의 전체 스토리 조회
     */
    public List<StoryEntry> getStoryEntries(UUID sessionId) {
        return storyEntryRepository.findAllBySessionIdOrderByTurn(sessionId);
    }

    /**
     * 전체 스토리 텍스트 생성
     */
    public String buildFullStory(GameSession session) {
        List<StoryEntry> entries = storyEntryRepository.findAllBySessionIdOrderByTurn(session.getSessionId());

        StringBuilder sb = new StringBuilder();

        // 초기 상황 추가
        if (session.getInitialSituation() != null) {
            sb.append(session.getInitialSituation()).append("\n\n");
        }

        // 각 턴의 스토리 추가
        for (StoryEntry entry : entries) {
            sb.append(entry.getContent()).append("\n\n");
        }

        return sb.toString().trim();
    }

    /**
     * 최대 턴 번호 조회
     */
    public int getMaxTurn(UUID sessionId) {
        return storyEntryRepository.findMaxTurnBySessionId(sessionId);
    }

    /**
     * 특정 턴의 스토리 조회
     */
    public StoryEntry getStoryEntry(UUID sessionId, int turn) {
        return storyEntryRepository.findBySessionSessionIdAndTurn(sessionId, turn)
                .orElse(null);
    }
}
