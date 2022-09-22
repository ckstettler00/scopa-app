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
        this.session = session;
        this.addHandler(EventType.NEWGAME, this::handleNewGameEvent);
        this.addHandler(EventType.REGISTER, this::handleRegistration);
        this.addHandler(EventType.PLAY_RESP, this::handlePlayResponse);
        this.addHandler(EventType.GAMEOVER, this::sendToClient);
        this.addHandler(EventType.STATUS, this::sendToClient);
        this.addHandler(EventType.PLAY_REQ, this::sendToClient);
        this.addHandler(EventType.SCOPA, this::sendToClient);
        this.addHandler(EventType.ERROR, this::sendToClient);
        this.addHandler(EventType.RECONNECT, this::handleReconnect);
    }

    protected void handlePlayResponse(GameEvent event) {
        logger.info("Playing a move {}", event);
        GameControl game = registry.findGame(event.getGameId());
        if (game == null) {
            logger.error("play failed. game not found: {}", event);
            throw new ScopaRuntimeException("failed to find game: "+event.getGameId());
        }

        logger.info("forwarding event to game controller: {} event:{}", game.getGameId(),
                event);
        game.triggerEvent(event);

    }
    protected void handleNewGameEvent(GameEvent event) {
        logger.info("Registering new game event: {}", event);
        GameControl game = registry.newGame();

        // Trigger the new game event first
        game.initializeGame(((NewGameEvent)event).getDetails(), this);

        logger.info("registered game id {} back to the client.", game.getGameId());
        NewGameEventResp resp = new NewGameEventResp(game.getGameId());
        sendToClient(resp);
    }

    protected void handleReconnect(GameEvent event) {
        logger.info("handleReconnect {}", event);
        ReconnectEvent reconnectEvent = (ReconnectEvent) event;
        if (reconnectEvent.getGameId() == null) {
            logger.error("Invalid game id received: {}", reconnectEvent.getGameId());
            throw new ScopaRuntimeException("Invalid game id " + reconnectEvent.getGameId());
        }
        if (reconnectEvent.getPlayerId() == null) {
            logger.error("Invalid player id received: {}", reconnectEvent.getPlayerId());
            throw new ScopaRuntimeException("Invalid player id " + reconnectEvent.getPlayerId());
        }

        GameControl game = registry.findGameWithPlayerId(reconnectEvent.getGameId(), reconnectEvent.getPlayerId());
        if (game == null) {
            logger.error("Invalid game id received: {}", reconnectEvent.getGameId());
            throw new ScopaRuntimeException("Invalid game id " + reconnectEvent.getGameId());
        }

        // Should be empty by this point.
        Optional<Player> player = game.getAllPlayers().stream().filter(p -> p.getDetails().getPlayerId().equals(event.getPlayerId())).findFirst();

        // Send an update game status to the new connection.
        logger.info("handleReconnect: successful");
        sendToClient(new GameStatusEvent(game.getStatus(player.get())));
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
                logger.info("sendToClient: {}", event);
                this.session.sendMessage(new TextMessage(msg));
            }
        } catch (IOException e) {
            logger.error("Failed to write message to the client: {}", msg);
            throw new ScopaRuntimeException("Failed to write event to client");
        }
    }
}
