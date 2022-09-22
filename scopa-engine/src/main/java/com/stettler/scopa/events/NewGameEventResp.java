package com.stettler.scopa.events;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class NewGameEventResp extends GameEvent {



    public String sessionId;
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
