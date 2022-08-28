package com.stettler.scopa.scopaserver.controller;

import com.stettler.scopa.model.GameStatus;
import com.stettler.scopa.scopaserver.model.GameEntry;
import com.stettler.scopa.scopaserver.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/scopa")
public class ApiController {
    @Autowired
    private GameService service;

    @GetMapping(path = "/gamelist", produces = "application/json")
    public List<GameEntry> getGameList(){
        return service.getGameList();
    }
    @GetMapping(path = "/gamelist/{id}", produces = "application/json")
    public List<GameStatus> getGameStatus(@PathVariable String gameId){
        return service.getGameStatus(gameId);
    }

}
