package com.stettler.scopa.scopaserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.stettler.scopa.events.EventType;
import com.stettler.scopa.events.NewGameEvent;
import com.stettler.scopa.events.PlayResponseEvent;
import com.stettler.scopa.events.RegisterEvent;
import com.stettler.scopa.model.*;
import com.stettler.scopa.scopaserver.model.ScopaMessage;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class ModelTest {
    @Test
    void modelTest() throws Exception{

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        ScopaMessage m = new ScopaMessage();

        m.setMessageType(EventType.NEWGAME.name());
        System.out.println(mapper.writeValueAsString(m));

        PlayerDetails p = new PlayerDetails();
        p.setEmailAddr("nate@gmail.com");
        p.setPlayerSecret("secret");
        p.setPlayerToken("token");
        p.setScreenHandle("natename");
        RegisterEvent r = new RegisterEvent(p);
        m.setMessageType(EventType.REGISTER.name());
        r.setGameId("62357ba0-00be-4fb3-bcd9-0fa7c1cec274");
        m.setPayload(mapper.writeValueAsString(r));
        System.out.println(mapper.writeValueAsString(m));

        m.setMessageType(EventType.NEWGAME.name());
        m.setPayload(mapper.writeValueAsString(new NewGameEvent(p)));
        System.out.println(mapper.writeValueAsString(m));

        Card c = new Card(2, Suit.COINS);
        Pickup t = new Pickup(c, Arrays.asList(c));
        Discard d = new Discard(c);

        System.out.println(mapper.writeValueAsString(d));
        System.out.println(mapper.writeValueAsString(t));

        PlayResponseEvent e = new PlayResponseEvent("playerid", t, "gameid");
        System.out.println(mapper.writeValueAsString(e));

    }

}
