package com.stettler.scopa.events;

import com.stettler.scopa.model.PlayerDetails;

public class PlayRequestEvent extends GameEvent {

    private PlayerDetails details;

    public PlayRequestEvent(PlayerDetails details) {
        super(details.getPlayerId(), EventType.PLAY_REQ);
        this.details = details;
    }

    public PlayerDetails getDetails() {
        return details;
    }
}
