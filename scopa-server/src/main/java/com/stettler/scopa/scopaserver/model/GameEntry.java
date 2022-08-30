package com.stettler.scopa.scopaserver.model;

import com.stettler.scopa.statemachine.State;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.Objects;

public class GameEntry {

    private List<String> playerList;
    private String gameId;

    private String gameState;

    public GameEntry(List<String> playerList, String gameId, State state) {
        this.playerList = playerList;
        this.gameId = gameId;
        this.gameState = state.name();
    }

    public GameEntry() {
    }

    public List<String> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(List<String> playerList) {
        this.playerList = playerList;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameEntry gameEntry = (GameEntry) o;
        return Objects.equals(playerList, gameEntry.playerList) && Objects.equals(gameId, gameEntry.gameId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerList, gameId);
    }

}
