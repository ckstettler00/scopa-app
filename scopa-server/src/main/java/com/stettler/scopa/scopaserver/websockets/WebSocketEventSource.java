package com.stettler.scopa.scopaserver.websockets;

import com.stettler.scopa.events.GameEvent;
import com.stettler.scopa.statemachine.EventSource;
import org.springframework.web.socket.WebSocketSession;

public class WebSocketEventSource extends EventSource {

    private WebSocketSession session = null;

    public WebSocketEventSource(WebSocketSession session) {
        this.session = session;
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
