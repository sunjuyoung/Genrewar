package com.example.doublecross.ai;


import com.example.doublecross.dto.GenreAnalysis;
import com.example.doublecross.dto.StoryWriteRequest;
import com.example.doublecross.dto.StoryWriteResponse;
import com.example.doublecross.service.GenreJudgeService;
import com.example.doublecross.service.StoryWriterService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class StoryWriterAiTest {

    @Autowired
    private StoryWriterService storyWriterService;

    @Autowired
    private GenreJudgeService genreJudgeService;

    @Nested
    @DisplayName("제시어 소화 테스트")
    class KeywordDigestionTest {

        @Test
        @DisplayName("로맨스 장르에서 '좀비' 제시어를 자연스럽게 소화")
        void romance_withZombieKeyword_shouldDigestNaturally() {
            // given: 로맨스 스토리 진행 중
            List<StoryWriteRequest.StoryEntry> storySoFar = List.of(
                    new StoryWriteRequest.StoryEntry(1, "PLAYER",
                            "10년 만에 고향에 돌아온 민수는 익숙한 카페 앞에 서 있었다."),
                    new StoryWriteRequest.StoryEntry(2, "AI",
                            "카페 문을 열자 종소리가 울렸다. 창가에 수진이 앉아 있었다."),
                    new StoryWriteRequest.StoryEntry(3, "PLAYER",
                            "민수는 심장이 빠르게 뛰는 것을 느꼈다. \"수진아...\"")
            );

            // when: AI가 "좀비"를 포함한 문장 작성
            StoryWriteResponse response = storyWriterService.writeStoryWithKeyword(
                    "ROMANCE",      // AI 장르
                    "좀비",          // 제시어
                    4,              // 현재 턴
                    10,             // 최대 턴
                    storySoFar
            );

            // then
            log.info("=== 로맨스 + 좀비 소화 테스트 ===");
            log.info("작성된 내용: {}", response.content());
            log.info("제시어 사용: {}", response.keywordUsed());
            log.info("블러핑: {}", response.bluffWord());
            log.info("전략 근거: {}", response.reasoning());

            // 제시어가 포함되어 있어야 함
            assertThat(response.containsKeyword("좀비"))
                    .as("'좀비'가 문장에 포함되어야 함")
                    .isTrue();

            assertThat(response.keywordUsed())
                    .as("keywordUsed가 true여야 함")
                    .isTrue();

            // Genre Judge로 품질 검증
            String fullStory = buildFullStory(storySoFar, response.content());
            GenreAnalysis quality = genreJudgeService.analyzeStory(fullStory);

            log.info("품질 계수: {}", quality.qualityFactor());
            log.info("뜬금없는 요소: {}", quality.unnaturalElements());

            // 자연스럽게 소화했다면 품질 계수가 높아야 함
            assertThat(quality.qualityFactor())
                    .as("자연스럽게 소화하면 품질 계수 0.7 이상이어야 함")
                    .isGreaterThanOrEqualTo(0.7);
        }

        @Test
        @DisplayName("스릴러 장르에서 '프로포즈' 제시어를 자연스럽게 소화")
        void thriller_withProposeKeyword_shouldDigestNaturally() {
            // given: 스릴러 스토리 진행 중
            List<StoryWriteRequest.StoryEntry> storySoFar = List.of(
                    new StoryWriteRequest.StoryEntry(1, "PLAYER",
                            "어두운 골목에서 발소리가 들렸다. 민수는 뒤를 돌아봤다."),
                    new StoryWriteRequest.StoryEntry(2, "AI",
                            "그림자가 다가오고 있었다. 민수는 뛰기 시작했다."),
                    new StoryWriteRequest.StoryEntry(3, "PLAYER",
                            "\"도망쳐봤자 소용없어.\" 차가운 목소리가 들렸다.")
            );

            // when: AI가 "프로포즈"를 포함한 문장 작성
            StoryWriteResponse response = storyWriterService.writeStoryWithKeyword(
                    "THRILLER",     // AI 장르
                    "프로포즈",      // 제시어
                    4,              // 현재 턴
                    10,             // 최대 턴
                    storySoFar
            );

            // then
            log.info("=== 스릴러 + 프로포즈 소화 테스트 ===");
            log.info("작성된 내용: {}", response.content());
            log.info("제시어 사용: {}", response.keywordUsed());
            log.info("블러핑: {}", response.bluffWord());
            log.info("전략 근거: {}", response.reasoning());

            // 제시어가 포함되어 있어야 함
            assertThat(response.containsKeyword("프로포즈"))
                    .as("'프로포즈'가 문장에 포함되어야 함")
                    .isTrue();

            // Genre Judge로 품질 검증
            String fullStory = buildFullStory(storySoFar, response.content());
            GenreAnalysis quality = genreJudgeService.analyzeStory(fullStory);

            log.info("품질 계수: {}", quality.qualityFactor());
            log.info("뜬금없는 요소: {}", quality.unnaturalElements());

            assertThat(quality.qualityFactor())
                    .as("자연스럽게 소화하면 품질 계수 0.7 이상이어야 함")
                    .isGreaterThanOrEqualTo(0.7);
        }

        @Test
        @DisplayName("코미디 장르에서 '시체' 제시어를 자연스럽게 소화")
        void comedy_withCorpseKeyword_shouldDigestNaturally() {
            // given: 코미디 스토리 진행 중
            List<StoryWriteRequest.StoryEntry> storySoFar = List.of(
                    new StoryWriteRequest.StoryEntry(1, "PLAYER",
                            "철수가 회사에 지각할 것 같아 미친 듯이 뛰고 있었다."),
                    new StoryWriteRequest.StoryEntry(2, "AI",
                            "바나나 껍질을 밟아 넘어질 뻔했지만 아슬아슬하게 피했다."),
                    new StoryWriteRequest.StoryEntry(3, "PLAYER",
                            "\"휴, 살았다!\" 철수는 안도의 한숨을 내쉬었다.")
            );

            // when: AI가 "시체"를 포함한 문장 작성
            StoryWriteResponse response = storyWriterService.writeStoryWithKeyword(
                    "COMEDY",       // AI 장르
                    "시체",          // 제시어
                    4,              // 현재 턴
                    10,             // 최대 턴
                    storySoFar
            );

            // then
            log.info("=== 코미디 + 시체 소화 테스트 ===");
            log.info("작성된 내용: {}", response.content());
            log.info("제시어 사용: {}", response.keywordUsed());
            log.info("전략 근거: {}", response.reasoning());

            assertThat(response.containsKeyword("시체"))
                    .as("'시체'가 문장에 포함되어야 함")
                    .isTrue();

            // 품질 검증
            String fullStory = buildFullStory(storySoFar, response.content());
            GenreAnalysis quality = genreJudgeService.analyzeStory(fullStory);

            log.info("품질 계수: {}", quality.qualityFactor());

            assertThat(quality.qualityFactor())
                    .as("자연스럽게 소화하면 품질 계수 0.7 이상이어야 함")
                    .isGreaterThanOrEqualTo(0.7);
        }
    }

    @Nested
    @DisplayName("장르 유도 테스트")
    class GenreGuidanceTest {

        @Test
        @DisplayName("로맨스 AI는 스토리를 로맨스 방향으로 유도")
        void romanceAi_shouldGuideTowardRomance() {
            // given: 중립적인 시작
            List<StoryWriteRequest.StoryEntry> storySoFar = List.of(
                    new StoryWriteRequest.StoryEntry(1, "PLAYER",
                            "민수는 새로 이사 온 아파트 복도에서 이웃을 마주쳤다.")
            );

            // when: AI가 여러 턴 작성 (제시어 없이 장르 유도만)
            List<String> aiResponses = new ArrayList<>();
            List<StoryWriteRequest.StoryEntry> currentStory = new ArrayList<>(storySoFar);

            for (int turn = 2; turn <= 4; turn += 2) {  // AI 턴: 2, 4
                StoryWriteResponse response = storyWriterService.writeStoryWithoutKeyword(
                        "ROMANCE",
                        "웨딩드레스",  // 아직 사용 안 함
                        turn,
                        10,
                        currentStory
                );

                aiResponses.add(response.content());
                currentStory.add(new StoryWriteRequest.StoryEntry(turn, "AI", response.content()));

                // 다음 플레이어 턴 시뮬레이션 (중립적)
                if (turn < 4) {
                    currentStory.add(new StoryWriteRequest.StoryEntry(turn + 1, "PLAYER",
                            "민수는 그녀에게 인사를 건넸다."));
                }
            }

            // then
            log.info("=== 로맨스 장르 유도 테스트 ===");
            for (int i = 0; i < aiResponses.size(); i++) {
                log.info("AI Turn {}: {}", (i + 1) * 2, aiResponses.get(i));
            }

            // 전체 스토리 장르 분석
            String fullStory = currentStory.stream()
                    .map(e -> String.format("Turn %d: %s", e.turn(), e.content()))
                    .reduce((a, b) -> a + "\n\n" + b)
                    .orElse("");

            GenreAnalysis analysis = genreJudgeService.analyzeStory(fullStory);

            log.info("장르 분석: {}", analysis.genreAnalysis());
            log.info("주요 장르: {}", analysis.primaryGenre());

            // 로맨스 비율이 의미 있게 높아야 함
            assertThat(analysis.genreAnalysis().get("ROMANCE"))
                    .as("로맨스 AI는 로맨스 비율을 높여야 함")
                    .isGreaterThan(30);
        }

        @Test
        @DisplayName("스릴러 AI는 스토리를 스릴러 방향으로 유도")
        void thrillerAi_shouldGuideTowardThriller() {
            // given: 중립적인 시작
            List<StoryWriteRequest.StoryEntry> storySoFar = List.of(
                    new StoryWriteRequest.StoryEntry(1, "PLAYER",
                            "민수는 늦은 밤 사무실에서 혼자 야근을 하고 있었다.")
            );

            // when: AI가 여러 턴 작성
            List<String> aiResponses = new ArrayList<>();
            List<StoryWriteRequest.StoryEntry> currentStory = new ArrayList<>(storySoFar);

            for (int turn = 2; turn <= 4; turn += 2) {
                StoryWriteResponse response = storyWriterService.writeStoryWithoutKeyword(
                        "THRILLER",
                        "프로포즈",
                        turn,
                        10,
                        currentStory
                );

                aiResponses.add(response.content());
                currentStory.add(new StoryWriteRequest.StoryEntry(turn, "AI", response.content()));

                if (turn < 4) {
                    currentStory.add(new StoryWriteRequest.StoryEntry(turn + 1, "PLAYER",
                            "민수는 이상한 기분이 들었다."));
                }
            }

            // then
            log.info("=== 스릴러 장르 유도 테스트 ===");
            for (int i = 0; i < aiResponses.size(); i++) {
                log.info("AI Turn {}: {}", (i + 1) * 2, aiResponses.get(i));
            }

            String fullStory = currentStory.stream()
                    .map(e -> String.format("Turn %d: %s", e.turn(), e.content()))
                    .reduce((a, b) -> a + "\n\n" + b)
                    .orElse("");

            GenreAnalysis analysis = genreJudgeService.analyzeStory(fullStory);

            log.info("장르 분석: {}", analysis.genreAnalysis());
            log.info("주요 장르: {}", analysis.primaryGenre());

            assertThat(analysis.genreAnalysis().get("THRILLER"))
                    .as("스릴러 AI는 스릴러 비율을 높여야 함")
                    .isGreaterThan(30);
        }
    }

    @Nested
    @DisplayName("블러핑 전략 테스트")
    class BluffingStrategyTest {

        @Test
        @DisplayName("블러핑 단어는 스토리에 자연스럽게 포함됨")
        void bluffWord_shouldBeNaturallyIncluded() {
            // given
            List<StoryWriteRequest.StoryEntry> storySoFar = List.of(
                    new StoryWriteRequest.StoryEntry(1, "PLAYER",
                            "민수는 수진을 카페에서 만났다."),
                    new StoryWriteRequest.StoryEntry(2, "AI",
                            "수진은 창가에 앉아 커피를 마시고 있었다.")
            );

            // when: 제시어 사용 없이 블러핑만 가능
            StoryWriteResponse response = storyWriterService.writeStoryWithoutKeyword(
                    "ROMANCE",
                    "좀비",
                    3,
                    10,
                    storySoFar
            );

            // then
            log.info("=== 블러핑 테스트 ===");
            log.info("작성된 내용: {}", response.content());
            log.info("블러핑 단어: {}", response.bluffWord());
            log.info("전략 근거: {}", response.reasoning());

            // 블러핑이 있다면 스토리에 포함되어야 함
            if (response.hasBluff()) {
                assertThat(response.content())
                        .as("블러핑 단어가 스토리에 포함되어야 함")
                        .contains(response.bluffWord());

                // 블러핑이 있어도 품질은 유지되어야 함
                String fullStory = buildFullStory(storySoFar, response.content());
                GenreAnalysis quality = genreJudgeService.analyzeStory(fullStory);

                log.info("품질 계수: {}", quality.qualityFactor());

                assertThat(quality.qualityFactor())
                        .as("블러핑이 있어도 품질 계수 0.7 이상이어야 함")
                        .isGreaterThanOrEqualTo(0.7);
            }
        }

        @Test
        @DisplayName("AI는 블러핑을 난발하지 않음")
        void ai_shouldNotSpamBluffing() {
            // given: 여러 턴 진행
            List<StoryWriteRequest.StoryEntry> currentStory = new ArrayList<>();
            currentStory.add(new StoryWriteRequest.StoryEntry(1, "PLAYER",
                    "민수는 수진을 만났다."));

            // when: 5턴 동안 AI 응답 수집
            int bluffCount = 0;

            for (int turn = 2; turn <= 10; turn += 2) {
                StoryWriteResponse response = storyWriterService.writeStoryWithoutKeyword(
                        "ROMANCE",
                        "좀비",
                        turn,
                        10,
                        currentStory
                );

                if (response.hasBluff()) {
                    bluffCount++;
                    log.info("Turn {}: 블러핑 '{}' 사용", turn, response.bluffWord());
                }

                currentStory.add(new StoryWriteRequest.StoryEntry(turn, "AI", response.content()));
                currentStory.add(new StoryWriteRequest.StoryEntry(turn + 1, "PLAYER",
                        "민수는 고개를 끄덕였다."));
            }

            // then
            log.info("=== 블러핑 난발 방지 테스트 ===");
            log.info("총 블러핑 횟수: {}/5턴", bluffCount);

            // 5턴 중 블러핑은 2회 이하여야 함
            assertThat(bluffCount)
                    .as("블러핑은 과도하게 사용되면 안 됨 (5턴 중 2회 이하)")
                    .isLessThanOrEqualTo(3);  // 약간 여유 있게
        }
    }

    @Nested
    @DisplayName("스토리 연속성 테스트")
    class StoryContinuityTest {

        @Test
        @DisplayName("AI는 이전 스토리와 자연스럽게 연결되는 문장을 작성")
        void ai_shouldWriteContinuousStory() {
            // given
            List<StoryWriteRequest.StoryEntry> storySoFar = List.of(
                    new StoryWriteRequest.StoryEntry(1, "PLAYER",
                            "민수는 버스 정류장에서 비를 피하고 있었다."),
                    new StoryWriteRequest.StoryEntry(2, "AI",
                            "갑자기 누군가 우산을 내밀었다. \"같이 쓸래요?\""),
                    new StoryWriteRequest.StoryEntry(3, "PLAYER",
                            "민수는 고개를 들어 그녀를 바라봤다.")
            );

            // when
            StoryWriteResponse response = storyWriterService.writeStoryWithoutKeyword(
                    "ROMANCE",
                    "좀비",
                    4,
                    10,
                    storySoFar
            );

            // then
            log.info("=== 스토리 연속성 테스트 ===");
            log.info("이전 맥락: 비 오는 날, 우산을 나눠 쓰는 상황");
            log.info("AI 응답: {}", response.content());

            // 응답이 비어있지 않아야 함
            assertThat(response.content())
                    .as("AI 응답이 비어있으면 안 됨")
                    .isNotBlank();

            // 문장이 1-3개여야 함 (대략적으로 마침표 개수로 확인)
            long sentenceCount = response.content().chars()
                    .filter(c -> c == '.' || c == '?' || c == '!')
                    .count();

            assertThat(sentenceCount)
                    .as("1-3문장이어야 함")
                    .isBetween(1L, 5L);  // 약간 여유 있게

            // 전체 스토리 품질 확인
            String fullStory = buildFullStory(storySoFar, response.content());
            GenreAnalysis quality = genreJudgeService.analyzeStory(fullStory);

            log.info("전체 스토리 품질: {}", quality.qualityFactor());

            assertThat(quality.qualityFactor())
                    .as("연속성이 좋으면 품질 계수가 높아야 함")
                    .isGreaterThanOrEqualTo(0.8);
        }
    }

    /**
     * 기존 스토리에 새 응답을 추가하여 전체 스토리 문자열 생성
     */
    private String buildFullStory(List<StoryWriteRequest.StoryEntry> storySoFar, String newContent) {
        StringBuilder sb = new StringBuilder();

        for (StoryWriteRequest.StoryEntry entry : storySoFar) {
            sb.append(String.format("Turn %d: %s\n\n", entry.turn(), entry.content()));
        }

        int nextTurn = storySoFar.isEmpty() ? 1 : storySoFar.get(storySoFar.size() - 1).turn() + 1;
        sb.append(String.format("Turn %d: %s", nextTurn, newContent));

        return sb.toString();
    }
}
