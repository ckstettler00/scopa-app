package com.stettler.scopa.exceptions;

import com.stettler.scopa.events.GameEvent;
import com.stettler.scopa.model.Move;
import com.stettler.scopa.statemachine.State;

public class InvalidMoveException extends ScopaException {
    private InvalidMoveException() {
        super();
    }

    public InvalidMoveException(String playerId, Move move, String message) {
        super(playerId, String.format("%s : move %s",message, move));
    }

    public InvalidMoveException(String playerId, Move move) {
        this(playerId, move, "Invalid");
    }
}
