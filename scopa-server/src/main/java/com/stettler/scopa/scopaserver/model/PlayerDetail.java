package com.stettler.scopa.scopaserver.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class PlayerDetail {
    private String screenHandle;

    public String getScreenHandle() {
        return screenHandle;
    }

    public void setScreenHandle(String screenHandle) {
        this.screenHandle = screenHandle;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
