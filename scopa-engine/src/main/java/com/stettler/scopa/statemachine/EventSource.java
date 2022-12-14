package com.stettler.scopa.statemachine;

import com.stettler.scopa.events.*;
import com.stettler.scopa.exceptions.ScopaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class EventSource {

    String sourceId = UUID.randomUUID().toString();
    Logger logger = LoggerFactory.getLogger(getClass().getName());

    private List<GameEventListener> listeners = new ArrayList<>();

    private LinkedBlockingQueue<GameEvent> eventSource = new LinkedBlockingQueue<>(100);

    private Map<EventType, Consumer<GameEvent>> handlers = new ConcurrentHashMap<>();

    Thread eventLoop = new Thread(this::run, "event-loop-" + sourceId);
    boolean done = false;

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public GameEvent nextEvent() {
        GameEvent event = null;

        logger.debug("Waiting for event");
        while (event == null)
        {
            try {
                event = eventSource.poll(3, TimeUnit.SECONDS);
                if (event != null) {
                    notifyAll(event);
                }
            } catch (InterruptedException e) {
                //NOOP
            }
        }
        logger.debug("Caught event thread:{} event:{}", event);
        return event;
    }

    public void run() {
        logger.debug("entered the run loop");
        while (!done) {
            try {
                GameEvent e = nextEvent();
                if (e instanceof ShutdownEvent) {
                    done = true;
                }
                handleEvent(e);
            } catch(ScopaException ex) {
                logger.error("Caught scopa exception", ex);
            }
        }
        logger.debug("exited the run loop");
    }

    /**
     * Shutdown the event loop
     */
    public void stop() {
        done = true;
        try {
            eventLoop.interrupt();
            eventLoop.join(3000);
        } catch (InterruptedException e) {
            //NOOP
        }
    }

    /**
     * Start the event loop
     */
    public void start() {
        eventLoop.start();
    }

    /**
     * Add an event to the queue.
     * @param event
     */
    public void triggerEvent(GameEvent event) {
        try {
            logger.debug("Setting the events source id {}", this.getSourceId());
            event.setFromSourceId(this.getSourceId());
            logger.debug("triggering event {}", event);
            this.eventSource.put(event);
        } catch (InterruptedException e) {
            //NOOP
        }
    }

    public void addHandler(EventType event, Consumer<GameEvent> handler) {
        this.handlers.put(event, handler);
    }

    public void handleEvent(GameEvent event) {
        logger.debug("Received event: {}", event);
        try {
            handlers.getOrDefault(event.getEventType(), this::handleUnknownEvent).accept(event);
        }
        catch (Exception ex) {
            logger.error("Event Loop caught exception:", ex);
            handleException(ex);
        }
    }

    public void handleUnknownEvent(GameEvent event) {
        logger.error("No handler exists for event: {}", event);
    }

    public void handleException(Exception ex) {
        //Eat the exception
    }

    public void addListener(GameEventListener listener) {
        this.listeners.add(listener);
    }

    private void notifyAll(final GameEvent event) {
        try {
            this.listeners.forEach(n -> n.notify(event));
        } catch (Exception e) {
            logger.error("Notification failed and ignored", e);
        }
    }

}
