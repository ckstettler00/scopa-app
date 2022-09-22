package com.stettler.scopa.scopaserver.utils;

import com.stettler.scopa.exceptions.ScopaRuntimeException;
import com.stettler.scopa.model.PlayerDetails;
import com.stettler.scopa.statemachine.EventSource;
import com.stettler.scopa.statemachine.GameControl;
import com.stettler.scopa.statemachine.Player;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
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

    /**
     * Search for a game with a particular playerId associated.
     * @param gameId
     * @param playerId
     * @return
     */
    public GameControl findGameWithPlayerId(String gameId, String playerId) {
        GameControl game = this.findGame(gameId);
        Optional<Player> tmp = game.getAllPlayers().stream().filter(p -> p.getDetails().getPlayerId().equals(playerId)).findFirst();
        if (tmp.isPresent()) {
            return game;
        }
        return null;
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
