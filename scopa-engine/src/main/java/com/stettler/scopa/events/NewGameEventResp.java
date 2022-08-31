package com.stettler.scopa.events;

import com.stettler.scopa.model.PlayerDetails;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class NewGameEventResp extends GameEvent {

    public NewGameEventResp() { super(EventType.NEWGAME_RESP);}

    public NewGameEventResp(String gameId) {
        super(EventType.NEWGAME_RESP);
        this.setGameId(gameId);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
