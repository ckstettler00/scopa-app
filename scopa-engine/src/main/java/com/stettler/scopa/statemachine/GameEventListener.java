package com.stettler.scopa.statemachine;

import com.stettler.scopa.events.GameEvent;

public interface GameEventListener {
    void notify(GameEvent event);
}
