package com.stettler.scopa.scopaserver.api;

import com.stettler.scopa.scopaserver.model.GameStatus;
import com.stettler.scopa.scopaserver.model.PlayerDetail;
import com.stettler.scopa.scopaserver.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    @Autowired
    GameService service;

//    // Aggregate root
//    // tag::get-aggregate-root[]
//    @GetMapping("/employees")
//    List<Employee> all() {
//        return repository.findAll();
//    }
    // end::get-aggregate-root[]

    @PostMapping("/newgame")
    GameStatus newEmployee(@RequestBody PlayerDetail player) {
        return service.startNewGame(player);
    }

}
