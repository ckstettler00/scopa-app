package com.stettler.scopa.scopaserver.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stettler.scopa.events.*;
import com.stettler.scopa.exceptions.ScopaRuntimeException;
import com.stettler.scopa.scopaserver.model.ScopaMessage;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StringToGameEvent implements Converter<String, GameEvent> {
    private ObjectMapper mapper = new ObjectMapper();
    Map<String, Class<?>> eventClass = new ConcurrentHashMap<>();

    public StringToGameEvent() {
        eventClass.put(EventType.ERROR.name(), ErrorEvent.class);
        eventClass.put(EventType.STATUS.name(), GameStatusEvent.class);
        eventClass.put(EventType.NEWGAME.name(), NewGameEvent.class);
        eventClass.put(EventType.NEWGAME_RESP.name(), NewGameEventResp.class);
        eventClass.put(EventType.PLAY_RESP.name(), PlayResponseEvent.class);
        eventClass.put(EventType.PLAY_REQ.name(), PlayRequestEvent.class);
        eventClass.put(EventType.REGISTER.name(), RegisterEvent.class);
    }

    @Override
    public GameEvent convert(String payload) {

        try {
            ScopaMessage msg = mapper.readValue(payload, ScopaMessage.class);
            Class<GameEvent> clazz = (Class<GameEvent>)eventClass.get(msg.getMessageType());
            if (clazz == null) {
                throw new ScopaRuntimeException("No event for eventType found");
            }
            return mapper.readValue(msg.getPayload(), clazz);
        } catch (JsonProcessingException e) {
            throw new ScopaRuntimeException(e);
        }
    }
}
