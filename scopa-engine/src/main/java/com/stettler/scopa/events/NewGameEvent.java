package com.stettler.scopa.events;

public class NewGameEvent extends GameEvent {
    public NewGameEvent() {
        super(EventType.NEWGAME);
    }
    public NewGameEvent(String playerId) {
        super(playerId, EventType.NEWGAME);
    }
}
