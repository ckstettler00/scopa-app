package com.stettler.scopa.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Pickup.class, name = "Pickup"),
        @JsonSubTypes.Type(value = Invalid.class, name = "Invalid"),
        @JsonSubTypes.Type(value = Discard.class, name = "Discard")})
public abstract class Move {

    MoveType type = MoveType.INVALID;
    static public Move INVALID = new Invalid();

    public Move() {
    }

    abstract public String description();

    public Move(MoveType type) {
        this.type = type;
    }

    public MoveType getType() {
        return type;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
