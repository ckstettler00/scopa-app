package com.stettler.scopa.statemachine;

import com.stettler.scopa.exceptions.*;
import com.stettler.scopa.events.*;
import com.stettler.scopa.model.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class GameControl extends EventSource {

    private String gameId = UUID.randomUUID().toString();
    Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    State currentState;

    Map<EventType, Consumer<GameEvent>> handlers = new ConcurrentHashMap<>();

    EventSource player1Source=null;
    EventSource player2Source=null;
    Player player1 = null;
    Player player2 = null;
    Player currentPlayer = null;
    EventSource currentPlayerSource = null;

    Player lastTrickPlayer = null;
    int turnCounter = -1;
    int roundCounter= -1;

    Gameplay gameplay = null;

    public GameStatus getStatus(Player player) {
        GameStatus status = new GameStatus();
        status.setGameId(gameId);
        status.setPlayer(player);
        status.setTable(this.gameplay.getTableCards());
        status.setDeck(this.gameplay.getDeck());

        if (this.currentPlayer != null) {
            status.setCurrentPlayerId(currentPlayer.getDetails().getPlayerId());
        }
        return status;
    }
    /**
     * Register the player with the game controller.
     * @param source
     */
    public void registerPlayer1Source(EventSource source) {
        logger.info("Registering Player1 event source. {}", source);
        this.player1Source = source;
    }

    /**
     * Register the player2 with the game controller.
     * @param source
     */
    public void registerPlayer2Source(EventSource source) {
        logger.info("Registering Player2 event source. {}", source);
        this.player2Source = source;
    }

    public GameControl() {
        logger.info("Creating new game controller - id {}", gameId);
        currentState = State.INIT;
        this.addHandler(EventType.REGISTER, this::handleRegister);
        this.addHandler(EventType.NEWGAME, this::handleNewGame);
        this.addHandler(EventType.START_ROUND, this::handleStartRound);
        this.addHandler(EventType.PLAY_RESP, this::handlePlayResponse);
        this.addHandler(EventType.GAMEOVER, this::handleGameOver);
    }

    /**
     * General event handler.
     * @param event
     */
    @Override
    public void handleEvent(GameEvent event) {
        logger.debug("Received event: {} State:{}", event, currentState);
        super.handleEvent(event);
    }

    @Override
    public void handleException(Exception ex) {
        if (ex instanceof ScopaException) {
            ScopaException sex = (ScopaException)ex;
            logger.error("ScopaExcepton:", sex);
            Pair<Player, EventSource> pair = lookupPlayer(sex.getPlayerId());
            if (pair != null) {
                logger.info("Notifying player: {} of the error.", sex.getPlayerId());
                pair.getRight().triggerEvent(new ErrorEvent(sex.getPlayerId(), ex.getMessage()));
            }

            if (ex instanceof InvalidMoveException) {
                logger.error("Re-requesting the player to move: {}", currentPlayer);
                currentPlayerSource.triggerEvent(new PlayRequestEvent(currentPlayer.getDetails()));
            }
        } else {
            logger.error("Unhandled exception detected", ex);
        }

    }

    protected void changeState(State next) {
        logger.info("changeState New State: {} Old State:{}", next, currentState);
        if (next.equals(State.WAIT_4_PLAYER1_MOVE)) {
            setCurrentPlayer(player1, player1Source);
        }
        if (next.equals(State.WAIT_4_PLAYER2_MOVE)) {
            setCurrentPlayer(player2, player2Source);
        }
        currentState = next;
    }

    protected void setCurrentPlayer(Player player, EventSource source) {
        logger.info("Play id:{} is now the current player.", player.getDetails().getPlayerId());
        this.currentPlayer = player;
        this.currentPlayerSource = source;
    }

    protected Pair<Player, EventSource> lookupPlayer(String id) {
        if (player1.getDetails().getPlayerId().equals(id)) {
            return Pair.of(player1, player1Source);
        }
        if (player2.getDetails().getPlayerId().equals(id)) {
            return Pair.of(player2, player2Source);
        }
        throw new PlayerNotFoundException(id);
    }
    protected void handleGameOver(GameEvent event) {

        GameOverEvent overEvent = new GameOverEvent();
        overEvent.setWinningPlayer(player1.getDetails());
        overEvent.setWinningScore(player1.getScore());
        overEvent.setLosingPlayer(player2.getDetails());
        overEvent.setLosingScore(player2.getScore());

        if (player2.getScore() > player1.getScore()) {
            overEvent.setWinningPlayer(player2.getDetails());
            overEvent.setWinningScore(player2.getScore());
            overEvent.setLosingPlayer(player1.getDetails());
            overEvent.setLosingScore(player1.getScore());
        }

        // Send status updates for score.
        sendStatuses();

        // Send game over event
        player1Source.triggerEvent(overEvent);
        player2Source.triggerEvent(overEvent);
    }
    protected void handlePlayResponse(GameEvent event) {
        logger.info("Received play response: {}", event);
        PlayResponseEvent responseEvent = (PlayResponseEvent)event;

        Pair<Player, EventSource> responsePlayer = lookupPlayer(responseEvent.getPlayerId());
        logger.debug("Found responsePlayer {}", responsePlayer);

        // Check to see if it is a move request from the appropriate player.
        if (!event.getPlayerId().equals(currentPlayer.getDetails().getPlayerId())) {
            logger.error("Detected play out of turn.");
            throw new UnexpectedEventException(event.getPlayerId(),event, "Played out of turn");
        }

        // Make sure we are waiting for a player move.
        if (!Arrays.asList(State.WAIT_4_PLAYER1_MOVE, State.WAIT_4_PLAYER2_MOVE).contains(currentState)) {
            logger.error("Detected invalid move for current state {}", currentState);
            throw new InvalidStateTransitionException(event.getPlayerId(),
                        currentState, event);
        }

         // Verify and create a move.
        Move move = ((PlayResponseEvent)event).getMove();
        if (responseEvent.getMove().getType().equals(MoveType.PICKUP)) {
            logger.info("Playing pickup {}", move);
            gameplay.handlePickup(responsePlayer.getLeft(), (Pickup) move);
            lastTrickPlayer = currentPlayer;

            // Check for scopa
            if (this.gameplay.getTableCards().isEmpty()) {
                logger.info("Player {} got a scopa", currentPlayer);

                currentPlayerSource.triggerEvent(new ScopaEvent());
                currentPlayer.setScore(currentPlayer.getScore()+1);
            }
        } else if (responseEvent.getMove().getType().equals(MoveType.DISCARD)){
            logger.info("Received discard play {}", move);
            gameplay.handleDiscard(responsePlayer.getLeft(),(Discard) move);
        } else {
            // Bad play so re-ask for a valid move.
            logger.error("An invalid play was attempted {}", move);
            responsePlayer.getRight().triggerEvent(new PlayRequestEvent(responsePlayer.getLeft().getDetails()));
            throw new InvalidMoveException(responsePlayer.getLeft().getDetails().getPlayerId(), move);
        }


        turnCounter++;

        // End of round tallies
        if (this.player2.getHand().isEmpty() && this.player1.getHand().isEmpty()) {
            logger.info("Hand was complete. Deck length {}", this.gameplay.getDeck().size());
            if (this.gameplay.getDeck().size() == 0) {
                logger.info("Detected end of round {}", roundCounter);

                this.gameplay.trackScore(player1, player2, lastTrickPlayer);

                if (this.gameplay.winner(player1, player2)) {
                    logger.info("Detected a winner.");
                    changeState(State.WINNER);
                } else {
                    logger.info("Detected end of round.");
                    // If we have exhausted the deck then trigger end of round
                    changeState(State.START_ROUND);
                }

            } else {
                logger.info("Deal next hand.  Turn Count={} Round Count={}", turnCounter, roundCounter);
                this.dealHands();
            }
        }

        if (currentState.equals(State.WINNER)) {
            logger.info("Game {} is over sending event.", this.gameId);
            this.triggerEvent(new GameOverEvent());
        } else if (currentState.equals(State.START_ROUND)) {
            logger.info("Sending start round event game {}", this.gameId);
            this.triggerEvent(new StartRoundEvent());
        } else if (currentState.equals(State.WAIT_4_PLAYER1_MOVE)) {

            logger.info("Send status updates and switch to player2");
            // Send a request for player 2 to play.
            changeState(State.WAIT_4_PLAYER2_MOVE);
            sendStatuses();
            player2Source.triggerEvent(new PlayRequestEvent(player2.getDetails()));

        } else if (currentState.equals(State.WAIT_4_PLAYER2_MOVE)) {
            logger.info("Send status updates and switch to player1.");
            // Send a request for player 1 to play.
            changeState(State.WAIT_4_PLAYER1_MOVE);
            sendStatuses();
            player1Source.triggerEvent(new PlayRequestEvent(player1.getDetails()));
        }
    }

    protected void handleStartRound(GameEvent event) {
        logger.debug("handleStartRound {}", event);
        this.roundCounter++;

        if (!currentState.equals(State.START_ROUND)) {
            logger.error("Invalid event {} for state: {}", event, currentState);
            throw new InvalidStateTransitionException(event.getPlayerId(), currentState, event);
        }

        this.gameplay.shuffle();

        // Deal cards to the table.
        this.gameplay.deal();
        this.dealHands();

        // Players take turns going first.  Player 1 has odd rounds
        if (roundCounter % 2 == 0) {
            changeState(State.WAIT_4_PLAYER1_MOVE);
            turnCounter = 0;
        } else {
            changeState(State.WAIT_4_PLAYER2_MOVE);
            turnCounter = 1;
        }

        // Update the players
        sendStatuses();

        // Send a request for player to play.
        logger.debug("waiting for player id: {} handle:{} to move", currentPlayer.getDetails().getPlayerId(),
                currentPlayer.getDetails().getScreenHandle());
        currentPlayerSource.triggerEvent(new PlayRequestEvent(currentPlayer.getDetails()));
    }
    protected void handleRegister(GameEvent event) {
        logger.debug("handleRegister: {}", event);
        if (currentState != State.WAIT_FOR_PLAYER1 &&
        currentState != State.WAIT_FOR_PLAYER2)
        {
            throw new InvalidStateTransitionException(event.getPlayerId(), currentState, event);
        }

        if (currentState.equals(State.WAIT_FOR_PLAYER1)) {
            player1 = new Player();
            player1.setDetails(((RegisterEvent) event).getDetails());
            changeState(State.WAIT_FOR_PLAYER2);
            return;
        }

        if (currentState.equals(State.WAIT_FOR_PLAYER2)) {
            player2 = new Player();
            player2.setDetails(((RegisterEvent) event).getDetails());
            changeState(State.START_ROUND);
            this.triggerEvent(new StartRoundEvent());
            return;
        }
    }

    protected void handleNewGame(GameEvent event) {
        logger.debug("handleNewGame: {}", event);
        this.gameplay = new Gameplay();
        changeState(State.WAIT_FOR_PLAYER1);
        return;
    }

    protected void sendStatuses() {
        player1Source.triggerEvent(new GameStatusEvent(getStatus(player1)));
        player2Source.triggerEvent(new GameStatusEvent(getStatus(player2)));
    }

    public void waitForGameComplete() {
        logger.debug("waiting for the game to complete");
        while (!currentState.equals(State.WINNER)) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                //NOOP
            }
        }
    }

    private void dealHands() {
        // Each player gets 3 cards.
        for (int i = 0; i < 3; i++) {
            this.gameplay.deal(player1);
            this.gameplay.deal(player2);
        }
    }

}
