package com.stettler.scopa.statemachine;

import com.stettler.scopa.events.*;
import com.stettler.scopa.exceptions.*;
import com.stettler.scopa.model.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class GameControl2 {

    private Integer MAX_PLAYERS = 2;
    private String gameId = UUID.randomUUID().toString();
    Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    Map<String, Pair<Player, EventSource>> playerMap = new ConcurrentHashMap<>();
    List<Player> playerOrder = new ArrayList<>();

    State currentState;

    Player currentPlayer = null;
    EventSource currentPlayerSource = null;

    Player lastTrickPlayer = null;
    int turnCounter = -1;
    int roundCounter = -1;

    Gameplay gameplay = new Gameplay();

    public GameControl2() {
        logger.info("Creating new game controller - id {}", gameId);
        currentState = State.INIT;
    }

    synchronized public GameStatus getStatus(Player player) {
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
     *
     * @param source
     */
    synchronized public boolean registerPlayer(PlayerDetails details, EventSource source) {

        if (!currentState.equals(State.INIT)) {
            throw new InvalidStateException("Attempt to register but not in WAIT_FOR_REGISTRATION", this.currentState);
        }

        logger.info("Registration requested {}", details.toShortString());
        if (this.playerMap.size() >= MAX_PLAYERS) {
            logger.error("Game is full {}", playerMap);
            throw new GameFullException();
        }

        if (playerMap.get(details.getPlayerId()) != null) {
            logger.info("{} is already registered", details.toShortString());
        } else {
            logger.info("Adding new {}", details.toShortString());
            Player p = new Player();
            p.setDetails(details);
            this.playerMap.put(details.getPlayerId(), Pair.of(p, source));
            this.playerOrder.add(p);
        }

        /**
         * Start the first round.
         */
        if (playerMap.size() == 2 && currentState == State.INIT) {
            this.startRound();
            return true;
        } else {
            logger.info("Current State: {} Game {} is not ready to start. Current # registered is {}",
                    currentState, this.gameId, this.playerOrder.size());
            return false;
        }

    }

    /**
     * Returns the system generated unique id of the game.
     *
     * @return
     */
    public String getGameId() {
        return gameId;
    }


    /**
     * Perform state transitions as required.
     *
     * @param next
     */
    synchronized protected void changeState(State next) {
        logger.info("changeState New State: {} Old State:{}", next, currentState);
        if (next.equals(State.WAIT_4_PLAYER1_MOVE)) {
            setCurrentPlayer(0);
        }
        if (next.equals(State.WAIT_4_PLAYER2_MOVE)) {
            setCurrentPlayer(1);
        }
        currentState = next;
    }

    synchronized protected void setCurrentPlayer(Integer order) {
        logger.info("Play index:{} is now the current player.", order);
        Player p = this.playerOrder.get(order);

        logger.info("Current player details {}", p.getDetails());
        this.currentPlayer = p;
        this.currentPlayerSource = lookupPlayer(p.getDetails().getPlayerId()).getRight();
    }

    synchronized protected Pair<Player, EventSource> lookupPlayer(String id) {
        Pair<Player, EventSource> p = this.playerMap.get(id);
        if (p == null) {
            logger.error("Invalid player lookup requested id: {}", id);
            throw new PlayerNotFoundException(id);
        }
        return p;
    }

    synchronized protected void gameOver() {
        changeState(State.WINNER);
        Player player1 = this.playerOrder.get(0);
        Player player2 = this.playerOrder.get(1);

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

        // Send game over event to all players.
        this.playerMap.forEach((k, v) -> {
            v.getRight().triggerEvent(overEvent);
        });
    }

    synchronized protected void play(String playerId, Move move) {
        logger.info("Received play from id:{} move:{}", playerId, move);

        Pair<Player, EventSource> responsePlayer = lookupPlayer(playerId);
        Player player1 = this.playerOrder.get(0);
        Player player2 = this.playerOrder.get(1);
        EventSource player1Source = lookupPlayer(player1.getDetails().getPlayerId()).getRight();
        EventSource player2Source = lookupPlayer(player2.getDetails().getPlayerId()).getRight();

        // Check to see if it is a move request from the appropriate player.
        if (!playerId.equals(currentPlayer.getDetails().getPlayerId())) {
            logger.error("Detected play out of turn.");
            throw new ScopaException(playerId, "Played out of turn");
        }

        // Make sure we are waiting for a player move.
        if (!Arrays.asList(State.WAIT_4_PLAYER1_MOVE, State.WAIT_4_PLAYER2_MOVE).contains(currentState)) {
            logger.error("Detected invalid move for current state {}", currentState);
            throw new InvalidStateException("Move request but not a move date",
                    currentState);
        }

         // Verify and create a move.
        if (move.getType().equals(MoveType.PICKUP)) {
            logger.info("Playing pickup {}", move);
            gameplay.handlePickup(responsePlayer.getLeft(), (Pickup) move);
            lastTrickPlayer = currentPlayer;

            // Check for scopa
            if (this.gameplay.getTableCards().isEmpty()) {
                if (!this.gameplay.getDeck().hasNext() && player1.getHand().size() == 0 && player2.getHand().size() == 0) {
                    logger.info("Scopa ended the game, so no point is awarded");
                    currentPlayerSource.triggerEvent(new ScopaEvent(true));
                } else {
                    logger.info("Player {} got a scopa", currentPlayer);
                    currentPlayerSource.triggerEvent(new ScopaEvent());
                    currentPlayer.setScore(currentPlayer.getScore() + 1);
                }
            }
        } else if (move.getType().equals(MoveType.DISCARD)) {
            logger.info("Received discard play {}", move);
            gameplay.handleDiscard(responsePlayer.getLeft(), (Discard) move);
        } else {
            // Bad play so re-ask for a valid move.
            logger.error("An invalid play was attempted {}", move);
            responsePlayer.getRight().triggerEvent(new PlayRequestEvent(responsePlayer.getLeft().getDetails()));
            throw new InvalidMoveException(responsePlayer.getLeft().getDetails().getPlayerId(), move);
        }


        turnCounter++;

        // End of round tallies
        if (player2.getHand().isEmpty() && player1.getHand().isEmpty()) {
            logger.info("Hand was complete. Deck length {}", this.gameplay.getDeck().size());
            if (this.gameplay.getDeck().size() == 0) {
                logger.info("Detected end of round {}", roundCounter);

                this.gameplay.trackScore(player1, player2, lastTrickPlayer);

                if (this.gameplay.winner(player1, player2)) {
                    logger.info("Detected a winner.");
                    gameOver();
                    return;
                } else {
                    logger.info("End of round not the end of game.");
                    // If we have exhausted the deck then trigger end of round
                    startRound();
                    return;
                }

            } else {
                logger.info("Deal next hand.  Turn Count={} Round Count={}", turnCounter, roundCounter);
                this.dealHands();
            }
        }

        if (currentState.equals(State.WAIT_4_PLAYER1_MOVE)) {

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

    /**
     * Starts a new game round.  Each round the player that moves first will flip flop.
     */
    protected void startRound() {

        this.roundCounter++;

        logger.info("Staring round {}", this.roundCounter);

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

    synchronized protected void sendStatuses() {
        playerMap.forEach((k, v) -> {
            try {
                v.getRight().triggerEvent(new GameStatusEvent(getStatus(v.getLeft())));
            } catch (ScopaRuntimeException e) {
                logger.error("caught unexpected exception", e);
            }
        });
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
            this.gameplay.deal(this.playerOrder.get(0));
            this.gameplay.deal(this.playerOrder.get(1));
        }
    }

    protected Player getPlayer1() {
        return this.playerOrder.get(0);
    }

    protected Player getPlayer2() {
        return this.playerOrder.get(1);
    }

}
