package com.stettler.scopa.events;

import com.stettler.scopa.model.Move;

public class PlayResponseEvent extends GameEvent {

    private Move move;

    public PlayResponseEvent() {
        super(EventType.PLAY_RESP);
    }

    public PlayResponseEvent(String playerId, Move move, String gameId) {
        super(playerId, EventType.PLAY_RESP, gameId);
        this.move = move;
    }

    public Move getMove() {
        return move;
    }

    public void setMove(Move move) {
        this.move = move;
    }
}
