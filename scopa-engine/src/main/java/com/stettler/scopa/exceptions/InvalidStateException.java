package com.stettler.scopa.exceptions;

import com.stettler.scopa.statemachine.State;

public class InvalidStateException extends ScopaRuntimeException {
    public InvalidStateException(String msg, State currentState) {
        super(String.format("%s current state:%s", msg, currentState.name()));
    }
}
