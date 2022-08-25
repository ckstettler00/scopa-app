package com.stettler.scopa.scopaserver.utils;

import com.stettler.scopa.exceptions.ScopaRuntimeException;
import com.stettler.scopa.model.PlayerDetails;
import com.stettler.scopa.statemachine.EventSource;
import com.stettler.scopa.statemachine.GameControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameRegistry {

    Logger logger = LoggerFactory.getLogger(getClass().getName());
    private Map<String, GameControl> gameMap = new ConcurrentHashMap<>();

    public GameControl newGame() {
        logger.info("Create a new game");
        GameControl game = new GameControl();
        game.start();
        logger.info("Game ID: {}", game.getGameId());
        this.gameMap.put(game.getGameId(), game);

        return game;
    }

    public List<String>  getGameIds(){

        ArrayList<String> temp = new ArrayList<>();
        temp.addAll(gameMap.keySet());

        return temp;
    }
    public GameControl findGame(String gameId) {
        return gameMap.get(gameId);
    }

}
