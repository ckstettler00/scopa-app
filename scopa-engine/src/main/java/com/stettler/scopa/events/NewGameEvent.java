package com.stettler.scopa.events;

import com.stettler.scopa.model.PlayerDetails;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class NewGameEvent extends GameEvent {

    private PlayerDetails details;

    public NewGameEvent() { super(EventType.NEWGAME);}

    public NewGameEvent(PlayerDetails details) {
        super(EventType.NEWGAME);
        this.details = details;
    }

    public void setDetails(PlayerDetails details) {
        this.details = details;
    }

    public PlayerDetails getDetails() {
        return this.details;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
