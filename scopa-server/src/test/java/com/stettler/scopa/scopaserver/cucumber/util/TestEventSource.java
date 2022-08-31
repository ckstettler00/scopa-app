package com.stettler.scopa.scopaserver.cucumber.util;

import com.stettler.scopa.events.EventType;
import com.stettler.scopa.events.GameEvent;
import com.stettler.scopa.statemachine.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestEventSource extends EventSource {

    Logger logger = LoggerFactory.getLogger(getClass().getName());
    List<GameEvent> events = new ArrayList<>();

    public void clearEvents() {
        events.clear();
    }
    public Optional<GameEvent> waitForEvent(EventType type, int timeout) {
        logger.info("source id:{} Waiting {} ms for event type: {}",this.getSourceId(), timeout, type);
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis()-start < timeout) {
            logger.info("source id:{} wait loop events: {}", this.getSourceId(), events);
            Optional<GameEvent> ret = events.stream()
                    .filter(e -> {
                        logger.info("{} == {} ? {}", e.getEventType(), type, e.getEventType().equals(type));
                        return e.getEventType().equals(type);
                    }).findFirst();
            logger.info("filter response {}", ret);
            if (ret.isPresent()) {
                return ret;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //NOOP
            }
        }
        return Optional.empty();
    }

    @Override
    public void handleEvent(GameEvent event) {
        logger.info("Test Source received event: {}", event);
        events.add(event);
        logger.info("Events: {}", events);
    }

    public TestEventSource() {
        super();
    }
}