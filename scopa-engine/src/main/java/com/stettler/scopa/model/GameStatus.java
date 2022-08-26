package com.stettler.scopa.model;

import com.stettler.scopa.statemachine.Player;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class GameStatus {
    private String gameId;
    private Player player;
    private String currentPlayerId;
    private List<Card> table;

    private Integer cardsRemaining;

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

    public String getCurrentPlayerId() {
        return currentPlayerId;
    }

    public void setCurrentPlayerId(String currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

    public Integer getCardsRemaining() {
        return cardsRemaining;
    }

    public void setCardsRemaining(Integer cardsRemaining) {
        this.cardsRemaining = cardsRemaining;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
