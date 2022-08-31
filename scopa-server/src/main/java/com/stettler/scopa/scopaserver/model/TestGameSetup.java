package com.stettler.scopa.scopaserver.model;

import com.stettler.scopa.model.Card;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class TestGameSetup {

    List<Card> tableCards;
    List<Card> player1Hand;
    List<Card> player2Hand;

    Integer cardsRemaining;

    public Integer getCardsRemaining() {
        return cardsRemaining;
    }

    public void setCardsRemaining(int cardsRemaining) {
        this.cardsRemaining = cardsRemaining;
    }

    public List<Card> getTableCards() {
        return tableCards;
    }

    public void setTableCards(List<Card> tableCards) {
        this.tableCards = tableCards;
    }

    public List<Card> getPlayer1Hand() {
        return player1Hand;
    }

    public void setPlayer1Hand(List<Card> player1Hand) {
        this.player1Hand = player1Hand;
    }

    public List<Card> getPlayer2Hand() {
        return player2Hand;
    }

    public void setPlayer2Hand(List<Card> player2Hand) {
        this.player2Hand = player2Hand;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
