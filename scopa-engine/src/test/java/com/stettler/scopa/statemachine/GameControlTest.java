package com.stettler.scopa.statemachine;

import com.stettler.scopa.events.*;
import com.stettler.scopa.exceptions.InvalidStateTransitionException;
import com.stettler.scopa.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

public class GameControlTest {
    Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static class TestSource extends EventSource {
        private final List<GameEvent> events = new ArrayList<>();

        public List<GameEvent> getEvents() {
            return events;
        }

        @Override
        public void handleEvent(GameEvent event) {
            events.add(event);
        }
    }

    GameControl control = new GameControl();
    TestSource player1 = new TestSource();
    TestSource player2 = new TestSource();

    String player1Id = null;
    String player2Id = null;

    @AfterEach
    void tearDown() {
        player1.triggerEvent(new ShutdownEvent());
        player2.triggerEvent(new ShutdownEvent());
        control.triggerEvent(new ShutdownEvent());
    }

    @BeforeEach
    void setup() {

        control = new GameControl();
        player1 = new TestSource();
        player2 = new TestSource();

        player1.start();
        player2.start();
        control.start();
    }

    @Test
    void testEmptyHands() throws Exception {
        newGameAndRegistration();

        // Verify counters and score before sending first event
        assertThat(control.gameplay.deck.size()).isEqualTo(30);
        assertThat(control.roundCounter).isEqualTo(0);
        assertThat(control.getPlayer1().getScore()).isEqualTo(0);
        assertThat(control.getPlayer2().getScore()).isEqualTo(0);

        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS), new Card(2, Suit.COINS),
                new Card(3, Suit.COINS), new Card(4, Suit.COINS)));
        control.getPlayer1().hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.CUPS)));
        control.getPlayer2().hand = new ArrayList<>(Arrays.asList(new Card(2, Suit.SWORDS)));

        // Player 1 uses last card.
        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(1, Suit.CUPS),
                new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS)))), this.control.getGameId()));

        // Verify key counts and scores have not changed.
        assertThat(control.gameplay.deck.size()).isEqualTo(30);
        assertThat(control.roundCounter).isEqualTo(0);
        assertThat(control.getPlayer1().getScore()).isEqualTo(0);
        assertThat(control.getPlayer2().getScore()).isEqualTo(0);

        // Should be player 2s turn
        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // Use player2 last card.
        triggerEvent(control, new PlayResponseEvent(player2Id, new Pickup(new Card(2, Suit.SWORDS),
                new ArrayList<>(Arrays.asList(new Card(2, Suit.COINS)))), this.control.getGameId()));

        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);

        // This should have triggered a deal of 6 cards. 3 for each.  The table
        // should not change.
        assertThat(control.gameplay.deck.size()).isEqualTo(24);

        // Scores should not be affected since the round did not increment and no scopa.
        assertThat(control.roundCounter).isEqualTo(0);
        assertThat(control.getPlayer1().getScore()).isEqualTo(0);
        assertThat(control.getPlayer2().getScore()).isEqualTo(0);

        assertThat(control.gameplay.tableCards).containsExactly(
                new Card(3, Suit.COINS), new Card(4, Suit.COINS));
        assertThat(control.getPlayer1().getHand()).hasSize(3);
        assertThat(control.getPlayer2().getHand()).hasSize(3);


    }

    @Test
    void testLastTrick() throws Exception {
        newGameAndRegistration();
        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(9, Suit.COINS), new Card(2, Suit.COINS),
                new Card(3, Suit.COINS), new Card(4, Suit.COINS)));
        control.getPlayer1().hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.CUPS), new Card(2, Suit.CUPS),
                new Card(3, Suit.CUPS), new Card(9, Suit.CUPS)));
        control.getPlayer2().hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.SWORDS), new Card(2, Suit.SWORDS),
                new Card(3, Suit.SWORDS), new Card(4, Suit.SWORDS)));

        // Playing a discard should not switch the lastTrick state.
        triggerEvent(control, new PlayResponseEvent(player1Id, new Discard(new Card(10, Suit.CUPS)), this.control.getGameId()));
        assertThat(this.control.lastTrickPlayer).isNull();

        // Confirming the move is player2 before proceeding.
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // Playing a pickup should switch the lastTrick indicator.
        triggerEvent(control, new PlayResponseEvent(player2Id, new Pickup(new Card(4, Suit.SWORDS),
                Arrays.asList(new Card(4, Suit.COINS))), this.control.getGameId()));
        // Check to see if the deck was updated in the status.
        assertThat(this.control.lastTrickPlayer.getDetails().getPlayerId()).isEqualTo(this.player2Id);

        // Confirming the move is player1 before proceeding.
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);

        // Playing a pickup should switch the lastTrick indicator.
        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(3, Suit.CUPS),
                Arrays.asList(new Card(3, Suit.COINS))), this.control.getGameId()));

        assertThat(this.control.lastTrickPlayer.getDetails().getPlayerId()).isEqualTo(this.player1Id);

    }

    @Test
    void testPlayOutOfTurn() throws Exception {
        newGameAndRegistration();
        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS), new Card(2, Suit.COINS),
                new Card(3, Suit.COINS), new Card(4, Suit.COINS)));
        control.getPlayer1().hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.CUPS), new Card(2, Suit.CUPS)));
        control.getPlayer2().hand = new ArrayList<>(Arrays.asList(new Card(2, Suit.SWORDS), new Card(1, Suit.SWORDS)));

        // Should be player 1s turn
        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);

        // Player 1 is up but player2 is playing
        triggerEvent(control, new PlayResponseEvent(player2Id, new Pickup(new Card(1, Suit.SWORDS),
                new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS)))), this.control.getGameId()));

        // Player2 will receive error for playing out of turn.
        assertThat(player2.getEvents().get(0)).isInstanceOf(ErrorEvent.class);
        ErrorEvent error = (ErrorEvent) player2.getEvents().get(0);
        assertThat(error.getMessage()).contains("Played out of turn");

        // Should be player 1s turn still.
        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);

        // Player 1 is up so play a card.
        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(1, Suit.CUPS),
                new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS)))), this.control.getGameId()));

        // Should be player 2s turn
        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // Player 2 is up but let player1 try to play.
        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(1, Suit.CUPS),
                new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS)))), this.control.getGameId()));

        // Player 1 will get an illegal play error.
        assertThat(player1.getEvents().get(0)).isInstanceOf(ErrorEvent.class);
        error = (ErrorEvent) player1.getEvents().get(0);
        assertThat(error.getMessage()).contains("Played out of turn");

        // Player 2 is still up.
        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // Make a valid player 2 play
        triggerEvent(control, new PlayResponseEvent(player2Id, new Pickup(new Card(2, Suit.SWORDS),
                new ArrayList<>(Arrays.asList(new Card(2, Suit.COINS)))), this.control.getGameId()));

        // Should be player 1s turn
        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);

        assertThat(this.control.turnCounter).isEqualTo(2);
        assertThat(this.control.getPlayer1().getHand()).hasSize(1);
        assertThat(this.control.getPlayer2().getHand()).hasSize(1);

    }

    @Test
    void testEndOfRound() throws Exception {
        newGameAndRegistration();

        // Have player1 and play2 two simulate the last discard of the deck
        // which triggers the end of a round.
        this.playAndVerifyOneRound();

        // The round should have incremented.
        assertThat(this.control.roundCounter).isEqualTo(1);

        // Confirming the move is player2 before proceeding.
        // the first player changes with each round.  Should be player2 now.
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // A new set of table cards would be dealt.
        assertThat(this.control.gameplay.tableCards).hasSize(4);

        // Deck would have re-dealt
        assertThat(this.control.gameplay.deck.size()).isEqualTo(30);
        assertThat(this.control.getPlayer1().getHand()).hasSize(3);
        assertThat(this.control.getPlayer2().getHand()).hasSize(3);

    }

    /**
     * You can end the game on a scopa but the point does not count.
     */
    @Test
    void testEndOfRoundScopaDoesNotCountForPlayer2() throws Exception {
        newGameAndRegistration();
        //Setup game play for player2 to go last

        control.changeState(State.WAIT_4_PLAYER2_MOVE);
        control.turnCounter = 1;
        for (int i = 0; i < 30; i++) {
            this.control.gameplay.deck.draw();
        }

        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS), new Card(2, Suit.COINS),
                new Card(3, Suit.COINS), new Card(4, Suit.COINS)));
        control.getPlayer1().hand = new ArrayList<>();
        control.getPlayer2().hand = new ArrayList<>(Arrays.asList(new Card(10, Suit.CUPS)));

        triggerEvent(control, new PlayResponseEvent(player2Id, new Pickup(new Card(10, Suit.CUPS),
                new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS), new Card(2, Suit.COINS),
                        new Card(3, Suit.COINS), new Card(4, Suit.COINS)))), this.control.getGameId()));

        assertThat(player2.getEvents().get(0)).isInstanceOf(ScopaEvent.class);
        assertThat(player2.getEvents().get(0).getEventType()).isEqualTo(EventType.SCOPA);
        assertThat(((ScopaEvent) player2.getEvents().get(0)).isFinalTrick()).isTrue();
        assertThat(control.gameplay.tableCards).hasSize(4);
        assertThat(control.getPlayer1().getScore()).isEqualTo(0);
        assertThat(control.getPlayer2().getScore()).isEqualTo(1);//ONE POINT ALREADY AWARDED FOR HIGHEST PRIME

        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);
        assertThat(control.turnCounter).isEqualTo(1);
        assertThat(control.roundCounter).isEqualTo(1);
    }

    void testEndOfRoundScopaDoesNotCountForPlayer1() throws Exception {
        newGameAndRegistration();
        //Setup game play for player1 to go last

        control.changeState(State.WAIT_4_PLAYER1_MOVE);
        control.turnCounter = 1;
        for (int i = 0; i < 30; i++) {
            this.control.gameplay.deck.draw();
        }

        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS), new Card(2, Suit.COINS),
                new Card(3, Suit.COINS), new Card(4, Suit.COINS)));
        control.getPlayer2().hand = new ArrayList<>();
        control.getPlayer1().hand = new ArrayList<>(Arrays.asList(new Card(10, Suit.CUPS)));

        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(10, Suit.CUPS),
                new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS), new Card(2, Suit.COINS),
                        new Card(3, Suit.COINS), new Card(4, Suit.COINS)))), this.control.getGameId()));

        assertThat(player1.getEvents().get(0)).isInstanceOf(ScopaEvent.class);
        assertThat(player1.getEvents().get(0).getEventType()).isEqualTo(EventType.SCOPA);
        assertThat(((ScopaEvent) player1.getEvents().get(0)).isFinalTrick()).isTrue();
        assertThat(control.gameplay.tableCards).hasSize(4);
        assertThat(control.getPlayer2().getScore()).isEqualTo(0);
        assertThat(control.getPlayer1().getScore()).isEqualTo(1);//ONE POINT ALREADY AWARDED FOR HIGHEST PRIME

        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);
        assertThat(control.turnCounter).isEqualTo(1);
        assertThat(control.roundCounter).isEqualTo(1);
    }

    @Test
    void testEndOfRoundLastTrickLogicPlayer2() throws Exception {
        newGameAndRegistration();

        // Empty deck to simulate drawing cards.
        while (this.control.gameplay.deck.hasNext()) {
            this.control.gameplay.deck.draw();
        }

        // Setup some coins to be on the table.
        this.control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS),
                new Card(2, Suit.COINS), new Card(7, Suit.COINS), new Card(1, Suit.SWORDS)));

        Player player2Spy = Mockito.spy(this.control.getPlayer2());
        this.control.setPlayer2(player2Spy);

        // Setup to look like player 2 move.
        this.control.changeState(State.WAIT_4_PLAYER2_MOVE);
        assertThat(this.control.getPlayer2().getCoins()).isEqualTo(0);
        assertThat(this.control.getPlayer2().isSevenCoins()).isFalse();

        // Put one card in player2 hand and zero in player 1 to simulate last card of the round.
        this.control.getPlayer2().hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.CUPS)));
        this.control.getPlayer1().hand = new ArrayList<>();

        // Play the card.
        this.control.handlePlayResponse(new PlayResponseEvent(player2Id, new Pickup(new Card(1, Suit.CUPS),
                Arrays.asList(new Card(1, Suit.SWORDS))), this.control.getGameId()));

        // There is some back ground threading that needs to happen
        // so cheat and sleep a little.
        Thread.sleep(500);

        assertThat(this.player2.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);

        assertThat(this.control.roundCounter).isEqualTo(1);
        Mockito.verify(player2Spy, times(1)).play(Optional.of(new Card(1, Suit.CUPS)),
                Arrays.asList(new Card(1, Suit.SWORDS)));
        Mockito.verify(player2Spy, times(1)).play(Optional.empty(),
                Arrays.asList(new Card(1, Suit.COINS),
                        new Card(2, Suit.COINS), new Card(7, Suit.COINS)));

    }

    @Test
    void testEndOfRoundLastTrickLogicPlayer1() throws Exception {
        newGameAndRegistration();

        // Empty deck to simulate drawing cards.
        while (this.control.gameplay.deck.hasNext()) {
            this.control.gameplay.deck.draw();
        }

        // Setup some coins to be on the table.
        this.control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS),
                new Card(2, Suit.COINS), new Card(7, Suit.COINS), new Card(1, Suit.SWORDS)));

        Player player1Spy = Mockito.spy(this.control.getPlayer1());
        this.control.setPlayer1(player1Spy);

        // Setup to look like player 1 move.
        this.control.changeState(State.WAIT_4_PLAYER1_MOVE);
        assertThat(this.control.getPlayer2().getCoins()).isEqualTo(0);
        assertThat(this.control.getPlayer2().isSevenCoins()).isFalse();

        // Put one card in player2 hand and zero in player 1 to simulate last card of the round.
        this.control.getPlayer1().hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.CUPS)));
        this.control.getPlayer2().hand = new ArrayList<>();

        // Play the card.
        this.control.handlePlayResponse(new PlayResponseEvent(player1Id, new Pickup(new Card(1, Suit.CUPS),
                Arrays.asList(new Card(1, Suit.SWORDS))), this.control.getGameId()));

        Thread.sleep(500);
        assertThat(this.player1.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);

        assertThat(this.control.roundCounter).isEqualTo(1);
        Mockito.verify(player1Spy, times(1)).play(Optional.of(new Card(1, Suit.CUPS)),
                Arrays.asList(new Card(1, Suit.SWORDS)));
        Mockito.verify(player1Spy, times(1)).play(Optional.empty(),
                Arrays.asList(new Card(1, Suit.COINS),
                        new Card(2, Suit.COINS), new Card(7, Suit.COINS)));

    }

    @Test
    void testEndOfGamePlayer2Wins() throws Exception {
        newGameAndRegistration();
        this.verifySpecifiedPlayerWins(this.control.getPlayer2(), this.control.getPlayer1());
    }

    @Test
    void testEndOfGamePlayer1Wins() throws Exception {
        newGameAndRegistration();
        this.verifySpecifiedPlayerWins(this.control.getPlayer1(), this.control.getPlayer2());
    }

    @Test
    void testTieGame() throws Exception {
        newGameAndRegistration();

        // This will generate for 21 - 21 tie
        this.control.getPlayer2().setScore(10);
        this.control.getPlayer1().setScore(10);
        this.playAndVerifyOneRound();

        // Confirm the tie.
        assertThat(this.control.getPlayer2().getScore()).isEqualTo(11);
        assertThat(this.control.getPlayer1().getScore()).isEqualTo(11);

        // First player switches at the end of a round so player2 is up again.
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);
        assertThat(this.control.roundCounter).isEqualTo(1);

    }

    @Test
    void testNewGame() throws Exception {
        assertThat(control.currentState).isEqualTo(State.INIT);
        this.control.initializeGame(createPlayer1Details(), player1);
        Thread.sleep(250L);

        // initializeGame also registers player1 so we will be waiting for player.
        assertThat(control.currentState).isEqualTo(State.WAIT_FOR_PLAYER2);

    }

    @Test
    void testRejoinGameSameNameAndSecret() throws Exception {
        newGameAndRegistration();

        com.stettler.scopa.model.PlayerDetails d1 = createPlayer1Details();
        d1.setPlayerId(null);
        TestSource src = new TestSource();

        List<Player> players = new ArrayList();
        players.addAll(control.getAllPlayers());

        control.registerPlayer(d1,src);
        assertThat(players).containsExactlyElementsOf(control.getAllPlayers());
        assertThat(control.playerMap.get(control.getAllPlayers().get(0).getDetails().getPlayerId())).isEqualTo(src);
    }

    @Test
    void testRejoinGamePlayerId() throws Exception {
        newGameAndRegistration();

        PlayerDetails d1 = new PlayerDetails();
        d1.setPlayerId(control.getPlayer1().getDetails().getPlayerId());
        TestSource src = new TestSource();

        List<Player> players = new ArrayList<>();
        players.addAll(control.getAllPlayers());
        control.registerPlayer(d1,src);
        assertThat(players).containsExactlyElementsOf(control.getAllPlayers());
        assertThat(control.playerMap.get(d1.getPlayerId())).isEqualTo(src);
    }
    @Test
    void testRegisterException() throws Exception {
        assertThatThrownBy(() -> {
            control.handleRegister(new RegisterEvent(createPlayer1Details()));
        }).isInstanceOf(InvalidStateTransitionException.class);
    }

    /**
     * Test trying to pickup a trick when you have a single card match.
     */
    @Test
    void testMoveExceptionWhenYouHaveSingleCardMatchingWithRetryPlayer1() throws Exception {
        newGameAndRegistration();
        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS), new Card(2, Suit.COINS),
                new Card(6, Suit.COINS), new Card(4, Suit.COINS)));
        control.getPlayer1().hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.CUPS), new Card(2, Suit.CUPS),
                new Card(3, Suit.CUPS), new Card(4, Suit.CUPS)));
        control.getPlayer2().hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.SWORDS), new Card(2, Suit.SWORDS),
                new Card(3, Suit.SWORDS), new Card(4, Suit.SWORDS)));

        player1.getEvents().clear();
        player2.getEvents().clear();

        control.triggerEvent(new PlayResponseEvent(player1Id, new Pickup(new Card(4, Suit.CUPS),
                Arrays.asList(new Card(1, Suit.COINS), new Card(3, Suit.COINS))), this.control.getGameId()));

        Thread.sleep(500);

        // Check to see if the deck was updated in the status.
        ErrorEvent tmpe = (ErrorEvent) player1.getEvents().get(0);
        assertThat(tmpe.getMessage()).contains("You must take the single card");

        assertThat(this.control.turnCounter).isEqualTo(0);
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);
        assertThat(this.player1.getEvents().get(1)).isInstanceOf(PlayRequestEvent.class);
        assertThat(this.player1.getEvents().get(1).getPlayerId()).isEqualTo(this.player1Id);
    }

    /**
     * Test trying to pickup a trick when you have a single card match.
     */
    @Test
    void testMoveExceptionOnDiscardWithRetryPlayer2() throws Exception {
        newGameAndRegistration();
        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(9, Suit.COINS), new Card(2, Suit.COINS),
                new Card(3, Suit.COINS), new Card(4, Suit.COINS)));
        control.getPlayer1().hand = new ArrayList<>(Arrays.asList(new Card(2, Suit.SCEPTERS), new Card(2, Suit.CUPS),
                new Card(3, Suit.CUPS), new Card(9, Suit.CUPS)));
        control.getPlayer2().hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.SWORDS), new Card(2, Suit.SWORDS),
                new Card(3, Suit.SWORDS), new Card(4, Suit.SWORDS)));

        triggerEvent(control, new PlayResponseEvent(player1Id, new Discard(new
                Card(10, Suit.CUPS)), this.control.getGameId()));

        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);
        assertThat(this.control.turnCounter).isEqualTo(1);

        triggerEvent(control, new PlayResponseEvent(player2Id, new Discard(new Card(4, Suit.SWORDS)), this.control.getGameId()));
        // Check to see if the deck was updated in the status.

        ErrorEvent tmpe = (ErrorEvent) player2.getEvents().get(0);
        assertThat(tmpe.getMessage()).contains("You can not discard that card because you can take a trick with it!");

        assertThat(this.control.turnCounter).isEqualTo(1);
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);
        assertThat(this.player2.getEvents().get(1)).isInstanceOf(PlayRequestEvent.class);
        assertThat(this.player2.getEvents().get(1).getPlayerId()).isEqualTo(this.player2Id);
    }
    /**
     * Test discarding an empty list.
     */
    @Test
    void testMoveExceptionOnEmptyDiscard() throws Exception {
        newGameAndRegistration();
        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(9, Suit.COINS), new Card(2, Suit.COINS),
                new Card(3, Suit.COINS), new Card(4, Suit.COINS)));
        control.getPlayer1().hand = new ArrayList<>(Arrays.asList(new Card(2, Suit.SCEPTERS), new Card(2, Suit.CUPS),
                new Card(3, Suit.CUPS), new Card(9, Suit.CUPS)));
        control.getPlayer2().hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.SWORDS), new Card(2, Suit.SWORDS),
                new Card(3, Suit.SWORDS), new Card(4, Suit.SWORDS)));

        Discard move = new Discard();
        triggerEvent(control, new PlayResponseEvent(player1Id, move, this.control.getGameId()));
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);
        assertThat(this.control.turnCounter).isEqualTo(0);

        ErrorEvent tmpe = (ErrorEvent) player1.getEvents().get(0);
        assertThat(tmpe.getMessage()).contains("No discard was provided.");

    }
    /**
     * Test pickup with empty items
     */
    @Test
    void testMoveExceptionOnEmptyHandCardOnPickup() throws Exception {
        newGameAndRegistration();
        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(9, Suit.COINS), new Card(2, Suit.COINS),
                new Card(3, Suit.COINS), new Card(4, Suit.COINS)));
        control.getPlayer1().hand = new ArrayList<>(Arrays.asList(new Card(2, Suit.SCEPTERS), new Card(2, Suit.CUPS),
                new Card(3, Suit.CUPS), new Card(9, Suit.CUPS)));
        control.getPlayer2().hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.SWORDS), new Card(2, Suit.SWORDS),
                new Card(3, Suit.SWORDS), new Card(4, Suit.SWORDS)));

        Pickup move = new Pickup();
        triggerEvent(control, new PlayResponseEvent(player1Id, move, this.control.getGameId()));
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);
        assertThat(this.control.turnCounter).isEqualTo(0);

        ErrorEvent tmpe = (ErrorEvent) player1.getEvents().get(0);
        assertThat(tmpe.getMessage()).contains("You must provide a card to pickup with.");

    }
    /**
     * Test pickup with empty items
     */
    @Test
    void testMoveExceptionOnEmptyPileForPickup() throws Exception {
        newGameAndRegistration();
        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(9, Suit.COINS), new Card(2, Suit.COINS),
                new Card(3, Suit.COINS), new Card(4, Suit.COINS)));
        control.getPlayer1().hand = new ArrayList<>(Arrays.asList(new Card(2, Suit.SCEPTERS), new Card(2, Suit.CUPS),
                new Card(3, Suit.CUPS), new Card(9, Suit.CUPS)));
        control.getPlayer2().hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.SWORDS), new Card(2, Suit.SWORDS),
                new Card(3, Suit.SWORDS), new Card(4, Suit.SWORDS)));

        Pickup move = new Pickup();
        move.setPlayerCard(new Card(2, Suit.SCEPTERS));
        triggerEvent(control, new PlayResponseEvent(player1Id, move, this.control.getGameId()));
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);
        assertThat(this.control.turnCounter).isEqualTo(0);

        ErrorEvent tmpe = (ErrorEvent) player1.getEvents().get(0);
        assertThat(tmpe.getMessage()).contains("Your list to pickup was empty.");

    }
    @Test
    void testMovePlayer1AndPlayer2Pickup() throws Exception {
        newGameAndRegistration();
        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS), new Card(2, Suit.COINS),
                new Card(6, Suit.COINS), new Card(4, Suit.COINS)));
        control.getPlayer1().hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.CUPS), new Card(2, Suit.CUPS),
                new Card(3, Suit.CUPS), new Card(4, Suit.CUPS)));
        control.getPlayer2().hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.SWORDS), new Card(2, Suit.SWORDS),
                new Card(3, Suit.SWORDS), new Card(4, Suit.SWORDS)));

        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(4, Suit.CUPS),
                Collections.singletonList(new Card(4, Suit.COINS))), this.control.getGameId()));

        // Check to see if the deck was updated in the status.
        GameStatusEvent tmpe = (GameStatusEvent) player1.getEvents().get(0);
        assertThat(tmpe.getStatus().getOpponentsLastCard()).isNull();
        GameStatusEvent tmpe2 = (GameStatusEvent) player2.getEvents().get(0);
        assertThat(tmpe2.getStatus().getOpponentsLastCard()).isEqualTo(new Card(4, Suit.CUPS));

        assertThat(tmpe.getStatus().getTable()).containsExactly(new Card(1, Suit.COINS), new Card(2, Suit.COINS),
                new Card(6, Suit.COINS)).isEqualTo(this.control.gameplay.tableCards);
        assertThat(tmpe.getStatus().getPlayerHand()).containsExactly(new Card(1, Suit.CUPS), new Card(2, Suit.CUPS),
                new Card(3, Suit.CUPS)).isEqualTo(this.control.getPlayer1().getHand());
        assertThat(tmpe.getStatus().getOpponentCardCount()).isEqualTo(this.control.getPlayer2().getHand().size());


        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);
        assertThat(player2.getEvents().get(1)).isInstanceOf(PlayRequestEvent.class);
        assertThat(control.turnCounter).isEqualTo(1);
        assertThat(control.roundCounter).isEqualTo(0);

        triggerEvent(control, new PlayResponseEvent(this.player2Id, new Pickup(new Card(3, Suit.SWORDS),
                Arrays.asList(new Card(1, Suit.COINS), new Card(2, Suit.COINS))), control.getGameId()));

        // Check to see if the deck was updated in the status.
        tmpe = (GameStatusEvent) player2.getEvents().get(0);
        assertThat(tmpe.getStatus().getTable()).containsExactly(new Card(6, Suit.COINS));
        assertThat(tmpe.getStatus().getPlayerHand()).containsExactly(new Card(1, Suit.SWORDS), new Card(2, Suit.SWORDS), new Card(4, Suit.SWORDS))
                .isEqualTo(this.control.getPlayer2().getHand());
        assertThat(tmpe.getStatus().getOpponentCardCount()).isEqualTo(this.control.getPlayer1().getHand().size());

        assertThat(tmpe.getStatus().getOpponentsLastCard()).isEqualTo(new Card(4, Suit.CUPS));
        GameStatusEvent tmpe1 = (GameStatusEvent) player1.getEvents().get(0);
        assertThat(tmpe1.getStatus().getOpponentsLastCard()).isEqualTo(new Card(3, Suit.SWORDS));


        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);
        assertThat(player1.getEvents().get(1)).isInstanceOf(PlayRequestEvent.class);
        assertThat(control.turnCounter).isEqualTo(2);
        assertThat(control.roundCounter).isEqualTo(0);

    }

    @Test
    void testPlayer1Scopa() throws Exception {
        newGameAndRegistration();
        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS), new Card(2, Suit.COINS),
                new Card(3, Suit.COINS), new Card(4, Suit.COINS)));
        control.getPlayer1().hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.CUPS), new Card(2, Suit.CUPS),
                new Card(3, Suit.CUPS), new Card(10, Suit.CUPS)));
        control.getPlayer2().hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.SWORDS), new Card(2, Suit.SWORDS),
                new Card(3, Suit.SWORDS), new Card(4, Suit.SWORDS)));

        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(10, Suit.CUPS),
                new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS), new Card(2, Suit.COINS),
                        new Card(3, Suit.COINS), new Card(4, Suit.COINS)))), this.control.getGameId()));

        assertThat(player1.getEvents().get(0).getEventType()).isEqualTo(EventType.SCOPA);
        assertThat(((ScopaEvent) player1.getEvents().get(0)).isFinalTrick()).isFalse();
        assertThat(control.gameplay.tableCards).hasSize(0);
        assertThat(control.getPlayer1().getScore()).isEqualTo(1);
        assertThat(control.getPlayer2().getScore()).isEqualTo(0);

        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);
        assertThat(player2.getEvents().get(1)).isInstanceOf(PlayRequestEvent.class);
        assertThat(control.turnCounter).isEqualTo(1);
        assertThat(control.roundCounter).isEqualTo(0);
    }

    @Test
    void testPlayer2Scopa() throws Exception {
        newGameAndRegistration();
        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS), new Card(2, Suit.COINS),
                new Card(3, Suit.COINS), new Card(4, Suit.COINS)));
        control.getPlayer1().hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.CUPS), new Card(2, Suit.CUPS),
                new Card(3, Suit.CUPS), new Card(10, Suit.CUPS)));
        control.getPlayer2().hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.SWORDS), new Card(2, Suit.SWORDS),
                new Card(3, Suit.SWORDS), new Card(4, Suit.SWORDS)));

        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(1, Suit.CUPS),
                new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS)))), this.control.getGameId()));

        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        triggerEvent(control, new PlayResponseEvent(player2Id, new Pickup(new Card(9, Suit.SWORDS),
                new ArrayList<>(Arrays.asList(new Card(2, Suit.COINS),
                        new Card(3, Suit.COINS), new Card(4, Suit.COINS)))), this.control.getGameId()));

        assertThat(player2.getEvents().get(0).getEventType()).isEqualTo(EventType.SCOPA);
        assertThat(((ScopaEvent) player2.getEvents().get(0)).isFinalTrick()).isFalse();
        assertThat(control.gameplay.tableCards).hasSize(0);
        assertThat(control.getPlayer1().getScore()).isEqualTo(0);
        assertThat(control.getPlayer2().getScore()).isEqualTo(1);

        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);
    }

    private void playAndVerifyOneRound() throws Exception {
        // Should register as the 0th round.
        assertThat(control.roundCounter).isEqualTo(0);

        // Remove cards to the last 6 cards.  Almost end of round.
        for (int i = 0; i < 24; i++) {
            control.gameplay.deck.draw();
        }

        assertThat(control.gameplay.deck.size()).isEqualTo(6);

        // Setup the table with some cards.
        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(7, Suit.SCEPTERS), new Card(6, Suit.CUPS),
                new Card(6, Suit.COINS), new Card(7, Suit.COINS)));

        // Setup player cards so they each player has one card left.
        control.getPlayer1().hand = new ArrayList<>(Arrays.asList(new Card(7, Suit.CUPS)));
        control.getPlayer2().hand = new ArrayList<>(Arrays.asList(new Card(5, Suit.SWORDS)));

        // Play1 plays a pickup
        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(7, Suit.CUPS),
                Arrays.asList(new Card(7, Suit.COINS))), this.control.getGameId()));
        assertThat(player1.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);

        // Confirming the move is player2 before proceeding.
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // Player2 playing a discard.
        triggerEvent(control, new PlayResponseEvent(player2Id, new Discard(new Card(5, Suit.SWORDS)), this.control.getGameId()));
        assertThat(player2.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);

        // The last 6 cards should  have been dealt.
        assertThat(this.control.gameplay.deck.size()).isEqualTo(0);
        assertThat(this.control.getPlayer1().getHand()).hasSize(3);
        assertThat(this.control.getPlayer2().getHand()).hasSize(3);

        // Change the cards to known values to make testing easier.
        control.getPlayer1().hand = new ArrayList<>();
        control.getPlayer2().hand = new ArrayList<>();
        for (int i = 8; i < 11; i++) {
            control.getPlayer1().hand.add(new Card(i, Suit.CUPS));
            control.getPlayer2().hand.add(new Card(i, Suit.SWORDS));
        }

        // this should be a safe discard for player1
        triggerEvent(control, new PlayResponseEvent(player1Id, new Discard(new Card(10, Suit.CUPS)), this.control.getGameId()));
        assertThat(this.player1.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // this should be a safe discard for player2
        triggerEvent(control, new PlayResponseEvent(player2Id, new Discard(new Card(8, Suit.SWORDS)), this.control.getGameId()));
        assertThat(this.player2.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);

        // this should be another safe discard for player1
        triggerEvent(control, new PlayResponseEvent(player1Id, new Discard(new Card(9, Suit.CUPS)), this.control.getGameId()));
        assertThat(this.player1.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // player 2 will pickup the 11 that player1 layed down.
        triggerEvent(control, new PlayResponseEvent(player2Id, new Pickup(new Card(9, Suit.SWORDS),
                Arrays.asList(new Card(9, Suit.CUPS))), this.control.getGameId()));
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);
        assertThat(this.player2.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);

        // Player1 should be able to pick up the 10 of sword layed down by player2
        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(8, Suit.CUPS),
                Arrays.asList(new Card(8, Suit.SWORDS))), this.control.getGameId()));
        assertThat(this.player1.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // player 2 should have a final safe discard
        triggerEvent(control, new PlayResponseEvent(player2Id, new Pickup(new Card(10, Suit.SWORDS),
                Arrays.asList(new Card(10, Suit.CUPS))), this.control.getGameId()));
        assertThat(this.player2.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);
    }

    private void verifySpecifiedPlayerWins(Player winner, Player loser) throws Exception {
        // Should register as the 0th round.
        assertThat(control.roundCounter).isEqualTo(0);

        // Remove cards to the last 6 cards.  Almost end of round.
        for (int i = 0; i < 24; i++) {
            control.gameplay.deck.draw();
        }

        assertThat(control.gameplay.deck.size()).isEqualTo(6);

        // Setup the table with some cards.
        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(7, Suit.SCEPTERS), new Card(6, Suit.CUPS),
                new Card(6, Suit.COINS), new Card(7, Suit.COINS)));

        // Setup player cards so they each player has one card left.
        control.getPlayer1().hand = new ArrayList<>(Arrays.asList(new Card(7, Suit.CUPS)));
        control.getPlayer2().hand = new ArrayList<>(Arrays.asList(new Card(5, Suit.SWORDS)));

        // Play1 plays a pickup
        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(7, Suit.CUPS),
                Arrays.asList(new Card(7, Suit.COINS))), this.control.getGameId()));
        assertThat(player1.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);

        // Confirming the move is player2 before proceeding.
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // Player2 playing a discard.
        triggerEvent(control, new PlayResponseEvent(player2Id, new Discard(new Card(5, Suit.SWORDS)), this.control.getGameId()));
        assertThat(player2.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);

        // The last 6 cards should  have been dealt.
        assertThat(this.control.gameplay.deck.size()).isEqualTo(0);
        assertThat(this.control.getPlayer1().getHand()).hasSize(3);
        assertThat(this.control.getPlayer2().getHand()).hasSize(3);

        // Change the cards to known values to make testing easier.
        control.getPlayer1().hand = new ArrayList<>();
        control.getPlayer2().hand = new ArrayList<>();
        for (int i = 8; i < 11; i++) {
            control.getPlayer1().hand.add(new Card(i, Suit.CUPS));
            control.getPlayer2().hand.add(new Card(i, Suit.SWORDS));
        }

        // Setup the score so that this is the end of the round.
        loser.setScore(10);
        winner.setScore(11);

        // this should be a safe discard for player1
        triggerEvent(control, new PlayResponseEvent(player1Id, new Discard(new Card(10, Suit.CUPS)), this.control.getGameId()));
        assertThat(this.player1.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // this should be a safe discard for player2
        triggerEvent(control, new PlayResponseEvent(player2Id, new Discard(new Card(8, Suit.SWORDS)), this.control.getGameId()));
        assertThat(this.player2.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);

        // this should be another safe discard for player1
        triggerEvent(control, new PlayResponseEvent(player1Id, new Discard(new Card(9, Suit.CUPS)), this.control.getGameId()));
        assertThat(this.player1.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // player 2 will pickup the 11 that player1 layed down.
        triggerEvent(control, new PlayResponseEvent(player2Id, new Pickup(new Card(9, Suit.SWORDS),
                Arrays.asList(new Card(9, Suit.CUPS))), this.control.getGameId()));
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);
        assertThat(this.player2.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);

        // Player1 should be able to pick up the 10 of sword layed down by player2
        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(8, Suit.CUPS),
                Arrays.asList(new Card(8, Suit.SWORDS))), this.control.getGameId()));
        assertThat(this.player1.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // player 2 should have a final safe discard
        triggerEvent(control, new PlayResponseEvent(player2Id, new Pickup(new Card(10, Suit.SWORDS),
                Arrays.asList(new Card(10, Suit.CUPS))), this.control.getGameId()));
        assertThat(this.player1.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);
        assertThat(this.player1.getEvents().get(1)).isInstanceOf(GameOverEvent.class);
        assertThat(this.player2.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);
        assertThat(this.player2.getEvents().get(1)).isInstanceOf(GameOverEvent.class);

        // Confirming the move is player2 before proceeding.
        // the first player changes with each round.  Should be player2 now.
        assertThat(this.control.currentState).isEqualTo(State.WINNER);

        // Both players should receive the same gameover event status.
        GameOverEvent oe = (GameOverEvent) this.player1.getEvents().get(1);
        assertThat(oe.getWinningPlayer()).isEqualTo(winner.getDetails());
        assertThat(oe.getLosingPlayer()).isEqualTo(loser.getDetails());
        assertThat(oe.getLosingScore()).isEqualTo(11);
        assertThat(oe.getWinningScore()).isEqualTo(12);

        oe = (GameOverEvent) this.player2.getEvents().get(1);
        assertThat(oe.getWinningPlayer()).isEqualTo(winner.getDetails());
        assertThat(oe.getLosingPlayer()).isEqualTo(loser.getDetails());
        assertThat(oe.getLosingScore()).isEqualTo(11);
        assertThat(oe.getWinningScore()).isEqualTo(12);
    }

    private com.stettler.scopa.model.PlayerDetails createPlayer1Details() {
        com.stettler.scopa.model.PlayerDetails p1 = new com.stettler.scopa.model.PlayerDetails();
        p1.setScreenHandle("player1");
        p1.setEmailAddr("player1@gmail.com");
        p1.setPlayerSecret("player1secret");
        p1.setPlayerToken("player1token");
        return p1;
    }

    private com.stettler.scopa.model.PlayerDetails createPlayer2Details() {
        com.stettler.scopa.model.PlayerDetails p = new com.stettler.scopa.model.PlayerDetails();
        p.setScreenHandle("player2");
        p.setEmailAddr("player2@gmail.com");
        p.setPlayerSecret("player2secret");
        p.setPlayerToken("player2token");
        return p;
    }

    private void newGameAndRegistration() throws Exception {
        triggerEvent(control, new NewGameEvent());

        com.stettler.scopa.model.PlayerDetails d1 = createPlayer1Details();
        com.stettler.scopa.model.PlayerDetails d2 = createPlayer2Details();
        control.initializeGame(d1, player1);
        control.registerPlayer(d2, player2);
        Thread.sleep(250);

        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);

        GameStatusEvent ps1 = (GameStatusEvent) player1.getEvents().get(0);
        this.player1Id = ps1.getStatus().getPlayerDetails().getPlayerId();
        assertThat(d1.getPlayerId()).isEqualTo(this.player1Id);

        GameStatusEvent ps2 = (GameStatusEvent) player2.getEvents().get(0);
        this.player2Id = ps2.getStatus().getPlayerDetails().getPlayerId();
        assertThat(d2.getPlayerId()).isEqualTo(this.player2Id);
    }

    private void triggerEvent(EventSource playerSource, GameEvent event) throws Exception{
        player1.getEvents().clear();
        player2.getEvents().clear();
        playerSource.triggerEvent(event);
        Thread.sleep(250);
    }
}
