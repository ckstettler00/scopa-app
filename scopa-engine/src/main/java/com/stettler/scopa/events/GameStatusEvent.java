package com.stettler.scopa.events;

import com.stettler.scopa.model.GameStatus;

public class GameStatusEvent extends GameEvent {

    private GameStatus status = new GameStatus();

    public GameStatusEvent(GameStatus status) {
        super(EventType.STATUS);
        this.status = status;
    }

    public GameStatus getStatus() {
        return status;
    }
}
