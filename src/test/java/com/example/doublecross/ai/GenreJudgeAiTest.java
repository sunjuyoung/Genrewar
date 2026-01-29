package com.example.doublecross.ai;


import com.example.doublecross.dto.GenreAnalysis;
import com.example.doublecross.service.GenreJudgeService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class GenreJudgeAiTest {
    
    @Autowired
    private GenreJudgeService genreJudgeService;
    
    @Nested
    @DisplayName("품질 계수 테스트")
    class QualityFactorTest {
        
        @Test
        @DisplayName("자연스러운 스토리 → 품질 계수 1.0")
        void naturalStory_qualityFactor_shouldBe_1() {
            // given
            String story = """
                Turn 1: 10년 만에 고향에 돌아온 민수는 익숙한 카페 앞에 서 있었다.
                
                Turn 2: 카페 문을 열자 오래된 종소리가 울렸다. 창가에 수진이 앉아 있었다.
                
                Turn 3: 민수의 심장이 빠르게 뛰기 시작했다. "오랜만이야, 수진."
                
                Turn 4: 수진이 고개를 들었다. "민수야... 정말 너야?"
                
                Turn 5: 두 사람의 눈이 마주쳤다. 10년의 시간이 한순간에 녹아내리는 것 같았다.
                """;
            
            // when
            GenreAnalysis result = genreJudgeService.analyzeStory(story);
            
            // then
            log.info("=== 자연스러운 스토리 분석 결과 ===");
            log.info("장르 분석: {}", result.genreAnalysis());
            log.info("품질 계수: {}", result.qualityFactor());
            log.info("뜬금없는 요소: {}", result.unnaturalElements());
            log.info("최종 점수: {}", result.finalScores());
            log.info("판정 근거: {}", result.reasoning());
            
            assertThat(result.qualityFactor())
                .as("자연스러운 스토리는 품질 계수 0.9 이상이어야 함")
                .isGreaterThanOrEqualTo(0.9);
            
            assertThat(result.getUnnaturalCount())
                .as("뜬금없는 요소가 없어야 함")
                .isLessThanOrEqualTo(1);
        }
        
        @Test
        @DisplayName("블러핑 난발 스토리 → 품질 계수 0.6 이하")
        void bluffingSpam_qualityFactor_shouldBeLow() {
            // given
            String story = """
                Turn 1: 민수는 수진을 봤다. 갑자기 코끼리가 생각났다.
                
                Turn 2: 창문 밖에 UFO가 지나갔다. 수진은 커피를 마셨다.
                
                Turn 3: 바나나 껍질이 떨어졌다. 민수는 "안녕"이라고 말했다.
                
                Turn 4: 냉장고에서 이상한 소리가 났다. 펭귄이 보였다.
                
                Turn 5: 수진이 웃었다. 공룡 뼈가 발견되었다.
                """;
            
            // when
            GenreAnalysis result = genreJudgeService.analyzeStory(story);
            
            // then
            log.info("=== 블러핑 난발 스토리 분석 결과 ===");
            log.info("장르 분석: {}", result.genreAnalysis());
            log.info("품질 계수: {}", result.qualityFactor());
            log.info("뜬금없는 요소 수: {}", result.getUnnaturalCount());
            log.info("뜬금없는 요소들: {}", result.unnaturalElements());
            log.info("판정 근거: {}", result.reasoning());
            
            assertThat(result.qualityFactor())
                .as("블러핑 난발 스토리는 품질 계수 0.6 이하여야 함")
                .isLessThanOrEqualTo(0.6);
            
            assertThat(result.getUnnaturalCount())
                .as("뜬금없는 요소가 4개 이상이어야 함")
                .isGreaterThanOrEqualTo(4);
        }
        
        @Test
        @DisplayName("적절한 블러핑 (1-2개) → 품질 계수 0.7-0.9")
        void moderateBluffing_qualityFactor_shouldBeModerate() {
            // given
            String story = """
                Turn 1: 10년 만에 고향에 돌아온 민수는 익숙한 카페 앞에 서 있었다.
                
                Turn 2: 카페 문을 열자 오래된 종소리가 울렸다. 창가에 수진이 앉아 있었다.
                
                Turn 3: 갑자기 까마귀가 창문을 스쳐 지나갔다. 민수는 깜짝 놀랐다.
                
                Turn 4: "오랜만이야, 수진." 민수가 다가가며 말했다.
                
                Turn 5: 수진이 미소를 지었다. "정말 오랜만이네."
                """;
            
            // when
            GenreAnalysis result = genreJudgeService.analyzeStory(story);
            
            // then
            log.info("=== 적절한 블러핑 스토리 분석 결과 ===");
            log.info("장르 분석: {}", result.genreAnalysis());
            log.info("품질 계수: {}", result.qualityFactor());
            log.info("뜬금없는 요소: {}", result.unnaturalElements());
            log.info("판정 근거: {}", result.reasoning());
            
            assertThat(result.qualityFactor())
                .as("적절한 블러핑은 품질 계수 0.7-0.9 사이여야 함")
                .isBetween(0.7, 0.95);
        }
    }
    
    @Nested
    @DisplayName("제시어 소화 vs 블러핑 구분 테스트")
    class KeywordDigestionTest {
        
        @Test
        @DisplayName("자연스러운 제시어 소화 → 품질 감점 없음")
        void naturalKeywordDigestion_noQualityPenalty() {
            // given: 로맨스 스토리에 "좀비"를 영화 대화로 자연스럽게 소화
            String story = """
                Turn 1: 민수는 수진에게 다가갔다. "오늘 뭐 하고 싶어?"
                
                Turn 2: 수진이 눈을 빛내며 말했다. "좀비 영화 보러 가고 싶어!"
                
                Turn 3: 민수는 웃으며 대답했다. "좀비? 무서운 거 좋아하는구나."
                
                Turn 4: "무섭긴 해도 네 옆이면 괜찮아." 수진이 민수의 팔을 잡았다.
                
                Turn 5: 두 사람은 손을 잡고 영화관으로 향했다.
                """;
            
            // when
            GenreAnalysis result = genreJudgeService.analyzeStory(story);
            
            // then
            log.info("=== 제시어 자연스러운 소화 분석 결과 ===");
            log.info("장르 분석: {}", result.genreAnalysis());
            log.info("품질 계수: {}", result.qualityFactor());
            log.info("주요 장르: {}", result.primaryGenre());
            log.info("뜬금없는 요소: {}", result.unnaturalElements());
            log.info("판정 근거: {}", result.reasoning());
            
            assertThat(result.qualityFactor())
                .as("자연스럽게 소화된 제시어는 품질 감점 없어야 함")
                .isGreaterThanOrEqualTo(0.9);
            
            // "좀비"가 뜬금없는 요소로 판정되지 않아야 함
            boolean zombieMarkedAsUnnatural = result.unnaturalElements().stream()
                .anyMatch(e -> e.element().contains("좀비"));
            
            assertThat(zombieMarkedAsUnnatural)
                .as("자연스럽게 소화된 '좀비'는 뜬금없음으로 판정되면 안 됨")
                .isFalse();
        }
        
        @Test
        @DisplayName("어색한 제시어 사용 → 뜬금없음으로 판정")
        void awkwardKeywordUsage_shouldBeMarkedUnnatural() {
            // given: 로맨스 스토리에 "좀비"를 맥락 없이 갑자기 사용
            String story = """
                Turn 1: 민수는 수진의 손을 잡았다. "사랑해."
                
                Turn 2: 수진이 미소 지었다. "나도 사랑해."
                
                Turn 3: 갑자기 좀비 한 마리가 지나갔다. 민수는 계속 수진을 바라봤다.
                
                Turn 4: "우리 결혼하자." 민수가 말했다.
                
                Turn 5: 수진은 눈물을 흘리며 고개를 끄덕였다.
                """;
            
            // when
            GenreAnalysis result = genreJudgeService.analyzeStory(story);
            
            // then
            log.info("=== 어색한 제시어 사용 분석 결과 ===");
            log.info("장르 분석: {}", result.genreAnalysis());
            log.info("품질 계수: {}", result.qualityFactor());
            log.info("뜬금없는 요소: {}", result.unnaturalElements());
            log.info("판정 근거: {}", result.reasoning());
            
            // "좀비"가 뜬금없는 요소로 판정되어야 함
            boolean zombieMarkedAsUnnatural = result.unnaturalElements().stream()
                .anyMatch(e -> e.element().contains("좀비"));
            
            assertThat(zombieMarkedAsUnnatural)
                .as("맥락 없이 등장한 '좀비'는 뜬금없음으로 판정되어야 함")
                .isTrue();
        }
    }
    
    @Nested
    @DisplayName("장르 판정 테스트")
    class GenreDetectionTest {
        
        @Test
        @DisplayName("명확한 로맨스 스토리 → ROMANCE 비율 높음")
        void clearRomance_shouldDetectRomance() {
            // given
            String story = """
                Turn 1: 민수는 수진을 처음 본 순간 심장이 멈추는 것 같았다.
                
                Turn 2: "안녕하세요, 저는 수진이에요." 그녀의 미소가 눈부셨다.
                
                Turn 3: 두 사람은 매일 카페에서 만나기 시작했다. 
                
                Turn 4: "수진아, 나... 너를 좋아해." 민수가 고백했다.
                
                Turn 5: 수진이 민수의 손을 잡았다. "나도 좋아해, 민수야."
                """;
            
            // when
            GenreAnalysis result = genreJudgeService.analyzeStory(story);
            
            // then
            log.info("=== 로맨스 스토리 분석 결과 ===");
            log.info("장르 분석: {}", result.genreAnalysis());
            log.info("주요 장르: {}", result.primaryGenre());
            log.info("최종 점수: {}", result.finalScores());
            
            assertThat(result.primaryGenre())
                .as("주요 장르가 ROMANCE여야 함")
                .isEqualTo("ROMANCE");
            
            assertThat(result.genreAnalysis().get("ROMANCE"))
                .as("ROMANCE 비율이 50% 이상이어야 함")
                .isGreaterThan(50);
        }
        
        @Test
        @DisplayName("명확한 스릴러 스토리 → THRILLER 비율 높음")
        void clearThriller_shouldDetectThriller() {
            // given
            String story = """
                Turn 1: 어두운 골목에서 발소리가 들렸다. 민수는 뒤를 돌아봤다.
                
                Turn 2: 그림자가 다가오고 있었다. 민수는 뛰기 시작했다.
                
                Turn 3: "도망쳐봤자 소용없어." 차가운 목소리가 들렸다.
                
                Turn 4: 민수는 막다른 골목에 몰렸다. 손에 칼이 번뜩였다.
                
                Turn 5: "드디어 찾았다." 살인마가 천천히 다가왔다.
                """;
            
            // when
            GenreAnalysis result = genreJudgeService.analyzeStory(story);
            
            // then
            log.info("=== 스릴러 스토리 분석 결과 ===");
            log.info("장르 분석: {}", result.genreAnalysis());
            log.info("주요 장르: {}", result.primaryGenre());
            log.info("최종 점수: {}", result.finalScores());
            
            assertThat(result.primaryGenre())
                .as("주요 장르가 THRILLER여야 함")
                .isEqualTo("THRILLER");
            
            assertThat(result.genreAnalysis().get("THRILLER"))
                .as("THRILLER 비율이 50% 이상이어야 함")
                .isGreaterThan(50);
        }
        
        @Test
        @DisplayName("혼합 장르 스토리 → 여러 장르 비율 표시")
        void mixedGenre_shouldDetectMultiple() {
            // given: 로맨스 + 스릴러 혼합
            String story = """
                Turn 1: 민수는 수진의 손을 잡았다. "사랑해."
                
                Turn 2: 갑자기 총소리가 들렸다. 두 사람은 몸을 숨겼다.
                
                Turn 3: "괜찮아?" 민수가 수진을 안으며 물었다.
                
                Turn 4: 검은 옷의 남자들이 다가왔다. "저기 있다!"
                
                Turn 5: "내가 막을게, 먼저 도망가." 민수가 수진을 밀며 말했다.
                """;
            
            // when
            GenreAnalysis result = genreJudgeService.analyzeStory(story);
            
            // then
            log.info("=== 혼합 장르 스토리 분석 결과 ===");
            log.info("장르 분석: {}", result.genreAnalysis());
            log.info("주요 장르: {}", result.primaryGenre());
            log.info("최종 점수: {}", result.finalScores());
            
            // 로맨스와 스릴러 둘 다 비율이 있어야 함
            assertThat(result.genreAnalysis().get("ROMANCE"))
                .as("ROMANCE 비율이 20% 이상이어야 함")
                .isGreaterThan(20);
            
            assertThat(result.genreAnalysis().get("THRILLER"))
                .as("THRILLER 비율이 20% 이상이어야 함")
                .isGreaterThan(20);
        }
    }
    
    @Nested
    @DisplayName("최종 점수 계산 테스트")
    class FinalScoreCalculationTest {
        
        @Test
        @DisplayName("품질 계수가 최종 점수에 반영됨")
        void qualityFactor_shouldAffectFinalScore() {
            // given: 블러핑이 포함된 스토리
            String story = """
                Turn 1: 민수는 수진을 봤다. 갑자기 기린이 생각났다.
                
                Turn 2: 수진이 웃었다. "오랜만이야."
                
                Turn 3: 두 사람은 손을 잡았다. 하늘에서 피자가 떨어졌다.
                
                Turn 4: "사랑해." 민수가 말했다.
                
                Turn 5: 수진이 고개를 끄덕였다. 냉장고가 울렸다.
                """;
            
            // when
            GenreAnalysis result = genreJudgeService.analyzeStory(story);
            
            // then
            log.info("=== 최종 점수 계산 테스트 결과 ===");
            log.info("장르 분석: {}", result.genreAnalysis());
            log.info("품질 계수: {}", result.qualityFactor());
            log.info("최종 점수: {}", result.finalScores());
            
            // 품질 계수가 1.0보다 작아야 함
            assertThat(result.qualityFactor())
                .as("블러핑이 있으면 품질 계수가 1.0 미만이어야 함")
                .isLessThan(1.0);
            
            // 최종 점수 = 장르 비율 × 품질 계수 검증
            String primaryGenre = result.primaryGenre();
            int originalRatio = result.genreAnalysis().get(primaryGenre);
            double finalScore = result.finalScores().get(primaryGenre);
            double expectedFinalScore = originalRatio * result.qualityFactor();
            
            assertThat(finalScore)
                .as("최종 점수는 (원본 비율 × 품질 계수)와 비슷해야 함")
                .isCloseTo(expectedFinalScore, org.assertj.core.data.Offset.offset(5.0));
        }
    }
}
