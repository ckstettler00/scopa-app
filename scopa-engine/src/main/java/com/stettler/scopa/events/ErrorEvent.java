package com.stettler.scopa.events;

import com.stettler.scopa.statemachine.EventSource;

public class ErrorEvent extends GameEvent {
    private String message;

    public ErrorEvent() {
        super(EventType.ERROR);
    }
    public ErrorEvent(String playerId, String message) {
        super(playerId, EventType.ERROR);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
