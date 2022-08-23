package com.stettler.scopa.events;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class ScopaEvent extends GameEvent {

    private boolean finalTrick = false;

    public ScopaEvent(boolean finalTrick) {
        this();
        this.finalTrick = finalTrick;
    }

    public ScopaEvent() {
        super(EventType.SCOPA);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public boolean isFinalTrick() {
        return finalTrick;
    }

    public void setFinalTrick(boolean finalTrick) {
        this.finalTrick = finalTrick;
    }
}
