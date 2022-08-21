package com.stettler.scopa.events;

import com.stettler.scopa.statemachine.Player;
import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class GameEvent {
    EventType type = EventType.NOOP;

    private String playerId;

    public GameEvent() {

    }

    public String getPlayerId() {
        return playerId;
    }

    public static GameEvent NOOP = new GameEvent(EventType.NOOP) {
        @Override
        public EventType getEventType() {
            return super.getEventType();
        }
    };

    protected GameEvent(EventType type) {
        this(Player.ALL, type);
    }
    protected GameEvent(String playerId, EventType type) {
        this.playerId = playerId;
        this.type = type;
    }
    public EventType getEventType() {
        return this.type;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
