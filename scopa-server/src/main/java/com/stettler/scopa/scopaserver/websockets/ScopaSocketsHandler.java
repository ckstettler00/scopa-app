package com.stettler.scopa.scopaserver.websockets;

import com.stettler.scopa.events.GameEvent;
import com.stettler.scopa.statemachine.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScopaSocketsHandler extends TextWebSocketHandler {

    Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    ConversionService converter;

    @Autowired
    BeanFactory factory;

    Map<String, WebSocketEventSource> clientToEventSource = new ConcurrentHashMap<>();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        logger.info("New session was created id:{}", session.getId());
        WebSocketEventSource client = this.factory.getBean(WebSocketEventSource.class, session);
        client.start();

        logger.info("Registering client {}", session.getId());
        clientToEventSource.put(session.getId(), client);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);

        logger.debug("Session:{} Received message: {}", session.getId(), message.getPayload());
        if (message.getPayloadLength()==0) {
            logger.debug("Received websocket keepalive");
            return;
        }

        GameEvent event = converter.convert(message.getPayload(), GameEvent.class);

        logger.debug("Lookup event source for websocket session {}", session.getId());
        EventSource source = clientToEventSource.get(session.getId());
        if (source == null) {
            logger.error("Invalid session id {}", session.getId());
            session.sendMessage(new TextMessage("Bad session ID"));
            return;
        }

        logger.info("Forwarding socket message:{} to session's event source:{} session:{}", event, source, session.getId());
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
        logger.info("Connection closed removing session:{}", session.getId());
        clientToEventSource.remove(session.getId());
    }
}
