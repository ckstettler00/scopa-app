package com.stettler.scopa.exceptions;

public class PlayerNotFoundException extends ScopaException {
    public PlayerNotFoundException(String playerId) {
        super(playerId, String.format("Player {} was not registered.", playerId));
    }
}
