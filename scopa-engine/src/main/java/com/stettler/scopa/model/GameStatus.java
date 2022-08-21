package com.stettler.scopa.model;

import com.stettler.scopa.statemachine.Player;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class GameStatus {
    private String gameId;
    private Player player;

    private String currentPlayerId;
    private Deck deck;

    private List<Card> table;

    public GameStatus() {
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public List<Card> getTable() {
        return table;
    }

    public void setTable(List<Card> table) {
        this.table = table;
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public String getCurrentPlayerId() {
        return currentPlayerId;
    }

    public void setCurrentPlayerId(String currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
