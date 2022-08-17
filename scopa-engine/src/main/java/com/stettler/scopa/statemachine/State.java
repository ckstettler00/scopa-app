package com.stettler.scopa.statemachine;

public enum State {
    INIT,
    WAIT_FOR_PLAYER1,
    WAIT_FOR_PLAYER2,

    REQUEST_PLAYER1_MOVE,
    WAIT_4_PLAYER1_MOVE,
    REQUEST_PLAYER2_MOVE,
    WAIT_4_PLAYER2_MOVE,
    START_ROUND,
    WINNER
}
