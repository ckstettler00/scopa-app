package com.stettler.scopa.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.stettler.scopa.statemachine.Player;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ErrorEvent.class, name = "ErrorEvent"),
        @JsonSubTypes.Type(value = GameOverEvent.class, name = "GameOverEvent"),
        @JsonSubTypes.Type(value = NewGameEvent.class, name = "NewGameEvent"),
        @JsonSubTypes.Type(value = PlayRequestEvent.class, name = "PlayRequestEvent"),
        @JsonSubTypes.Type(value = PlayResponseEvent.class, name = "PlayResponseEvent"),
        @JsonSubTypes.Type(value = RegisterEvent.class, name = "RegisterEvent"),
        @JsonSubTypes.Type(value = ScopaEvent.class, name = "ScopaEvent"),
        @JsonSubTypes.Type(value = ShutdownEvent.class, name = "ShutdownEvent"),
        @JsonSubTypes.Type(value = StartRoundEvent.class, name = "StartRoundEvent"),
        @JsonSubTypes.Type(value = GameStatusEvent.class, name = "GameStatusEvent")})
public abstract class GameEvent {
    EventType eventType = EventType.NOOP;

    @JsonIgnore
    private String fromSourceId;

    private String playerId;

    public GameEvent() {

    }

    public String getFromSourceId() {
        return fromSourceId;
    }

    public void setFromSourceId(String fromSourceId) {
        this.fromSourceId = fromSourceId;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
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
        this.eventType = type;
    }
    public EventType getEventType() {
        return this.eventType;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
