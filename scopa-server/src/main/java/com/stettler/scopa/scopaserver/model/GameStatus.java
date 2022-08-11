package com.stettler.scopa.scopaserver.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.UUID;

public class GameStatus {
    private String gameId = UUID.randomUUID().toString();
    private String score;
    private String player1;
    private String player2;

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
