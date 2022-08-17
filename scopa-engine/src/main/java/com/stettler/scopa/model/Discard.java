package com.stettler.scopa.model;

public class Discard extends Move {
    public Card getDiscarded() {
        return discarded;
    }

    public void setDiscarded(Card discarded) {
        this.discarded = discarded;
    }

    private Card discarded;

    public Discard() {
        super(MoveType.DISCARD);
    }
    public Discard(Card c) {
        this();
        discarded = c;
    }
}
