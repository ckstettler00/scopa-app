package com.stettler.scopa.events;

import com.stettler.scopa.model.PlayerDetails;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class NewGameEventResp extends GameEvent {

    private String gameId;

    public NewGameEventResp() { super(EventType.NEWGAME_RESP);}

    public NewGameEventResp(String gameId) {
        super(EventType.NEWGAME_RESP);
        this.gameId = gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameId() {
        return this.gameId;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
