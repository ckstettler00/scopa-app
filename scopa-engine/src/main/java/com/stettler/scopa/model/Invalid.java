package com.stettler.scopa.model;

public class Invalid extends Move {
    Invalid() {
        super(MoveType.INVALID);
    }

    @Override
    public String description() {
        return "Invalid Move.";
    }
}
