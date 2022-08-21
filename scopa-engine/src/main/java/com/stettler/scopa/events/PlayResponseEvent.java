package com.stettler.scopa.events;

import com.stettler.scopa.model.Discard;
import com.stettler.scopa.model.Move;
import com.stettler.scopa.model.MoveType;

public class PlayResponseEvent extends GameEvent {

    private Move move;

    public PlayResponseEvent() {
        super();
    }

    public PlayResponseEvent(String playerId, Move move) {
        super(playerId, EventType.PLAY_RESP);
        this.move = move;
    }

    public Move getMove() {
        return move;
    }
}
