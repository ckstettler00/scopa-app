package com.stettler.scopa.scopaserver.websockets;

import com.stettler.scopa.events.*;
import com.stettler.scopa.exceptions.ScopaRuntimeException;
import com.stettler.scopa.scopaserver.utils.GameRegistry;
import com.stettler.scopa.statemachine.EventSource;
import com.stettler.scopa.statemachine.GameControl;
import com.stettler.scopa.statemachine.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Optional;

@Scope("prototype")
@Component
public class WebSocketEventSource extends EventSource {

    Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    GameRegistry registry;

    @Autowired
    ConversionService converter;

    private WebSocketSession session = null;

    public WebSocketEventSource(WebSocketSession session) {
        logger.info("Creating WebSocketEventSource for session:{}", session.getId());
        this.session = session;
        this.addHandler(EventType.NEWGAME, this::handleNewGameEvent);
        this.addHandler(EventType.REGISTER, this::handleRegistration);
        this.addHandler(EventType.PLAY_RESP, this::handlePlayResponse);
        this.addHandler(EventType.GAMEOVER, this::sendToClient);
        this.addHandler(EventType.STATUS, this::sendToClient);
        this.addHandler(EventType.PLAY_REQ, this::sendToClient);
        this.addHandler(EventType.SCOPA, this::sendToClient);
        this.addHandler(EventType.ERROR, this::sendToClient);
    }

    protected void handlePlayResponse(GameEvent event) {
        logger.info("Session:{} handlePlayResponse {}", this.session.getId(), event);
        GameControl game = registry.findGame(event.getGameId());
        if (game == null) {
            logger.error("Session:{} play failed. game not found: {}", this.session.getId(), event);
            throw new ScopaRuntimeException("failed to find game: "+event.getGameId());
        }

        logger.info("Session:{} forwarding event to game controller: {} event:{}",
                this.session.getId(), game.getGameId(),
                event);
        game.triggerEvent(event);

    }
    protected void handleNewGameEvent(GameEvent event) {
        logger.info("Session:{} Registering new game event: {}", this.session.getId(), event);
        GameControl game = registry.newGame();

        // Trigger the new game event first
        game.initializeGame(((NewGameEvent)event).getDetails(), this);

        logger.info("Session:{} registered game id {} back to the client.",
                this.session.getId(), game.getGameId());
        NewGameEventResp resp = new NewGameEventResp(game.getGameId());
        sendToClient(resp);
    }

    protected void handleRegistration(GameEvent event) {
        RegisterEvent registerEvent = (RegisterEvent) event;
        if (registerEvent.getGameId() == null) {
            logger.error("Session:{} Invalid game id received: {}",
                    this.session.getId(), registerEvent.getGameId());
            throw new ScopaRuntimeException("Invalid game id " + registerEvent.getGameId());
        }
        logger.debug("Session:{} Locate game id: {}",
                this.session.getId(), registerEvent.getGameId());
        GameControl game = registry.findGame(registerEvent.getGameId());
        if (game == null) {
            logger.error("Session:{} Invalid game id received: {}", this.session.getId(),
                    registerEvent.getGameId());
            throw new ScopaRuntimeException("Invalid game id " + registerEvent.getGameId());
        }
        logger.info("Session:{} Processing registerEvent details {}", this.session.getId(),
                registerEvent);
        game.registerPlayer(registerEvent.getDetails(), this);

    }

    @Override
    public void handleEvent(GameEvent event) {
        super.handleEvent(event);
    }

    @Override
    public void handleUnknownEvent(GameEvent event) {
        super.handleUnknownEvent(event);
    }

    @Override
    public void handleException(Exception ex) {
        super.handleException(ex);
        ErrorEvent error = new ErrorEvent(Player.ALL, ex.getMessage());
        sendToClient(error);
    }


    protected void sendToClient(GameEvent event) {
        String msg = converter.convert(event, String.class);
        try {
            synchronized(this.session) {
                logger.info("Session:{} sendToClient: {}", this.session.getId(), event);
                this.session.sendMessage(new TextMessage(msg));
            }
        } catch (IOException e) {
            logger.error("Session:{} Failed to write message to the client: {}", this.session.getId(), msg);
            throw new ScopaRuntimeException("Failed to write event to client");
        }
    }
    @Override
    public String toString() {
        return String.format("WebSocketEventSource: session:%s source:%s" +
                "", this.session, this.getSourceId());
    }
}
