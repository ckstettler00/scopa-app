package com.stettler.scopa.scopaserver.cucumber.util;

import com.stettler.scopa.events.GameEvent;
import com.stettler.scopa.statemachine.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class TestSocketHandler extends TextWebSocketHandler {

    Logger logger = LoggerFactory.getLogger(getClass().getName());
    EventSource source;
    ConversionService converter;

    public EventSource getEventSource() {
        return source;
    }

    public TestSocketHandler(ConversionService converter) {
        this.converter = converter;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        this.source = new TestEventSource();
        this.source.setSourceId(session.getId());
        logger.info("Setting source id {} to session id {}",
                this.source.getSourceId(), session.getId());
        this.source.start();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        GameEvent event = converter.convert(message.getPayload(), GameEvent.class);
        logger.info("Received server message {} placing in event. session id {}.", event, session.getId());
        this.source.triggerEvent(event);
    }
}

