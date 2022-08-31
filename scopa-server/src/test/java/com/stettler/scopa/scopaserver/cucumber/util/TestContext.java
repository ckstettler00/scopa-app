package com.stettler.scopa.scopaserver.cucumber.util;

import com.stettler.scopa.model.GameStatus;
import com.stettler.scopa.model.PlayerDetails;
import com.stettler.scopa.statemachine.EventSource;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

public class TestContext {

    String gameId;

    GameStatus gameStatus;

    private List<PlayerDetails> players = new ArrayList<>();
    private List<WebSocketSession> sessions = new ArrayList<>();
    private List<EventSource> eventSources = new ArrayList<>();

    private static TestContext context = new TestContext();

    public void clear() {
        players.clear();
        sessions.clear();
        eventSources.clear();
        init();
    }
    public TestContext() {
        init();
    }
    private void init() {
        for (int i = 0; i < 2; i++){
            this.eventSources.add(null);
            this.players.add(null);
            this.sessions.add(null);
        }
    }
    public static TestContext context() {
        return context;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public void setPlayer(Integer idx, PlayerDetails details) {
        players.set(idx, details);
    }

    public void setSession(Integer idx, WebSocketSession session) {
        sessions.set(idx, session);
    }

    public void setEventSource(Integer idx, EventSource source)
    {
        eventSources.set(idx, source);
    }

    public WebSocketSession getSession(Integer idx) {
        return this.sessions.get(idx);
    }
    public PlayerDetails getPlayer(Integer idx) {
        return this.players.get(idx);
    }
    public TestEventSource getEventSource(Integer idx) {
        return (TestEventSource) eventSources.get(idx);
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }
}
