package com.example.doublecross.exception;

import java.util.UUID;

public class GameNotFoundException extends GameException {

    public GameNotFoundException(UUID sessionId) {
        super("게임을 찾을 수 없습니다: " + sessionId);
    }

    public GameNotFoundException(String message) {
        super(message);
    }
}
