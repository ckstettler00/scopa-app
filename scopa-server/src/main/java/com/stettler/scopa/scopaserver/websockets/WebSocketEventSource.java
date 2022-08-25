package com.stettler.scopa.scopaserver.websockets;

import com.stettler.scopa.events.EventType;
import com.stettler.scopa.events.GameEvent;
import com.stettler.scopa.events.NewGameEvent;
import com.stettler.scopa.events.RegisterEvent;
import com.stettler.scopa.scopaserver.config.GameRegistry;
import com.stettler.scopa.statemachine.EventSource;
import com.stettler.scopa.statemachine.GameControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.socket.WebSocketSession;


public class WebSocketEventSource extends EventSource {

    Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    GameRegistry registry;

    private WebSocketSession session = null;

    public WebSocketEventSource(WebSocketSession session) {
        this.session = session;
        this.addHandler(EventType.NEWGAME, this::handleNewGameEvent);
        this.addHandler(EventType.REGISTER, this::handleRegistration);
    }

    protected void handleNewGameEvent(GameEvent event) {
        logger.info("Registering new game event: ");
        GameControl game = registry.newGame();
        game.triggerEvent(new NewGameEvent());
        logger.info("Trigger new game event: {} {}", event, game.getGameId());
    }

    protected void handleRegistration(GameEvent event) {
        RegisterEvent registerEvent = (RegisterEvent) event;
        this.registry.registerPlayer(registerEvent.getGameId(), registerEvent.getDetails(), this);
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
}
