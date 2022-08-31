package com.stettler.scopa.scopaserver.controller;

import com.stettler.scopa.exceptions.ScopaRuntimeException;
import com.stettler.scopa.model.GameStatus;
import com.stettler.scopa.scopaserver.model.GameEntry;
import com.stettler.scopa.scopaserver.model.TestGameSetup;
import com.stettler.scopa.scopaserver.service.GameService;
import com.stettler.scopa.scopaserver.utils.GameRegistry;
import com.stettler.scopa.statemachine.GameControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Profile("testhelper")
@RestController
@RequestMapping("/testhelper")
public class TestController {
    Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    private GameRegistry registry;

    @PostMapping(path = "/initgame/{gameId}", produces = "application/json")
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
