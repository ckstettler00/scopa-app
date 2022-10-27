package com.stettler.scopa.scopaserver.controller;

import com.stettler.scopa.events.GameOverEvent;
import com.stettler.scopa.events.ScopaEvent;
import com.stettler.scopa.exceptions.ScopaRuntimeException;
import com.stettler.scopa.model.GameStatus;
import com.stettler.scopa.scopaserver.model.GameEntry;
import com.stettler.scopa.scopaserver.model.TestGameSetup;
import com.stettler.scopa.scopaserver.service.GameService;
import com.stettler.scopa.scopaserver.utils.GameRegistry;
import com.stettler.scopa.scopaserver.websockets.WebSocketEventSource;
import com.stettler.scopa.statemachine.GameControl;
import com.stettler.scopa.statemachine.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;

@Profile("testhelper")
@RestController
@RequestMapping("/testhelper")
public class TestController {
    Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    private GameRegistry registry;


    @PostConstruct
    void init() {
        logger.warn("TestHelper is enabled.");
    }

    @PostMapping(path="simulate/gameover/{gameId}/{playerNum}")
    public void gameOver(@PathVariable("gameId") String gameId, @PathVariable("playerNum") Integer playerNum) {
        GameControl game = registry.findGame(gameId);
        if (game == null) {
            logger.error("gameSetup: game id:{} not found", gameId);
            throw new ScopaRuntimeException("Cant file game:"+gameId);
        }

        WebSocketEventSource s1 = (WebSocketEventSource) game.lookupSource(game.getAllPlayers().get(0));
        WebSocketEventSource s2 = (WebSocketEventSource) game.lookupSource(game.getAllPlayers().get(1));
        GameOverEvent event = new GameOverEvent();
        event.setGameId(gameId);
        event.setLosingPlayer(game.getAllPlayers().get((playerNum==0)?1:0).getDetails());
        event.setWinningPlayer(game.getAllPlayers().get(playerNum).getDetails());
        event.setLosingScore(11);
        event.setWinningScore(12);

        s1.triggerEvent(event);
        s2.triggerEvent(event);
    }

    @PostMapping(path="simulate/scopa/{gameId}/{playerNum}")
    public void scopa(@PathVariable("gameId") String gameId, @PathVariable("playerNum") Integer playerNum) {
        GameControl game = registry.findGame(gameId);
        if (game == null) {
            logger.error("gameSetup: game id:{} not found", gameId);
            throw new ScopaRuntimeException("Cant file game:"+gameId);
        }

        WebSocketEventSource s = (WebSocketEventSource) game.lookupSource(game.getAllPlayers().get(playerNum));

        ScopaEvent event = new ScopaEvent();
        event.setGameId(gameId);
        event.setFinalTrick(false);

        s.triggerEvent(event);
    }

    @GetMapping (path="kill/{gameId}/{playerNum}")
    public void killPlayer(@PathVariable("gameId") String gameId, @PathVariable("playerNum") Integer playerNum) {
        GameControl game = registry.findGame(gameId);
        if (game == null) {
            logger.error("gameSetup: game id:{} not found", gameId);
            throw new ScopaRuntimeException("Cant file game:"+gameId);
        }
        Player p = game.getAllPlayers().get(playerNum);
        WebSocketEventSource s = (WebSocketEventSource) game.lookupSource(p);
        s.close();
    }
    @PostMapping(path = "initgame/{gameId}", produces = "application/json")
    public void gameSetup(@PathVariable("gameId") String gameId, @RequestBody TestGameSetup setup){
        GameControl game = registry.findGame(gameId);
        if (game == null) {
            logger.error("gameSetup: game id:{} not found", gameId);
            throw new ScopaRuntimeException("Cant file game:"+gameId);
        }

        logger.info("gameSetup input: {}", setup);
        if (setup.getPlayer1Hand() != null) {
            logger.info("gameSetup -- setting player1 hand {}", setup.getPlayer1Hand());
            game.getPlayer1().getHand().clear();
            game.getPlayer1().getHand().addAll(setup.getPlayer1Hand());
        }
        if (setup.getPlayer2Hand() != null) {
            logger.info("gameSetup -- setting player2 hand {}", setup.getPlayer2Hand());
            game.getPlayer2().getHand().clear();
            game.getPlayer2().getHand().addAll(setup.getPlayer2Hand());
        }
        if (setup.getTableCards() != null) {
            logger.info("gameSetup  -- table cards {}", setup.getTableCards());
            game.getGameplay().setTableCards(setup.getTableCards());
        }

        if (setup.getCardsRemaining()!=null) {
            logger.info("gameSetup -- setting cards remaining {}", setup.getCardsRemaining());
            while (game.getGameplay().getDeck().size() < setup.getCardsRemaining()) {
                game.getGameplay().getDeck().draw();
            }
        }
        logger.info("test setup gamestatus player 2: {}", game.getStatus(game.getPlayer2()));
        logger.info("test setup gamestatus player 1: {}", game.getStatus(game.getPlayer1()));
    }

}
