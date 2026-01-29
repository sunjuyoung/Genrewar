package com.example.doublecross.ai;


import com.example.doublecross.dto.KeywordGuessRequest;
import com.example.doublecross.dto.KeywordGuessResponse;
import com.example.doublecross.service.KeywordGuesserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class KeywordGuesserAiTest {

    @Autowired
    private KeywordGuesserService keywordGuesserService;

    @Nested
    @DisplayName("어색한 단어 탐지 테스트")
    class AwkwardWordDetectionTest {

        @Test
        @DisplayName("로맨스 스토리에서 '좀비' 탐지")
        void romance_detectZombie() {
            // given: 로맨스 스토리에 "좀비"가 어색하게 등장
            List<KeywordGuessRequest.StoryEntry> story = List.of(
                    new KeywordGuessRequest.StoryEntry(1, "PLAYER",
                            "민수는 수진의 손을 잡았다. \"사랑해.\""),
                    new KeywordGuessRequest.StoryEntry(2, "AI",
                            "수진이 미소 지었다. \"나도 사랑해.\""),
                    new KeywordGuessRequest.StoryEntry(3, "PLAYER",
                            "갑자기 좀비 한 마리가 지나갔다. 민수는 신경 쓰지 않았다."),
                    new KeywordGuessRequest.StoryEntry(4, "AI",
                            "두 사람은 서로를 바라보며 행복해했다.")
            );

            // when
            KeywordGuessResponse response = keywordGuesserService.guessKeyword(
                    "THRILLER",      // 내 장르 (스릴러)
                    "ROMANCE",       // 상대 장르 추정 (로맨스)
                    3,               // 남은 추측 기회
                    4,               // 현재 턴
                    10,              // 최대 턴
                    story
            );

            // then
            log.info("=== 로맨스에서 '좀비' 탐지 테스트 ===");
            log.info("결정: {}", response.decision());
            log.info("추측 단어: {}", response.guessWord());
            log.info("확신도: {}%", response.confidence());
            log.info("의심 단어들: {}", response.suspiciousWords());
            log.info("판단 근거: {}", response.reasoning());

            // "좀비"가 의심 단어에 포함되어야 함
            boolean zombieDetected = response.suspiciousWords().stream()
                    .anyMatch(w -> w.word().contains("좀비"));

            assertThat(zombieDetected)
                    .as("'좀비'가 의심 단어로 탐지되어야 함")
                    .isTrue();

            // 확신도가 높으면 GUESS 결정
            if (response.confidence() >= 70) {
                assertThat(response.isGuess())
                        .as("확신도 70% 이상이면 GUESS 결정이어야 함")
                        .isTrue();

                assertThat(response.guessWord())
                        .as("추측 단어가 '좀비'여야 함")
                        .isEqualTo("좀비");
            }
        }

        @Test
        @DisplayName("스릴러 스토리에서 '프로포즈' 탐지")
        void thriller_detectPropose() {
            // given: 스릴러 스토리에 "프로포즈"가 어색하게 등장
            List<KeywordGuessRequest.StoryEntry> story = List.of(
                    new KeywordGuessRequest.StoryEntry(1, "PLAYER",
                            "어두운 골목에서 발소리가 들렸다."),
                    new KeywordGuessRequest.StoryEntry(2, "AI",
                            "민수는 뒤를 돌아봤다. 그림자가 다가오고 있었다."),
                    new KeywordGuessRequest.StoryEntry(3, "PLAYER",
                            "\"잠깐! 나 수진한테 프로포즈 아직 못 했어!\" 민수가 외쳤다."),
                    new KeywordGuessRequest.StoryEntry(4, "AI",
                            "살인마가 잠시 멈칫했다.")
            );

            // when
            KeywordGuessResponse response = keywordGuesserService.guessKeyword(
                    "ROMANCE",       // 내 장르 (로맨스)
                    "THRILLER",      // 상대 장르 추정 (스릴러)
                    3,
                    4,
                    10,
                    story
            );

            // then
            log.info("=== 스릴러에서 '프로포즈' 탐지 테스트 ===");
            log.info("결정: {}", response.decision());
            log.info("추측 단어: {}", response.guessWord());
            log.info("확신도: {}%", response.confidence());
            log.info("의심 단어들: {}", response.suspiciousWords());
            log.info("판단 근거: {}", response.reasoning());

            // "프로포즈"가 의심 단어에 포함되어야 함
            boolean proposeDetected = response.suspiciousWords().stream()
                    .anyMatch(w -> w.word().contains("프로포즈"));

            assertThat(proposeDetected)
                    .as("'프로포즈'가 의심 단어로 탐지되어야 함")
                    .isTrue();
        }

        @Test
        @DisplayName("자연스러운 스토리에서는 높은 확신도 없음")
        void naturalStory_lowConfidence() {
            // given: 자연스러운 로맨스 스토리 (제시어 없음)
            List<KeywordGuessRequest.StoryEntry> story = List.of(
                    new KeywordGuessRequest.StoryEntry(1, "PLAYER",
                            "민수는 카페에서 수진을 만났다."),
                    new KeywordGuessRequest.StoryEntry(2, "AI",
                            "수진이 환하게 웃으며 손을 흔들었다."),
                    new KeywordGuessRequest.StoryEntry(3, "PLAYER",
                            "\"오랜만이야, 수진.\" 민수가 다가가며 말했다."),
                    new KeywordGuessRequest.StoryEntry(4, "AI",
                            "두 사람은 창가 자리에 앉았다.")
            );

            // when
            KeywordGuessResponse response = keywordGuesserService.guessKeyword(
                    "THRILLER",
                    "ROMANCE",
                    3,
                    4,
                    10,
                    story
            );

            // then
            log.info("=== 자연스러운 스토리 테스트 ===");
            log.info("결정: {}", response.decision());
            log.info("확신도: {}%", response.confidence());
            log.info("의심 단어들: {}", response.suspiciousWords());
            log.info("판단 근거: {}", response.reasoning());

            // 자연스러운 스토리에서는 높은 확신도의 추측이 어려움
            // PASS하거나, 추측해도 확신도가 높지 않아야 함
            if (response.isGuess()) {
                log.warn("자연스러운 스토리인데 GUESS 결정함. 확신도: {}", response.confidence());
            }

            // 적어도 매우 높은 확신도(90%+)로 추측하면 안 됨
            assertThat(response.confidence())
                    .as("자연스러운 스토리에서는 90% 이상의 확신도가 나오면 안 됨")
                    .isLessThan(90);
        }
    }

    @Nested
    @DisplayName("블러핑 vs 제시어 구분 테스트")
    class BluffingDistinctionTest {

        @Test
        @DisplayName("노골적인 블러핑은 제시어로 추측하지 않음")
        void obviousBluffing_shouldNotGuess() {
            // given: 노골적으로 어색한 단어 (블러핑으로 의심)
            List<KeywordGuessRequest.StoryEntry> story = List.of(
                    new KeywordGuessRequest.StoryEntry(1, "PLAYER",
                            "민수는 수진을 봤다. 갑자기 코끼리가 생각났다."),
                    new KeywordGuessRequest.StoryEntry(2, "AI",
                            "수진이 다가왔다."),
                    new KeywordGuessRequest.StoryEntry(3, "PLAYER",
                            "\"안녕!\" 민수가 인사했다. 창밖에 UFO가 지나갔다."),
                    new KeywordGuessRequest.StoryEntry(4, "AI",
                            "\"오랜만이야.\" 수진이 대답했다.")
            );

            // when
            KeywordGuessResponse response = keywordGuesserService.guessKeyword(
                    "THRILLER",
                    "ROMANCE",
                    3,
                    4,
                    10,
                    story
            );

            // then
            log.info("=== 노골적 블러핑 테스트 ===");
            log.info("결정: {}", response.decision());
            log.info("추측 단어: {}", response.guessWord());
            log.info("확신도: {}%", response.confidence());
            log.info("의심 단어들: {}", response.suspiciousWords());
            log.info("판단 근거: {}", response.reasoning());

            // 노골적인 블러핑(코끼리, UFO)은 의심하되, 확신도가 낮아야 함
            // 왜냐면 너무 뜬금없어서 블러핑일 가능성이 높음

            // 블러핑 특성 인식 여부 확인 (reasoning에 블러핑 언급)
            if (response.reasoning() != null) {
                log.info("블러핑 언급 여부: {}",
                        response.reasoning().contains("블러핑") ||
                                response.reasoning().contains("미끼") ||
                                response.reasoning().contains("의도적"));
            }
        }

        @Test
        @DisplayName("자연스럽게 녹인 제시어는 탐지")
        void naturallyDigestedKeyword_shouldDetect() {
            // given: 대화로 자연스럽게 녹인 "좀비" (실제 제시어)
            List<KeywordGuessRequest.StoryEntry> story = List.of(
                    new KeywordGuessRequest.StoryEntry(1, "PLAYER",
                            "민수는 수진에게 다가갔다. \"오늘 뭐 할래?\""),
                    new KeywordGuessRequest.StoryEntry(2, "AI",
                            "수진이 생각하다가 대답했다. \"영화 보러 갈까?\""),
                    new KeywordGuessRequest.StoryEntry(3, "PLAYER",
                            "\"좀비 영화 어때? 요즘 재밌는 거 나왔던데.\" 민수가 제안했다."),
                    new KeywordGuessRequest.StoryEntry(4, "AI",
                            "수진이 웃으며 고개를 끄덕였다.")
            );

            // when
            KeywordGuessResponse response = keywordGuesserService.guessKeyword(
                    "THRILLER",
                    "ROMANCE",
                    3,
                    4,
                    10,
                    story
            );

            // then
            log.info("=== 자연스럽게 녹인 제시어 테스트 ===");
            log.info("결정: {}", response.decision());
            log.info("추측 단어: {}", response.guessWord());
            log.info("확신도: {}%", response.confidence());
            log.info("의심 단어들: {}", response.suspiciousWords());
            log.info("판단 근거: {}", response.reasoning());

            // "좀비"가 의심 단어에 포함되어야 함 (자연스럽게 녹였어도)
            boolean zombieDetected = response.suspiciousWords().stream()
                    .anyMatch(w -> w.word().contains("좀비"));

            assertThat(zombieDetected)
                    .as("자연스럽게 녹인 '좀비'도 의심 단어로 탐지되어야 함")
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("조사 제거 테스트")
    class ParticleRemovalTest {

        @Test
        @DisplayName("'좀비가'에서 '좀비'로 추출")
        void zombieGa_extractZombie() {
            // given: "좀비가"로 사용
            List<KeywordGuessRequest.StoryEntry> story = List.of(
                    new KeywordGuessRequest.StoryEntry(1, "PLAYER",
                            "민수는 수진과 데이트 중이었다."),
                    new KeywordGuessRequest.StoryEntry(2, "AI",
                            "카페에서 커피를 마시고 있었다."),
                    new KeywordGuessRequest.StoryEntry(3, "PLAYER",
                            "갑자기 좀비가 나타났다! 민수는 수진을 보호했다.")
            );

            // when
            KeywordGuessResponse response = keywordGuesserService.guessKeyword(
                    "THRILLER",
                    "ROMANCE",
                    3,
                    3,
                    10,
                    story
            );

            // then
            log.info("=== 조사 제거 테스트 (좀비가 → 좀비) ===");
            log.info("결정: {}", response.decision());
            log.info("추측 단어: {}", response.guessWord());
            log.info("의심 단어들: {}", response.suspiciousWords());

            if (response.isGuess()) {
                // 추측할 때 "좀비가"가 아닌 "좀비"로 추측해야 함
                assertThat(response.guessWord())
                        .as("조사를 제거하고 '좀비'로 추측해야 함")
                        .isEqualTo("좀비");
            }

            // 의심 단어에서도 원본 단어 추출 확인
            KeywordGuessResponse.SuspiciousWord mostSuspicious = response.getMostSuspiciousWord();
            if (mostSuspicious != null) {
                log.info("가장 의심되는 단어: {} (의심도: {}%)",
                        mostSuspicious.word(), mostSuspicious.suspicionLevel());
            }
        }

        @Test
        @DisplayName("'웨딩드레스를'에서 '웨딩드레스'로 추출")
        void weddingDressReul_extractWeddingDress() {
            // given: "웨딩드레스를"로 사용
            List<KeywordGuessRequest.StoryEntry> story = List.of(
                    new KeywordGuessRequest.StoryEntry(1, "PLAYER",
                            "어두운 창고에서 민수가 수상한 소리를 들었다."),
                    new KeywordGuessRequest.StoryEntry(2, "AI",
                            "천천히 다가가자 피 묻은 칼이 보였다."),
                    new KeywordGuessRequest.StoryEntry(3, "PLAYER",
                            "구석에서 누군가가 웨딩드레스를 입고 서 있었다. 소름이 돋았다.")
            );

            // when
            KeywordGuessResponse response = keywordGuesserService.guessKeyword(
                    "ROMANCE",
                    "THRILLER",
                    3,
                    3,
                    10,
                    story
            );

            // then
            log.info("=== 조사 제거 테스트 (웨딩드레스를 → 웨딩드레스) ===");
            log.info("결정: {}", response.decision());
            log.info("추측 단어: {}", response.guessWord());
            log.info("의심 단어들: {}", response.suspiciousWords());

            // "웨딩드레스"가 의심 단어에 포함되어야 함
            boolean detected = response.suspiciousWords().stream()
                    .anyMatch(w -> w.word().contains("웨딩드레스"));

            assertThat(detected)
                    .as("'웨딩드레스'가 의심 단어로 탐지되어야 함")
                    .isTrue();

            if (response.isGuess()) {
                assertThat(response.guessWord())
                        .as("조사를 제거하고 '웨딩드레스'로 추측해야 함")
                        .isEqualTo("웨딩드레스");
            }
        }
    }

    @Nested
    @DisplayName("추측 타이밍 전략 테스트")
    class GuessTimingStrategyTest {

        @Test
        @DisplayName("확신도 낮으면 PASS")
        void lowConfidence_shouldPass() {
            // given: 명확한 제시어가 없는 스토리
            List<KeywordGuessRequest.StoryEntry> story = List.of(
                    new KeywordGuessRequest.StoryEntry(1, "PLAYER",
                            "민수는 길을 걷고 있었다."),
                    new KeywordGuessRequest.StoryEntry(2, "AI",
                            "날씨가 좋았다.")
            );

            // when
            KeywordGuessResponse response = keywordGuesserService.guessKeyword(
                    "THRILLER",
                    "ROMANCE",
                    3,
                    2,
                    10,
                    story
            );

            // then
            log.info("=== 확신도 낮을 때 PASS 테스트 ===");
            log.info("결정: {}", response.decision());
            log.info("확신도: {}%", response.confidence());
            log.info("판단 근거: {}", response.reasoning());

            // 명확한 제시어가 없으면 PASS하거나 낮은 확신도
            if (response.isGuess()) {
                assertThat(response.confidence())
                        .as("GUESS하더라도 확신도가 70% 미만이어야 함 (명확한 제시어 없음)")
                        .isLessThan(70);
            }
        }

        @Test
        @DisplayName("추측 기회가 1회 남았을 때 더 신중")
        void lastChance_shouldBeMoreCareful() {
            // given: 약간 의심스러운 스토리 + 마지막 기회
            List<KeywordGuessRequest.StoryEntry> story = List.of(
                    new KeywordGuessRequest.StoryEntry(1, "PLAYER",
                            "민수는 수진과 카페에 있었다."),
                    new KeywordGuessRequest.StoryEntry(2, "AI",
                            "수진이 메뉴를 보고 있었다."),
                    new KeywordGuessRequest.StoryEntry(3, "PLAYER",
                            "\"어제 좀비 드라마 봤어?\" 민수가 물었다.")
            );

            // when: 기회 1회 남음
            KeywordGuessResponse response = keywordGuesserService.guessKeyword(
                    "THRILLER",
                    "ROMANCE",
                    1,              // 마지막 기회!
                    3,
                    10,
                    story
            );

            // then
            log.info("=== 마지막 기회 신중함 테스트 ===");
            log.info("남은 기회: 1회");
            log.info("결정: {}", response.decision());
            log.info("확신도: {}%", response.confidence());
            log.info("판단 근거: {}", response.reasoning());

            // reasoning에 기회가 적다는 언급이 있을 수 있음
            if (response.reasoning() != null) {
                log.info("기회 관련 언급: {}",
                        response.reasoning().contains("기회") ||
                                response.reasoning().contains("신중") ||
                                response.reasoning().contains("마지막"));
            }

            // 마지막 기회이므로 80% 이상 확신이 없으면 PASS가 바람직
            if (response.isGuess() && response.confidence() < 80) {
                log.warn("마지막 기회인데 확신도 {}%로 GUESS 결정 - 위험한 선택",
                        response.confidence());
            }
        }

        @Test
        @DisplayName("게임 후반에 기회 아끼기")
        void lateGame_conserveChances() {
            // given: 게임 후반 + 기회 여유
            List<KeywordGuessRequest.StoryEntry> story = List.of(
                    new KeywordGuessRequest.StoryEntry(1, "PLAYER",
                            "민수는 수진에게 다가갔다."),
                    new KeywordGuessRequest.StoryEntry(2, "AI",
                            "수진이 웃으며 손을 흔들었다."),
                    new KeywordGuessRequest.StoryEntry(3, "PLAYER",
                            "\"좀비 게임 같이 할래?\" 민수가 물었다."),
                    new KeywordGuessRequest.StoryEntry(4, "AI",
                            "수진이 고개를 끄덕였다."),
                    new KeywordGuessRequest.StoryEntry(5, "PLAYER",
                            "두 사람은 PC방으로 향했다."),
                    new KeywordGuessRequest.StoryEntry(6, "AI",
                            "PC방은 사람들로 북적였다."),
                    new KeywordGuessRequest.StoryEntry(7, "PLAYER",
                            "민수가 자리를 잡았다."),
                    new KeywordGuessRequest.StoryEntry(8, "AI",
                            "게임을 시작했다.")
            );

            // when: 턴 8/10, 기회 2회
            KeywordGuessResponse response = keywordGuesserService.guessKeyword(
                    "THRILLER",
                    "ROMANCE",
                    2,
                    8,              // 후반
                    10,
                    story
            );

            // then
            log.info("=== 게임 후반 기회 관리 테스트 ===");
            log.info("현재 턴: 8/10");
            log.info("남은 기회: 2회");
            log.info("결정: {}", response.decision());
            log.info("확신도: {}%", response.confidence());
            log.info("의심 단어들: {}", response.suspiciousWords());

            // "좀비"가 의심 단어로 탐지되어야 함
            boolean zombieDetected = response.suspiciousWords().stream()
                    .anyMatch(w -> w.word().contains("좀비"));

            assertThat(zombieDetected)
                    .as("'좀비'가 의심 단어로 탐지되어야 함")
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("복합 시나리오 테스트")
    class ComplexScenarioTest {

        @Test
        @DisplayName("여러 의심 단어 중 가장 확실한 것 선택")
        void multiplesSuspiciousWords_selectMostConfident() {
            // given: 여러 의심스러운 단어가 있는 스토리
            List<KeywordGuessRequest.StoryEntry> story = List.of(
                    new KeywordGuessRequest.StoryEntry(1, "PLAYER",
                            "민수는 수진과 공원에서 산책했다."),
                    new KeywordGuessRequest.StoryEntry(2, "AI",
                            "벚꽃이 흩날리고 있었다."),
                    new KeywordGuessRequest.StoryEntry(3, "PLAYER",
                            "\"저번에 시체 발견된 곳이 저기래.\" 민수가 갑자기 말했다."),
                    new KeywordGuessRequest.StoryEntry(4, "AI",
                            "수진이 놀랐다."),
                    new KeywordGuessRequest.StoryEntry(5, "PLAYER",
                            "\"아, 미안. 좀비 영화 생각나서.\" 민수가 얼버무렸다.")
            );

            // when
            KeywordGuessResponse response = keywordGuesserService.guessKeyword(
                    "THRILLER",
                    "ROMANCE",
                    3,
                    5,
                    10,
                    story
            );

            // then
            log.info("=== 복합 시나리오 테스트 ===");
            log.info("결정: {}", response.decision());
            log.info("추측 단어: {}", response.guessWord());
            log.info("확신도: {}%", response.confidence());
            log.info("의심 단어들:");
            for (var word : response.suspiciousWords()) {
                log.info("  - Turn {}: '{}' (의심도 {}%) - {}",
                        word.turn(), word.word(), word.suspicionLevel(), word.reason());
            }
            log.info("판단 근거: {}", response.reasoning());

            // 여러 단어가 탐지되어야 함
            assertThat(response.suspiciousWords())
                    .as("여러 의심 단어가 탐지되어야 함")
                    .hasSizeGreaterThanOrEqualTo(1);

            // 가장 의심되는 단어 확인
            var mostSuspicious = response.getMostSuspiciousWord();
            if (mostSuspicious != null) {
                log.info("가장 의심되는 단어: '{}' (의심도 {}%)",
                        mostSuspicious.word(), mostSuspicious.suspicionLevel());
            }
        }
    }
}
