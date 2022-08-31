package com.stettler.scopa.events;

public class ShutdownEvent extends GameEvent {
    public ShutdownEvent() {
        super(EventType.SHUTDOWN);
    }
}
