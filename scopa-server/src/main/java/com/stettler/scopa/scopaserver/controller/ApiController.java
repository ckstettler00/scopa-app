package com.stettler.scopa.scopaserver.controller;

import com.stettler.scopa.scopaserver.model.GameEntry;
import com.stettler.scopa.scopaserver.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("scopa2")

public class ApiController {
    @Autowired
    private GameService service;

    @GetMapping(name = "gamelist", produces = "application/json")
    public List<GameEntry> getGameList(){

        return service.getGameList();
    }

}
