package com.stettler.scopa.scopaserver.config;

import com.stettler.scopa.events.NewGameEvent;
import com.stettler.scopa.exceptions.ScopaRuntimeException;
import com.stettler.scopa.model.PlayerDetails;
import com.stettler.scopa.statemachine.EventSource;
import com.stettler.scopa.statemachine.GameControl;
import jdk.internal.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameRegistry {

    Logger logger = LoggerFactory.getLogger(getClass().getName());
    private Map<String, GameControl> gameMap = new ConcurrentHashMap<>();

    public GameControl newGame() {
        GameControl game = new GameControl();
        game.start();
        return game;
    }

    public GameControl findGame(String gameId) {
        return gameMap.get(gameId);
    }

    public void registerPlayer(String gameId, PlayerDetails details, EventSource source) {
        GameControl control = gameMap.get(gameId);
        if (control == null) {
            logger.error("Invalid game id: " + gameId);
            throw new ScopaRuntimeException(String.format("Invalid game id: %s", gameId));
        }

        control.registerPlayer(details, source);

    }

}
