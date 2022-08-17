package com.stettler.scopa.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Card implements Comparable<Card>{
    private int val = 0;
    private Suit suit= null;
    final int[] primeVal = {16,12,13,14,15,16,18,21,10,10,10};

    public Card(int val, Suit suit) {
        this.val = val;
        this.suit = suit;
    }

    public int getVal() {
        return val;
    }

    public Suit getSuit() {
        return suit;
    }

    @Override
    public String toString() {
        return "com.stettler.scopa.model.Card{" +
                "val=" + val +
                ", suit=" + suit +
                '}';
    }
    public int getPrime(){
        return primeVal[val];
    }
    @Override
    public int compareTo(Card o) {
        return Integer.compare(this.getVal(), o.getVal());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Card card = (Card) o;

        return new EqualsBuilder().append(val, card.val).append(suit, card.suit).append(primeVal, card.primeVal).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(val).append(suit).append(primeVal).toHashCode();
    }
}
