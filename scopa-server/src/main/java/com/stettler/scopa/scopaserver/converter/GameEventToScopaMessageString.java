package com.stettler.scopa.scopaserver.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stettler.scopa.events.GameEvent;
import com.stettler.scopa.exceptions.ScopaRuntimeException;
import com.stettler.scopa.scopaserver.model.ScopaMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

public class GameEventToScopaMessageString implements Converter<GameEvent, String> {
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convert(GameEvent event) {
        ScopaMessage msg = new ScopaMessage();
        msg.setMessageType(event.getEventType().name());
        msg.setPayload(toJson(event));
        return toJson(msg);
    }

    private String toJson(Object msg) {
        try {
            return mapper.writeValueAsString(msg);
        } catch (JsonProcessingException e) {
            throw new ScopaRuntimeException(String.format("Failed to convert to json %s", msg), e);
        }
    }
}
