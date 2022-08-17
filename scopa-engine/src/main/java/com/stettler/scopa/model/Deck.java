package com.stettler.scopa.model;

import com.stettler.scopa.model.Card;
import com.stettler.scopa.model.Suit;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;

public class Deck {
    private Card[] deck = new Card[40];

    private int index = 0;
    public Deck() {
        shuffle();
    }

    public void shuffle(){
        int deckInt;
        int[] deckCheck = new int[40];
        this.index = 0;

        for(int i = 0; i < 40; i++){
            deckCheck[i] = 0;
        }
        for(int i = 0; i < 40; i++){
            deckInt = (int)(Math.random()*(40));
            if(deckCheck[deckInt] == 0){
                deckCheck[deckInt] = 1;
                deck[i] = new Card(faceVal(deckInt), cardSuit(deckInt));
            }
            else{
                i--;
            }

        }

    }
    private int faceVal(int num){
        return (num % 10)+1;
    }

    private Suit cardSuit(int num){
        if (num >= 30){
            return Suit.COINS;
        }
        else if (num >= 20){
            return Suit.CUPS;
        }
        else if (num >= 10){
            return Suit.SWORDS;
        }
        else{
            return Suit.SCEPTERS;
        }
    }

    @Override
    public String toString() {

        return "com.stettler.scopa.model.Deck{" +
                "deck=" + Arrays.toString(deck) +
                '}';
    }

    public Card draw() {
        index++;
        return deck[index-1];
    }

    public boolean hasNext(){
        return (index < 39);
    }

    public int size(){
        return 40-index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Deck deck1 = (Deck) o;

        return new EqualsBuilder().append(index, deck1.index).append(deck, deck1.deck).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(deck).append(index).toHashCode();
    }
}
