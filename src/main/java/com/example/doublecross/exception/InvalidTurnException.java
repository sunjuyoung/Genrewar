package com.example.doublecross.exception;

public class InvalidTurnException extends GameException {

    public InvalidTurnException(String message) {
        super(message);
    }

    public InvalidTurnException(int expectedTurn, int actualTurn) {
        super(String.format("잘못된 턴입니다. 예상: %d, 실제: %d", expectedTurn, actualTurn));
    }
}
