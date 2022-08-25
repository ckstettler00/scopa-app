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
        this.session = session;
        this.addHandler(EventType.NEWGAME, this::handleNewGameEvent);
        this.addHandler(EventType.REGISTER, this::handleRegistration);
        this.addHandler(EventType.GAMEOVER, this::sendToClient);
        this.addHandler(EventType.STATUS, this::sendToClient);
        this.addHandler(EventType.PLAY_REQ, this::sendToClient);
        this.addHandler(EventType.SCOPA, this::sendToClient);
        this.addHandler(EventType.ERROR, this::sendToClient);
    }

    protected void handleNewGameEvent(GameEvent event) {
        logger.info("Registering new game event: ");
        GameControl game = registry.newGame();
        game.triggerEvent(new NewGameEvent());
        sendToClient(new ErrorEvent(Player.ALL, "Not an error"));
        logger.info("Trigger new game event: {} {}", event, game.getGameId());
    }

    protected void handleRegistration(GameEvent event) {
        RegisterEvent registerEvent = (RegisterEvent) event;
        if (registerEvent.getGameId() == null) {
            logger.error("Invalid game id received: {}", registerEvent.getGameId());
            throw new ScopaRuntimeException("Invalid game id " + registerEvent.getGameId());
        }
        logger.debug("Locate game id: {}", registerEvent.getGameId());
        GameControl game = registry.findGame(registerEvent.getGameId());
        if (game == null) {
            logger.error("Invalid game id received: {}", registerEvent.getGameId());
            throw new ScopaRuntimeException("Invalid game id " + registerEvent.getGameId());
        }
        logger.info("Processing registerEvent details {}", registerEvent);
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
    }


    protected void sendToClient(GameEvent event) {
        String msg = converter.convert(event, String.class);
        try {
            synchronized(this.session) {
                this.session.sendMessage(new TextMessage(msg));
            }
        } catch (IOException e) {
            logger.error("Failed to write message to the client: {}", msg);
            throw new ScopaRuntimeException("Failed to write event to client");
        }
    }
}
