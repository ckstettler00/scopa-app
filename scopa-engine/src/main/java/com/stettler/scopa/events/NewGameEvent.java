package com.stettler.scopa.events;

public class NewGameEvent extends GameEvent {
    public NewGameEvent() {
        super(EventType.NEWGAME);
    }
}
