package com.stettler.scopa.exceptions;

import com.stettler.scopa.events.GameEvent;

public class UnexpectedEventException extends ScopaException {
    private UnexpectedEventException() {
        super();
    }

    public UnexpectedEventException(String playerId, GameEvent event, String message) {
        super(playerId, String.format("%s : event: %s ", event, message));
    }

    public UnexpectedEventException(String playerId, GameEvent event) {
        this(playerId, event, "Unexpected event");
    }
}
