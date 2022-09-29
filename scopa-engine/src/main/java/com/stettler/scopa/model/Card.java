package com.stettler.scopa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stettler.scopa.exceptions.ScopaException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Card implements Comparable<Card> {
    private int val = 0;
    private Suit suit = null;
    final int[] primeVal = {16, 12, 13, 14, 15, 18, 21, 10, 10, 10};

    public Card() {
    }


    public Card(int val, Suit suit) {
        if (val < 1 || val > 10) {
            throw new ScopaException("Invalid card value");
        }
        this.val = val;
        this.suit = suit;
    }

    public int getVal() {
        return val;
    }

    public Suit getSuit() {
        return suit;
    }

    public String shortString() {
        return String.format("%s(%d)", this.getSuit(), this.val);
    }

    @Override
    public String toString() {
        return "Card{" +
                "val=" + val +
                ", suit=" + suit +
                '}';
    }

    @JsonIgnore
    public int getPrime(){
        return primeVal[val - 1];
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
