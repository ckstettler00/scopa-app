package com.stettler.scopa.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class GameStatus {

    private String currentGameState;
    private String gameId;
    private PlayerDetails playerDetails;

    private String currentPlayerId;
    private List<Card> table;

    private List<Card> playerHand;

    private int opponentCardCount;

    private Integer cardsRemaining;

    public GameStatus() {
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public PlayerDetails getPlayerDetails() {
        return playerDetails;
    }

    public void setPlayerDetails(PlayerDetails playerDetails) {
        this.playerDetails = playerDetails;
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

    public List<Card> getPlayerHand() {
        return playerHand;
    }

    public void setPlayerHand(List<Card> playerHand) {
        this.playerHand = playerHand;
    }

    public int getOpponentCardCount() {
        return opponentCardCount;
    }

    public void setOpponentCardCount(int opponentCardCount) {
        this.opponentCardCount = opponentCardCount;
    }

    public String getCurrentGameState() {
        return currentGameState;
    }

    public void setCurrentGameState(String currentGameState) {
        this.currentGameState = currentGameState;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
