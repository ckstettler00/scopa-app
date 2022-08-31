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

public class GameControl extends EventSource {

    private static final Integer MAX_PLAYERS = 2;
    private String gameId = UUID.randomUUID().toString();
    Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    State currentState;

    Map<String, EventSource> playerMap = new ConcurrentHashMap<>();
    List<Player> playerOrder = new ArrayList<>();

    Player currentPlayer = null;
    EventSource currentPlayerSource = null;

    Player lastTrickPlayer = null;
    int turnCounter = -1;
    int roundCounter = -1;

    Gameplay gameplay = new Gameplay();

    public GameStatus getStatus(Player player) {
        GameStatus status = new GameStatus();
        status.setGameId(gameId);
        status.setPlayerDetails(player.getDetails());
        status.setTable(this.gameplay.getTableCards());
        status.setCardsRemaining(this.gameplay.getDeck().size());
        status.setPlayerHand(player.getHand());
        status.setCurrentGameState(this.currentState.name());

        // If hands have been dealt then set the opponentCardCount
        if (this.playerOrder.size() >1 && this.currentPlayer!=null) {
            if (this.playerOrder.get(0).getDetails().getPlayerId().
                    equals(player.getDetails().getPlayerId())) {
                status.setOpponentCardCount(this.playerOrder.get(1).getHand().size());
            } else {
                status.setOpponentCardCount(this.playerOrder.get(0).getHand().size());
            }
        }
        if (this.currentPlayer != null) {
            status.setCurrentPlayerId(currentPlayer.getDetails().getPlayerId());
        }
        return status;
    }

    public List<Player> getAllPlayers() {
        return this.playerOrder;
    }

    public Player getPlayer1() {
        if (playerOrder.size() == 0) {
            return null;
        }
        return this.playerOrder.get(0);
    }

    public void setPlayer1(Player p) {
        this.playerOrder.set(0, p);
    }

    public Player getPlayer2() {
        if (playerOrder.size() <= 1) {
            return null;
        }
        return this.playerOrder.get(1);
    }

    public void setPlayer2(Player p) {
        this.playerOrder.set(1, p);
    }

    public State getCurrentState() {
        return currentState;
    }

    public void initializeGame(PlayerDetails details, EventSource source) {
        logger.info("Starting a new game id:{} by player:{}", this.getGameId(), details);
        if (!currentState.equals(State.INIT)) {
            throw new ScopaRuntimeException("Invalid state to create a game:" + this.currentState);
        }
        changeState(State.WAIT_FOR_PLAYER1);

        this.registerPlayer(details, source);
    }

    /**
     * Register the player with the game controller.
     *
     * @param source
     */
    synchronized public boolean registerPlayer(PlayerDetails details, EventSource source) {
        logger.info("Registering Player event source. {}", source);

        if (!currentState.equals(State.WAIT_FOR_PLAYER1) && !currentState.equals(State.WAIT_FOR_PLAYER2)) {
            logger.error("Player {} could not register invalid state {}", details, this.currentState);
            throw new ScopaRuntimeException("Invalid state for registrations:" + this.currentState);
        }

        if (this.playerMap.get(details.getPlayerId()) != null) {
            logger.info("Player {} is already registered.");
            return false;
        }

        if (this.playerOrder.size() >= MAX_PLAYERS) {
            logger.error("Too many players {}", this.playerOrder);
            throw new ScopaRuntimeException("Unexpected exception too many players");
        }

        // Create the association between the player and the source.
        Player p = new Player();
        p.setDetails(details);
        this.playerMap.put(details.getPlayerId(), source);
        this.playerOrder.add(p);

        logger.info("Sending the register event to the controller");
        this.handleRegister(new RegisterEvent(details));

        return true;
    }

    public GameControl() {
        logger.info("Creating new game controller - id {}", gameId);
        currentState = State.INIT;
        this.addHandler(EventType.REGISTER, this::handleRegister);
        this.addHandler(EventType.START_ROUND, this::handleStartRound);
        this.addHandler(EventType.PLAY_RESP, this::handlePlayResponse);
        this.addHandler(EventType.GAMEOVER, this::handleGameOver);
    }

    public String getGameId() {
        logger.info("I hope I see this");
        return gameId;
    }

    /**
     * General event handler.
     *
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
            ScopaException sex = (ScopaException) ex;
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
            Player p = this.playerOrder.get(0);
            setCurrentPlayer(p, this.playerMap.get(p.getDetails().getPlayerId()));
        } else if (next.equals(State.WAIT_4_PLAYER2_MOVE)) {
            Player p = this.playerOrder.get(1);
            setCurrentPlayer(p, this.playerMap.get(p.getDetails().getPlayerId()));
        }
        currentState = next;
    }

    protected void setCurrentPlayer(Player player, EventSource source) {
        logger.info("Play id:{} is now the current player.", player.getDetails().getPlayerId());
        this.currentPlayer = player;
        this.currentPlayerSource = source;
    }

    public Gameplay getGameplay() {
        return gameplay;
    }

    protected Pair<Player, EventSource> lookupPlayer(String id) {
        Player player1 = this.playerOrder.get(0);
        Player player2 = this.playerOrder.get(1);
        if (player1.getDetails().getPlayerId().equals(id)) {
            return Pair.of(player1, this.playerMap.get(player1.getDetails().getPlayerId()));
        }
        if (player2.getDetails().getPlayerId().equals(id)) {
            return Pair.of(player2, this.playerMap.get(player2.getDetails().getPlayerId()));
        }
        throw new PlayerNotFoundException(id);
    }

    /**
     * Determine where to send the message back to given the source id.
     *
     * @param sourceId
     * @return
     */
    protected EventSource lookupSourceBySourceId(String sourceId) {

        for (EventSource s : this.playerMap.values()) {
            if (s.getSourceId().equals(sourceId)) {
                return s;
            }
        }
        logger.error("No matching event source id: {}", sourceId);
        throw new ScopaRuntimeException("No matching source id found.");
    }

    protected void handleGameOver(GameEvent event) {
        Player player1 = this.playerOrder.get(0);
        Player player2 = this.playerOrder.get(1);
        EventSource player1Source = this.playerMap.get(player1.getDetails().getPlayerId());
        EventSource player2Source = this.playerMap.get(player2.getDetails().getPlayerId());

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

        Player player1 = this.playerOrder.get(0);
        Player player2 = this.playerOrder.get(1);
        EventSource player1Source = this.playerMap.get(player1.getDetails().getPlayerId());
        EventSource player2Source = this.playerMap.get(player2.getDetails().getPlayerId());

        logger.info("Received play response: {}", event);
        PlayResponseEvent responseEvent = (PlayResponseEvent) event;

        Pair<Player, EventSource> responsePlayer = lookupPlayer(responseEvent.getPlayerId());
        logger.debug("Found responsePlayer {}", responsePlayer);

        // Check to see if it is a move request from the appropriate player.
        if (!event.getPlayerId().equals(currentPlayer.getDetails().getPlayerId())) {
            logger.error("Detected play out of turn.");
            throw new UnexpectedEventException(event.getPlayerId(), event, "Played out of turn");
        }

        // Make sure we are waiting for a player move.
        if (!Arrays.asList(State.WAIT_4_PLAYER1_MOVE, State.WAIT_4_PLAYER2_MOVE).contains(currentState)) {
            logger.error("Detected invalid move for current state {}", currentState);
            throw new InvalidStateTransitionException(event.getPlayerId(),
                    currentState, event);
        }

        // Verify and create a move.
        Move move = ((PlayResponseEvent) event).getMove();
        if (responseEvent.getMove().getType().equals(MoveType.PICKUP)) {
            logger.info("Playing pickup {}", move);
            gameplay.handlePickup(responsePlayer.getLeft(), (Pickup) move);
            lastTrickPlayer = currentPlayer;

            // Check for scopa
            if (this.gameplay.getTableCards().isEmpty()) {
                if (!this.gameplay.getDeck().hasNext() &&
                        this.playerOrder.get(0).getHand().size() == 0 &&
                        this.playerOrder.get(1).getHand().size() == 0) {
                    logger.info("Scopa ended the game, so no point is awarded");
                    currentPlayerSource.triggerEvent(new ScopaEvent(true));
                } else {
                    logger.info("Player {} got a scopa", currentPlayer);
                    currentPlayerSource.triggerEvent(new ScopaEvent());
                    currentPlayer.setScore(currentPlayer.getScore() + 1);
                }
            }
        } else if (responseEvent.getMove().getType().equals(MoveType.DISCARD)) {
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
        if (this.playerOrder.get(0).getHand().isEmpty() && this.playerOrder.get(1).getHand().isEmpty()) {
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

    protected synchronized void handleRegister(GameEvent event) {
        logger.debug("handleRegister: {}", event);
        if (currentState != State.WAIT_FOR_PLAYER1 &&
                currentState != State.WAIT_FOR_PLAYER2) {
            throw new InvalidStateTransitionException(event.getPlayerId(), currentState, event);
        }

        if (currentState.equals(State.WAIT_FOR_PLAYER1)) {
            changeState(State.WAIT_FOR_PLAYER2);
            return;
        }

        if (currentState.equals(State.WAIT_FOR_PLAYER2)) {
            changeState(State.START_ROUND);
            this.triggerEvent(new StartRoundEvent());
            return;
        }
    }

    protected void sendStatuses() {
        this.playerOrder.forEach(p -> {
            lookupPlayer(p.getDetails().getPlayerId()).getRight().triggerEvent(new GameStatusEvent(getStatus(p)));
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

}
