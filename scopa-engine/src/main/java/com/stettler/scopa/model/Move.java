package com.stettler.scopa.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class Move {

    MoveType type = MoveType.INVALID;
    static public Move INVALID = new Invalid();

    public Move() {
    }

    public Move(MoveType type) {
        this.type = type;
    }

    public MoveType getType() {
        return type;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
