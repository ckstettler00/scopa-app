package com.stettler.scopa.exceptions;

public class ScopaRuntimeException extends RuntimeException {
    public ScopaRuntimeException() {
        super();
    }

    public ScopaRuntimeException(String msg) {
        super(msg);
    }

    public ScopaRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScopaRuntimeException(Throwable cause) {
        super(cause);
    }
}
