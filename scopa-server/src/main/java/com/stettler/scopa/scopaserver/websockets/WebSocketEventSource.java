package com.stettler.scopa.scopaserver.websockets;

import com.stettler.scopa.events.EventType;
import com.stettler.scopa.events.GameEvent;
import com.stettler.scopa.events.NewGameEvent;
import com.stettler.scopa.events.RegisterEvent;
import com.stettler.scopa.scopaserver.config.GameRegistry;
import com.stettler.scopa.statemachine.EventSource;
import com.stettler.scopa.statemachine.GameControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

public class WebSocketEventSource extends EventSource {

    @Autowired
    GameRegistry registry;

    private WebSocketSession session = null;

    public WebSocketEventSource(WebSocketSession session) {
        this.session = session;
        this.addHandler(EventType.NEWGAME, this::handleNewGameEvent);
        this.addHandler(EventType.REGISTER, this::handleRegistration);
    }

    protected void handleNewGameEvent(GameEvent event) {
        GameControl game = registry.newGame();
        game.triggerEvent(new NewGameEvent());
    }

    protected void handleRegistration(GameEvent event) {
        RegisterEvent registerEvent = (RegisterEvent) event;

        GameControl game = this.registry.findGame(registerEvent.getGameId());
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
}
