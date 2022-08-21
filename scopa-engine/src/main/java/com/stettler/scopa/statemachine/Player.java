package com.stettler.scopa.statemachine;

import com.stettler.scopa.model.Card;
import com.stettler.scopa.model.PlayerDetails;
import com.stettler.scopa.model.Suit;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Player {

    final public static String ALL="all";

    private PlayerDetails details;
    List<Card> hand = new ArrayList<>();
    private int coins = 0;
    private int total = 0;
    private boolean sevenCoins = false;

    private int score = 0;

    private int primesCoin = 0;
    private int primesScepter = 0;
    private int primesSwords = 0;
    private int primesCups = 0;


    public void play(Optional<Card> handCard, List<Card> tableCards){

        List<Card> collection = new ArrayList<>();
        collection.addAll(tableCards);

        if(handCard.isPresent())
            collection.add(handCard.get());

        for(int i = 0; i < collection.size(); i++){
            this.total++;
            if (collection.get(i).getSuit() == Suit.COINS) {
                this.coins++;
                if (collection.get(i).getVal() == 7) {
                    sevenCoins = true;
                }
            }
            if (collection.get(i).getSuit() == Suit.COINS && collection.get(i).getPrime() > primesCoin) {
                primesCoin = collection.get(i).getPrime();
            } else if (collection.get(i).getSuit() == Suit.SCEPTERS && collection.get(i).getPrime() > primesScepter) {
                primesScepter = collection.get(i).getPrime();
            } else if (collection.get(i).getSuit() == Suit.SWORDS && collection.get(i).getPrime() > primesSwords) {
                primesSwords = collection.get(i).getPrime();
            } else if (collection.get(i).getSuit() == Suit.CUPS && collection.get(i).getPrime() > primesCups) {
                primesCups = collection.get(i).getPrime();
            }

        }
        if(handCard.isPresent())
            this.hand.remove(handCard.get());
    }

    public void deal(Card newCard){
        this.hand.add(newCard);
    }
    public void setDetails(PlayerDetails details) {
        this.details = details;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public int getCoins() {
        return coins;
    }
    public int getTotal() {
        return total;
    }
    public PlayerDetails getDetails() {
        return details;
    }
    public List<Card> getHand() {
        return hand;
    }
    public int getScore() {
        return score;
    }
    public boolean isSevenCoins() {
        return sevenCoins;
    }
    public int getPrimesSum(){return (primesCoin + primesScepter + primesSwords + primesCups);}

    public void clearScore(){
        coins = 0;
        total = 0;
        sevenCoins = false;
        primesCoin = 0;
        primesCups = 0;
        primesSwords = 0;
        primesScepter = 0;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
