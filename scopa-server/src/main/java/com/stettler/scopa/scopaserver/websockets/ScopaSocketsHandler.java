package com.stettler.scopa.scopaserver.websockets;

import com.stettler.scopa.events.*;
import com.stettler.scopa.scopaserver.model.ScopaMessage;
import com.stettler.scopa.statemachine.EventSource;
import com.stettler.scopa.statemachine.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScopaSocketsHandler extends TextWebSocketHandler {

    Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    ConversionService converter;

    Map<String, WebSocketEventSource> clientToEventSource = new ConcurrentHashMap<>();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        WebSocketEventSource client = new WebSocketEventSource(session);

        logger.info("Registering client {}",session);
        clientToEventSource.put(session.getId(), client);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);

        logger.debug("Received message: {}", message.getPayload());
        ScopaMessage msg = converter.convert(message.getPayload(), ScopaMessage.class);

        GameEvent event = converter.convert(message.getPayload(), GameEvent.class);

        logger.debug("Lookup event source for websocket session {}", session.getId());
        EventSource source = clientToEventSource.get(session.getId());

        logger.debug("Sending event:{} to session's event source:{}", event, source);
        source.triggerEvent(event);

    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        super.handlePongMessage(session, message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        logger.info("Connection closed removing session.");
        clientToEventSource.remove(session.getId());
    }
}
