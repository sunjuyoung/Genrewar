package com.example.doublecross.service;

import com.example.doublecross.domain.entity.GameSession;
import com.example.doublecross.domain.entity.GuessAttempt;
import com.example.doublecross.domain.entity.Keyword;
import com.example.doublecross.domain.entity.Participant;
import com.example.doublecross.domain.enums.KeywordStatus;
import com.example.doublecross.domain.enums.ParticipantType;
import com.example.doublecross.domain.repository.GuessAttemptRepository;
import com.example.doublecross.domain.repository.ParticipantRepository;
import com.example.doublecross.dto.GuessResult;
import com.example.doublecross.exception.GameException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuessService {

    private final ParticipantRepository participantRepository;
    private final GuessAttemptRepository guessAttemptRepository;
    private final ScoreService scoreService;
    private final KeywordService keywordService;

    /**
     * 추측 처리
     */
    @Transactional
    public GuessResult processGuess(GameSession session, ParticipantType guesserType, String guessWord) {
        UUID sessionId = session.getSessionId();
        int currentTurn = session.getCurrentTurn();

        // 1. 참가자 조회
        Participant guesser = participantRepository
                .findBySessionIdAndTypeWithKeyword(sessionId, guesserType)
                .orElseThrow(() -> new GameException("참가자를 찾을 수 없습니다."));

        ParticipantType opponentType = getOpponentType(guesserType);
        Participant opponent = participantRepository
                .findBySessionIdAndTypeWithKeyword(sessionId, opponentType)
                .orElseThrow(() -> new GameException("상대 참가자를 찾을 수 없습니다."));

        // 2. 추측 기회 확인
        if (guesser.getGuessesRemaining() <= 0) {
            return GuessResult.noChances(guesser.getScore());
        }

        // 3. 상대 제시어 상태 확인
        Keyword opponentKeyword = opponent.getCurrentKeyword();

        if (opponentKeyword == null || opponent.getKeywordStatus() == KeywordStatus.PENDING) {
            // 상대가 아직 제시어를 사용하지 않음 → 무조건 오답 처리
            return processWrongGuess(guesser, currentTurn, guessWord,
                    "상대가 아직 제시어를 사용하지 않았습니다.");
        }

        // 4. 정답 확인
        if (opponentKeyword.getWord().equals(guessWord)) {
            return processCorrectGuess(guesser, opponent, currentTurn, guessWord);
        } else {
            return processWrongGuess(guesser, currentTurn, guessWord, "오답입니다.");
        }
    }

    /**
     * 정답 처리
     */
    private GuessResult processCorrectGuess(Participant guesser, Participant opponent,
                                            int turn, String guessWord) {
        // 점수 +5
        scoreService.addGuessCorrectScore(guesser, turn, guessWord);

        // 상대 제시어 상태 변경
        keywordService.markKeywordCaught(opponent);

        // 상대에게 새 제시어 배정
        Keyword newKeyword = keywordService.assignNewKeyword(
                opponent,
                guesser.getSecretGenre(),  // 상대방에게는 추측자의 장르에서 제시어 배정
                opponent.getCurrentKeyword().getDifficulty()
        );

        // 추측 기록 저장
        saveGuessAttempt(guesser, turn, guessWord, true);

        log.info("Correct guess! {} guessed '{}'. Opponent gets new keyword '{}'",
                guesser.getType(), guessWord, newKeyword.getWord());

        return GuessResult.correct(
                guessWord,
                ScoreService.GUESS_CORRECT_POINTS,
                guesser.getGuessesRemaining(),
                guesser.getScore(),
                "정답! 상대의 제시어는 '" + guessWord + "'였습니다."
        );
    }

    /**
     * 오답 처리
     */
    private GuessResult processWrongGuess(Participant guesser, int turn,
                                          String guessWord, String message) {
        // 점수 -1
        scoreService.addGuessWrongScore(guesser, turn, guessWord);

        // 기회 차감
        guesser.decrementGuesses();

        // 추측 기록 저장
        saveGuessAttempt(guesser, turn, guessWord, false);

        log.info("Wrong guess! {} guessed '{}'. Remaining guesses: {}",
                guesser.getType(), guessWord, guesser.getGuessesRemaining());

        return GuessResult.wrong(
                guessWord,
                ScoreService.GUESS_WRONG_POINTS,
                guesser.getGuessesRemaining(),
                guesser.getScore(),
                message
        );
    }

    /**
     * 추측 기록 저장
     */
    private void saveGuessAttempt(Participant guesser, int turn, String guessWord, boolean isCorrect) {
        GuessAttempt attempt = GuessAttempt.builder()
                .participant(guesser)
                .turn(turn)
                .guessedWord(guessWord)
                .isCorrect(isCorrect)
                .build();

        guessAttemptRepository.save(attempt);
    }

    /**
     * 상대 타입 반환
     */
    private ParticipantType getOpponentType(ParticipantType type) {
        return type == ParticipantType.PLAYER ? ParticipantType.AI : ParticipantType.PLAYER;
    }

    /**
     * 추측 가능 여부 확인
     */
    public boolean canGuess(UUID sessionId, ParticipantType guesserType) {
        return participantRepository.findBySessionIdAndType(sessionId, guesserType)
                .map(p -> p.getGuessesRemaining() > 0)
                .orElse(false);
    }

    /**
     * 남은 추측 기회 조회
     */
    public int getRemainingGuesses(UUID sessionId, ParticipantType guesserType) {
        return participantRepository.findBySessionIdAndType(sessionId, guesserType)
                .map(Participant::getGuessesRemaining)
                .orElse(0);
    }
}
