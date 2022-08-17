package com.stettler.scopa.events;

import com.stettler.scopa.model.PlayerDetails;

public class GameOverEvent extends GameEvent {

    PlayerDetails winningPlayer;
    int winningScore;

    PlayerDetails losingPlayer;
    int losingScore;

    public GameOverEvent() {
        super(EventType.GAMEOVER);
    }

    public PlayerDetails getWinningPlayer() {
        return winningPlayer;
    }

    public void setWinningPlayer(PlayerDetails winningPlayer) {
        this.winningPlayer = winningPlayer;
    }

    public int getWinningScore() {
        return winningScore;
    }

    public void setWinningScore(int winningScore) {
        this.winningScore = winningScore;
    }

    public PlayerDetails getLosingPlayer() {
        return losingPlayer;
    }

    public void setLosingPlayer(PlayerDetails losingPlayer) {
        this.losingPlayer = losingPlayer;
    }

    public int getLosingScore() {
        return losingScore;
    }

    public void setLosingScore(int losingScore) {
        this.losingScore = losingScore;
    }
}
