package com.stettler.scopa.scopaserver.controller;

import com.stettler.scopa.model.GameStatus;
import com.stettler.scopa.scopaserver.model.GameEntry;
import com.stettler.scopa.scopaserver.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/scopa")
public class ApiController {
    Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    private GameService service;

    @GetMapping(path = "/gamelist", produces = "application/json")
    public List<GameEntry> getGameList(){
        List<GameEntry> list = service.getGameList();
        logger.info("gamelist api resp: {}", list);
        return list;
    }
    @GetMapping(path = "/gamelist/{gameId}", produces = "application/json")
    public List<GameStatus> getGameStatus(@PathVariable String gameId){
        List<GameStatus> list = service.getGameStatus(gameId);
        logger.info("game status list for game id: {} list: {}", gameId, list);
        return list;
    }

    @GetMapping(path = "/health", produces = "application/json")
    public String getGameStatus(){
        return "{ \"status\": \"ok\"}";
    }


}
