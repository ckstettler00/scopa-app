package com.stettler.scopa.events;

import com.stettler.scopa.model.PlayerDetails;

public class RegisterEvent extends GameEvent {

    private PlayerDetails details;

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
}
