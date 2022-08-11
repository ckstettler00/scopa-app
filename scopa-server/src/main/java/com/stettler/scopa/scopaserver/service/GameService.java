package com.stettler.scopa.scopaserver.service;

import com.stettler.scopa.scopaserver.model.GameStatus;
import com.stettler.scopa.scopaserver.model.PlayerDetail;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class GameService {
    public GameStatus startNewGame(PlayerDetail detail) {
        GameStatus status = new GameStatus();
        status.setPlayer1(detail.getScreenHandle());
        status.setPlayer2("disconnected.");
        status.setScore("0");
        return status;
    }
}
