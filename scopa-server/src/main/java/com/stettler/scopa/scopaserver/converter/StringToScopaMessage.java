package com.stettler.scopa.scopaserver.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stettler.scopa.exceptions.ScopaRuntimeException;
import com.stettler.scopa.scopaserver.model.ScopaMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToScopaMessage implements Converter<String, ScopaMessage> {
    private ObjectMapper mapper = new ObjectMapper();

    public ScopaMessage convert(String payload) {
        try {
            return mapper.readValue(payload, ScopaMessage.class);
        } catch (JsonProcessingException e) {
            throw new ScopaRuntimeException("ScopaMessaage parse exception", e);
        }
    }
}
