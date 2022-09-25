package com.stettler.scopa.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.UUID;

public class PlayerDetails {

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    private String playerId = UUID.randomUUID().toString();
    private String screenHandle;
    private String emailAddr;
    private String playerToken;
    private String playerSecret;

    public String getScreenHandle() {
        return screenHandle;
    }

    public void setScreenHandle(String screenHandle) {
        this.screenHandle = screenHandle;
    }

    public String getEmailAddr() {
        return emailAddr;
    }

    public void setEmailAddr(String emailAddr) {
        this.emailAddr = emailAddr;
    }

    public String getPlayerToken() {
        return playerToken;
    }

    public void setPlayerToken(String playerToken) {
        this.playerToken = playerToken;
    }

    public String getPlayerSecret() {
        return playerSecret;
    }

    public void setPlayerSecret(String playerSecret) {
        this.playerSecret = playerSecret;
    }

    public String getPlayerId() {
        return playerId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;

        if (object == null || getClass() != object.getClass()) return false;

        PlayerDetails that = (PlayerDetails) object;

        return new org.apache.commons.lang3.builder.EqualsBuilder().appendSuper(super.equals(object)).append(screenHandle, that.screenHandle).append(emailAddr, that.emailAddr).append(playerToken, that.playerToken).append(playerSecret, that.playerSecret).isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang3.builder.HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(screenHandle).append(emailAddr).append(playerToken).append(playerSecret).toHashCode();
    }
}
