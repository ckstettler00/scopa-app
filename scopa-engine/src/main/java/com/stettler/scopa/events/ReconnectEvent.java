package com.stettler.scopa.events;

import com.stettler.scopa.model.PlayerDetails;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ReconnectEvent extends GameEvent {

    private String gameId;

    private String playerId;

    public ReconnectEvent() {
        super(EventType.RECONNECT);
    }

    @Override
    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
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

