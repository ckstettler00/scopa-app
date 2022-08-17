package com.stettler.scopa.model;

import com.stettler.scopa.model.Card;
import com.stettler.scopa.model.Move;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class Pickup extends Move {
    private Card playerCard;
    private List<Card> tableCards = new ArrayList<>();

    public Pickup() { super(MoveType.PICKUP);}

    public Pickup(Card player, List<Card> pickedUp) {
        this();
        playerCard = player;
        tableCards.addAll(pickedUp);
    }
    public void addCardToPickUp(Card c) {
        tableCards.add(c);
    }

    public Card getPlayerCard() {
        return playerCard;
    }

    public void setPlayerCard(Card playerCard) {
        this.playerCard = playerCard;
    }

    public List<Card> getTableCards() {
        return tableCards;
    }

    public void setTableCards(List<Card> tableCards) {
        this.tableCards = tableCards;
    }

}
