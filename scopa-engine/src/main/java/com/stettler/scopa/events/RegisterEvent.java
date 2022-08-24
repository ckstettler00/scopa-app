package com.stettler.scopa.events;

import com.stettler.scopa.model.PlayerDetails;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class RegisterEvent extends GameEvent {

    private PlayerDetails details;
    private String gameId;

    public RegisterEvent() { super(EventType.REGISTER);}

    public RegisterEvent(PlayerDetails details) {
        super(EventType.REGISTER);
        this.details = details;
    }

    public void setDetails(PlayerDetails details) {
        this.details = details;
    }

    public PlayerDetails getDetails() {
        return this.details;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
