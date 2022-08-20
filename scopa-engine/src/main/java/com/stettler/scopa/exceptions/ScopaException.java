package com.stettler.scopa.exceptions;

import com.stettler.scopa.statemachine.Player;

public class ScopaException extends RuntimeException {

    String playerId;

    public String getPlayerId() {
        return playerId;
    }

    public ScopaException() {
        super();
        playerId = Player.ALL;
    }

    public ScopaException(String playerId, String message) {
        super(message);
        this.playerId = playerId;
    }

    public ScopaException(String message) {
        super(message);
        this.playerId = Player.ALL;
    }

    public ScopaException(String playerId, String message, Throwable cause) {
        super(message, cause);
        this.playerId = playerId;
    }

    public ScopaException(String playerId, Throwable cause) {
        super(cause);
        this.playerId = playerId;
    }

    protected ScopaException(String playerId, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.playerId = playerId;
    }
}
