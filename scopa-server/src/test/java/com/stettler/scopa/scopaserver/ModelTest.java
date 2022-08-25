package com.stettler.scopa.scopaserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.stettler.scopa.events.EventType;
import com.stettler.scopa.events.NewGameEvent;
import com.stettler.scopa.events.RegisterEvent;
import com.stettler.scopa.model.PlayerDetails;
import com.stettler.scopa.scopaserver.model.ScopaMessage;
import org.junit.jupiter.api.Test;

public class ModelTest {
    @Test
    void modelTest() throws Exception{

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        ScopaMessage m = new ScopaMessage();

        m.setMessageType(EventType.NEWGAME.name());
        m.setPayload(mapper.writeValueAsString(new NewGameEvent()));

        System.out.println(mapper.writeValueAsString(m));

        PlayerDetails p = new PlayerDetails();
        p.setEmailAddr("nate@gmail.com");
        p.setPlayerSecret("secret");
        p.setPlayerToken("token");
        p.setScreenHandle("natename");
        RegisterEvent r = new RegisterEvent(p);
        m.setMessageType(EventType.REGISTER.name());
        m.setPayload(mapper.writeValueAsString(r));
        System.out.println(mapper.writeValueAsString(m));
    }

}
