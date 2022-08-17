package com.stettler.scopa.exceptions;

import com.stettler.scopa.events.GameEvent;
import com.stettler.scopa.statemachine.State;

public class InvalidStateTransitionException extends ScopaException{
    private InvalidStateTransitionException() {
        super();
    }

    public InvalidStateTransitionException(String playerId, State current, GameEvent event) {
        super(playerId, String.format("Invalid event %s for current state %s",
                event.getEventType(), current));
    }
}
