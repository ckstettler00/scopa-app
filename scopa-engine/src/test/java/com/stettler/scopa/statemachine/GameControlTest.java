package com.stettler.scopa.statemachine;

import com.stettler.scopa.events.*;
import com.stettler.scopa.exceptions.InvalidStateTransitionException;
import com.stettler.scopa.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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


        control.registerPlayer1Source(player1);
        control.registerPlayer2Source(player2);
        player1.start();
        player2.start();
        control.start();
    }

    @Test
    void testEmptyHands() throws Exception {
        newGameAndRegistration();
        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS), new Card(2, Suit.COINS),
                new Card(3, Suit.COINS), new Card(4, Suit.COINS)));
        control.gameplay.player1.hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.CUPS)));
        control.gameplay.player2.hand = new ArrayList<>(Arrays.asList(new Card(2, Suit.SWORDS)));

        // Verify counters and score before sending first event
        assertThat(control.gameplay.deck.size()).isEqualTo(30);
        assertThat(control.roundCounter).isEqualTo(0);
        assertThat(control.gameplay.player1.getScore()).isEqualTo(0);
        assertThat(control.gameplay.player2.getScore()).isEqualTo(0);

        // Player 1 uses last card.
        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(1, Suit.CUPS),
                new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS))))));

        // Verify key counts and scores have not changed.
        assertThat(control.gameplay.deck.size()).isEqualTo(30);
        assertThat(control.roundCounter).isEqualTo(0);
        assertThat(control.gameplay.player1.getScore()).isEqualTo(0);
        assertThat(control.gameplay.player2.getScore()).isEqualTo(0);

        // Should be player 2s turn
        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // Use player2 last card.
        triggerEvent(control, new PlayResponseEvent(player2Id, new Pickup(new Card(2, Suit.SWORDS),
                new ArrayList<>(Arrays.asList(new Card(2, Suit.COINS))))));

        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);

        // This should have triggered a deal of 6 cards. 3 for each.  The table
        // should not change.
        assertThat(control.gameplay.deck.size()).isEqualTo(24);

        // Scores should not be affected since the round did not increment and no scopa.
        assertThat(control.roundCounter).isEqualTo(0);
        assertThat(control.gameplay.player1.getScore()).isEqualTo(0);
        assertThat(control.gameplay.player2.getScore()).isEqualTo(0);

        assertThat(control.gameplay.tableCards).containsExactly(
                new Card(3, Suit.COINS), new Card(4, Suit.COINS));
        assertThat(control.gameplay.player1.getHand()).hasSize(3);
        assertThat(control.gameplay.player2.getHand()).hasSize(3);


    }

    @Test
    void testLastTrick() throws Exception {
        newGameAndRegistration();
        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS), new Card(2, Suit.COINS),
                new Card(3, Suit.COINS), new Card(4, Suit.COINS)));
        control.gameplay.player1.hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.CUPS), new Card(2, Suit.CUPS),
                new Card(3, Suit.CUPS), new Card(9, Suit.CUPS)));
        control.gameplay.player2.hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.SWORDS), new Card(2, Suit.SWORDS),
                new Card(3, Suit.SWORDS), new Card(4, Suit.SWORDS)));

        // Playing a discard should not switch the lastTrick state.
        triggerEvent(control, new PlayResponseEvent(player1Id, new Discard(new Card(11, Suit.CUPS))));
        assertThat(this.control.lastTrickPlayer).isNull();

        // Confirming the move is player2 before proceeding.
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // Playing a pickup should switch the lastTrick indicator.
        triggerEvent(control, new PlayResponseEvent(player2Id, new Pickup(new Card(4, Suit.SWORDS),
                Arrays.asList(new Card(4, Suit.COINS)))));
        // Check to see if the deck was updated in the status.
        assertThat(this.control.lastTrickPlayer.getDetails().getPlayerId()).isEqualTo(this.player2Id);

        // Confirming the move is player1 before proceeding.
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);

        // Playing a pickup should switch the lastTrick indicator.
        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(3, Suit.CUPS),
                Arrays.asList(new Card(3, Suit.COINS)))));

        assertThat(this.control.lastTrickPlayer.getDetails().getPlayerId()).isEqualTo(this.player1Id);

    }

    @Test
    void testPlayOutOfTurn() throws Exception {
        newGameAndRegistration();
        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS), new Card(2, Suit.COINS),
                new Card(3, Suit.COINS), new Card(4, Suit.COINS)));
        control.gameplay.player1.hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.CUPS), new Card(2, Suit.CUPS)));
        control.gameplay.player2.hand = new ArrayList<>(Arrays.asList(new Card(2, Suit.SWORDS), new Card(1, Suit.SWORDS)));

        // Should be player 1s turn
        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);

        // Player 1 is up but player2 is playing
        triggerEvent(control, new PlayResponseEvent(player2Id, new Pickup(new Card(1, Suit.SWORDS),
                new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS))))));

        // Player2 will receive error for playing out of turn.
        assertThat(player2.getEvents().get(0)).isInstanceOf(ErrorEvent.class);
        ErrorEvent error = (ErrorEvent) player2.getEvents().get(0);
        assertThat(error.getMessage()).contains("Played out of turn");

        // Should be player 1s turn still.
        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);

        // Player 1 is up so play a card.
        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(1, Suit.CUPS),
                new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS))))));

        // Should be player 2s turn
        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // Player 2 is up but let player1 try to play.
        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(1, Suit.CUPS),
                new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS))))));

        // Player 1 will get an illegal play error.
        assertThat(player1.getEvents().get(0)).isInstanceOf(ErrorEvent.class);
        error = (ErrorEvent) player1.getEvents().get(0);
        assertThat(error.getMessage()).contains("Played out of turn");

        // Player 2 is still up.
        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // Make a valid player 2 play
        triggerEvent(control, new PlayResponseEvent(player2Id, new Pickup(new Card(2, Suit.SWORDS),
                new ArrayList<>(Arrays.asList(new Card(2, Suit.COINS))))));

        // Should be player 1s turn
        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);

        assertThat(this.control.turnCounter).isEqualTo(2);
        assertThat(this.control.player1.getHand()).hasSize(1);
        assertThat(this.control.player2.getHand()).hasSize(1);

    }

    @Test
    void testEndOfRound() throws Exception {
        newGameAndRegistration();

        // Should register as the 0th round.
        assertThat(control.roundCounter).isEqualTo(0);

        // Remove cards to the last 6 cards.  Almost end of round.
        for (int i = 0; i < 24; i++) {
            control.gameplay.deck.draw();
        }

        assertThat(control.gameplay.deck.size()).isEqualTo(6);

        // Setup the table with some cards.
        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS), new Card(6, Suit.CUPS),
                new Card(6, Suit.COINS), new Card(7, Suit.COINS)));

        // Setup player cards so they each player has one card left.
        control.gameplay.player1.hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.CUPS)));
        control.gameplay.player2.hand = new ArrayList<>(Arrays.asList(new Card(5, Suit.SWORDS)));

        // Play1 plays a pickup
        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(1, Suit.CUPS),
                Arrays.asList(new Card(1, Suit.COINS)))));
        assertThat(player1.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);

        // Confirming the move is player2 before proceeding.
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // Player2 playing a discard.
        triggerEvent(control, new PlayResponseEvent(player2Id, new Discard(new Card(5, Suit.SWORDS))));
        assertThat(player2.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);

        // The last 6 cards should  have been dealt.
        assertThat(this.control.gameplay.deck.size()).isEqualTo(0);
        assertThat(this.control.gameplay.player1.getHand()).hasSize(3);
        assertThat(this.control.gameplay.player2.getHand()).hasSize(3);

        // Change the cards to known values to make testing easier.
        control.gameplay.player1.hand = new ArrayList<>();
        control.gameplay.player2.hand = new ArrayList<>();
        for (int i = 8; i < 11; i++) {
            control.gameplay.player1.hand.add(new Card(i, Suit.CUPS));
            control.gameplay.player2.hand.add(new Card(i, Suit.SWORDS));
        }

        // this should be a safe discard for player1
        triggerEvent(control, new PlayResponseEvent(player1Id, new Discard(new Card(10, Suit.CUPS))));
        assertThat(this.player1.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // this should be a safe discard for player2
        triggerEvent(control, new PlayResponseEvent(player2Id, new Discard(new Card(8, Suit.SWORDS))));
        assertThat(this.player2.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);

        // this should be another safe discard for player1
        triggerEvent(control, new PlayResponseEvent(player1Id, new Discard(new Card(9, Suit.CUPS))));
        assertThat(this.player1.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // player 2 will pickup the 11 that player1 layed down.
        triggerEvent(control, new PlayResponseEvent(player2Id, new Pickup(new Card(9, Suit.SWORDS),
                Arrays.asList(new Card(9, Suit.CUPS)))));
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);
        assertThat(this.player2.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);

        // Player1 should be able to pick up the 10 of sword layed down by player2
        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(8, Suit.CUPS),
                Arrays.asList(new Card(8, Suit.SWORDS)))));
        assertThat(this.player1.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // player 2 should have a final safe discard
        triggerEvent(control, new PlayResponseEvent(player2Id, new Pickup(new Card(10, Suit.SWORDS),
                Arrays.asList(new Card(10, Suit.CUPS)))));
        assertThat(this.player2.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);

        // The round should have incremented.
        assertThat(this.control.roundCounter).isEqualTo(1);

        // Confirming the move is player2 before proceeding.
        // the first player changes with each round.  Should be player2 now.
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        // A new set of table cards would be dealt.
        assertThat(this.control.gameplay.tableCards).hasSize(4);

        // Deck would have re-dealt
        assertThat(this.control.gameplay.deck.size()).isEqualTo(30);
        assertThat(this.control.gameplay.player1.getHand()).hasSize(3);
        assertThat(this.control.gameplay.player2.getHand()).hasSize(3);

        // Confirm player 1 got the credit for the table cards.

    }

    /**
     * You can send the game on a scopa but the point does not count.
     */
    @Test
    void testEndOfRoundScopaDoesNotCount() {
        assert (false);
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

        Player player2Spy = Mockito.spy(this.control.player2);
        this.control.player2 = player2Spy;

        // Setup to look like player 2 move.
        this.control.changeState(State.WAIT_4_PLAYER2_MOVE);
        assertThat(this.control.player2.getCoins()).isEqualTo(0);
        assertThat(this.control.player2.isSevenCoins()).isFalse();

        // Put one card in player2 hand and zero in player 1 to simulate last card of the round.
        this.control.player2.hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.CUPS)));
        this.control.player1.hand = new ArrayList<>();

        // Play the card.
        this.control.handlePlayResponse(new PlayResponseEvent(player2Id, new Pickup(new Card(1, Suit.CUPS),
                Arrays.asList(new Card(1, Suit.SWORDS)))));

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

        Player player1Spy = Mockito.spy(this.control.player1);
        this.control.player1 = player1Spy;

        // Setup to look like player 1 move.
        this.control.changeState(State.WAIT_4_PLAYER1_MOVE);
        assertThat(this.control.player2.getCoins()).isEqualTo(0);
        assertThat(this.control.player2.isSevenCoins()).isFalse();

        // Put one card in player2 hand and zero in player 1 to simulate last card of the round.
        this.control.player1.hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.CUPS)));
        this.control.player2.hand = new ArrayList<>();

        // Play the card.
        this.control.handlePlayResponse(new PlayResponseEvent(player1Id, new Pickup(new Card(1, Suit.CUPS),
                Arrays.asList(new Card(1, Suit.SWORDS)))));

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
    void testEndOfGame() {
        assert (false);
    }

    @Test
    void testTieGame() {
        assert (false);
    }

    @Test
    void testNewGame() throws Exception {
        assertThat(control.currentState).isEqualTo(State.INIT);
        control.triggerEvent(new NewGameEvent());
        Thread.sleep(1000L);
        assertThat(control.currentState).isEqualTo(State.WAIT_FOR_PLAYER1);

    }

    @Test
    void testRegistration() throws Exception {
        assertThat(control.currentState).isEqualTo(State.INIT);
        control.triggerEvent(new NewGameEvent());
        Thread.sleep(500);

        assertThat(control.currentState).isEqualTo(State.WAIT_FOR_PLAYER1);

        PlayerDetails p1 = createPlayer1Details();
        control.triggerEvent(new RegisterEvent(p1));
        Thread.sleep(500);

        assertThat(control.currentState).isEqualTo(State.WAIT_FOR_PLAYER2);

        PlayerDetails p2 = createPlayer2Details();
        control.triggerEvent(new RegisterEvent(p2));

        Thread.sleep(500L);
        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);
        assertThat(control.turnCounter).isEqualTo(0);

        assertThat(player1.getEvents()).hasSize(2);
        assertThat(player1.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);
        assertThat(player1.getEvents().get(1)).isInstanceOf(PlayRequestEvent.class);

        assertThat(player2.getEvents()).hasSize(1);
        assertThat(player2.getEvents().get(0)).isInstanceOf(GameStatusEvent.class);

        GameStatusEvent ps1 = (GameStatusEvent) player1.getEvents().get(0);
        assertThat(ps1.getPlayerId()).isEqualTo("all");
        assertThat(ps1.getStatus().getCurrentPlayerId()).isEqualTo(ps1.getStatus().getPlayer().getDetails().getPlayerId());

        assertThat(ps1.getStatus().getPlayer().getDetails().getScreenHandle()).isEqualTo("player1");
        assertThat(ps1.getStatus().getPlayer().getDetails().getEmailAddr()).isEqualTo("player1@gmail.com");
        assertThat(ps1.getStatus().getPlayer().getDetails().getPlayerSecret()).isEqualTo("player1secret");
        assertThat(ps1.getStatus().getPlayer().getDetails().getPlayerToken()).isEqualTo("player1token");

        GameStatusEvent ps2 = (GameStatusEvent) player2.getEvents().get(0);
        assertThat(ps2.getPlayerId()).isEqualTo("all");
        assertThat(ps2.getStatus().getCurrentPlayerId()).isEqualTo(ps1.getStatus().getPlayer().getDetails().getPlayerId());
        assertThat(ps2.getStatus().getPlayer().getDetails().getScreenHandle()).isEqualTo("player2");
        assertThat(ps2.getStatus().getPlayer().getDetails().getEmailAddr()).isEqualTo("player2@gmail.com");
        assertThat(ps2.getStatus().getPlayer().getDetails().getPlayerSecret()).isEqualTo("player2secret");
        assertThat(ps2.getStatus().getPlayer().getDetails().getPlayerToken()).isEqualTo("player2token");


        assertThat(ps1.getStatus().getGameId()).isEqualTo(ps2.getStatus().getGameId());
        assertThat(ps1.getStatus().getGameId()).isNotEmpty();
        assertThat(ps1.getStatus().getDeck()).isEqualTo(ps2.getStatus().getDeck());
        assertThat(ps1.getStatus().getDeck().size()).isEqualTo(30);

        assertThat(ps1.getStatus().getTable()).isEqualTo(ps2.getStatus().getTable());
        assertThat(ps1.getStatus().getTable().size()).isEqualTo(4);

        assertThat(ps1.getStatus().getPlayer().getHand()).isNotEqualTo(ps2.getStatus().getPlayer().getHand());
        assertThat(ps1.getStatus().getPlayer().getHand().size()).isEqualTo(3);
        assertThat(ps2.getStatus().getPlayer().getHand().size()).isEqualTo(3);

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
        control.gameplay.player1.hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.CUPS), new Card(2, Suit.CUPS),
                new Card(3, Suit.CUPS), new Card(4, Suit.CUPS)));
        control.gameplay.player2.hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.SWORDS), new Card(2, Suit.SWORDS),
                new Card(3, Suit.SWORDS), new Card(4, Suit.SWORDS)));

        player1.getEvents().clear();
        player2.getEvents().clear();

        control.triggerEvent(new PlayResponseEvent(player1Id, new Pickup(new Card(4, Suit.CUPS),
                Arrays.asList(new Card(1, Suit.COINS), new Card(3, Suit.COINS)))));

        Thread.sleep(500);

        // Check to see if the deck was updated in the status.
        ErrorEvent tmpe = (ErrorEvent) player1.getEvents().get(0);
        assertThat(tmpe.getMessage()).contains("You must take the single card for that card");

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
        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS), new Card(2, Suit.COINS),
                new Card(3, Suit.COINS), new Card(4, Suit.COINS)));
        control.gameplay.player1.hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.CUPS), new Card(2, Suit.CUPS),
                new Card(3, Suit.CUPS), new Card(9, Suit.CUPS)));
        control.gameplay.player2.hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.SWORDS), new Card(2, Suit.SWORDS),
                new Card(3, Suit.SWORDS), new Card(4, Suit.SWORDS)));

        triggerEvent(control, new PlayResponseEvent(player1Id, new Discard(new Card(11, Suit.CUPS))));

        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);
        assertThat(this.control.turnCounter).isEqualTo(1);

        triggerEvent(control, new PlayResponseEvent(player2Id, new Discard(new Card(4, Suit.SWORDS))));
        // Check to see if the deck was updated in the status.

        ErrorEvent tmpe = (ErrorEvent) player2.getEvents().get(0);
        assertThat(tmpe.getMessage()).contains("You can not discard that card because you can take a trick with it!");

        assertThat(this.control.turnCounter).isEqualTo(1);
        assertThat(this.control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);
        assertThat(this.player2.getEvents().get(1)).isInstanceOf(PlayRequestEvent.class);
        assertThat(this.player2.getEvents().get(1).getPlayerId()).isEqualTo(this.player2Id);
    }

    @Test
    void testMovePlayer1AndPlayer2Pickup() throws Exception {
        newGameAndRegistration();
        control.gameplay.tableCards = new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS), new Card(2, Suit.COINS),
                new Card(6, Suit.COINS), new Card(4, Suit.COINS)));
        control.gameplay.player1.hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.CUPS), new Card(2, Suit.CUPS),
                new Card(3, Suit.CUPS), new Card(4, Suit.CUPS)));
        control.gameplay.player2.hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.SWORDS), new Card(2, Suit.SWORDS),
                new Card(3, Suit.SWORDS), new Card(4, Suit.SWORDS)));

        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(4, Suit.CUPS),
                Collections.singletonList(new Card(4, Suit.COINS)))));

        // Check to see if the deck was updated in the status.
        GameStatusEvent tmpe = (GameStatusEvent) player1.getEvents().get(0);
        assertThat(tmpe.getStatus().getTable()).containsExactly(new Card(1, Suit.COINS), new Card(2, Suit.COINS),
                new Card(6, Suit.COINS)).isEqualTo(this.control.gameplay.tableCards);
        assertThat(tmpe.getStatus().getPlayer().getHand()).containsExactly(new Card(1, Suit.CUPS), new Card(2, Suit.CUPS),
                new Card(3, Suit.CUPS)).isEqualTo(this.control.player1.getHand());

        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);
        assertThat(player2.getEvents().get(1)).isInstanceOf(PlayRequestEvent.class);
        assertThat(control.turnCounter).isEqualTo(1);
        assertThat(control.roundCounter).isEqualTo(0);

        triggerEvent(control, new PlayResponseEvent(this.player2Id, new Pickup(new Card(3, Suit.SWORDS),
                Arrays.asList(new Card(1, Suit.COINS), new Card(2, Suit.COINS)))));

        // Check to see if the deck was updated in the status.
        tmpe = (GameStatusEvent) player2.getEvents().get(0);
        assertThat(tmpe.getStatus().getTable()).containsExactly(new Card(6, Suit.COINS));
        assertThat(tmpe.getStatus().getPlayer().getHand()).containsExactly(new Card(1, Suit.SWORDS), new Card(2, Suit.SWORDS), new Card(4, Suit.SWORDS))
                .isEqualTo(this.control.player2.getHand());

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
        control.gameplay.player1.hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.CUPS), new Card(2, Suit.CUPS),
                new Card(3, Suit.CUPS), new Card(10, Suit.CUPS)));
        control.gameplay.player2.hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.SWORDS), new Card(2, Suit.SWORDS),
                new Card(3, Suit.SWORDS), new Card(4, Suit.SWORDS)));

        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(10, Suit.CUPS),
                new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS), new Card(2, Suit.COINS),
                        new Card(3, Suit.COINS), new Card(4, Suit.COINS))))));

        assertThat(player1.getEvents().get(0).getEventType()).isEqualTo(EventType.SCOPA);
        assertThat(control.gameplay.tableCards).hasSize(0);
        assertThat(control.gameplay.player1.getScore()).isEqualTo(1);
        assertThat(control.gameplay.player2.getScore()).isEqualTo(0);

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
        control.gameplay.player1.hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.CUPS), new Card(2, Suit.CUPS),
                new Card(3, Suit.CUPS), new Card(10, Suit.CUPS)));
        control.gameplay.player2.hand = new ArrayList<>(Arrays.asList(new Card(1, Suit.SWORDS), new Card(2, Suit.SWORDS),
                new Card(3, Suit.SWORDS), new Card(4, Suit.SWORDS)));

        triggerEvent(control, new PlayResponseEvent(player1Id, new Pickup(new Card(1, Suit.CUPS),
                new ArrayList<>(Arrays.asList(new Card(1, Suit.COINS))))));

        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER2_MOVE);

        triggerEvent(control, new PlayResponseEvent(player2Id, new Pickup(new Card(9, Suit.SWORDS),
                new ArrayList<>(Arrays.asList(new Card(2, Suit.COINS),
                        new Card(3, Suit.COINS), new Card(4, Suit.COINS))))));

        assertThat(player2.getEvents().get(0).getEventType()).isEqualTo(EventType.SCOPA);
        assertThat(control.gameplay.tableCards).hasSize(0);
        assertThat(control.gameplay.player1.getScore()).isEqualTo(0);
        assertThat(control.gameplay.player2.getScore()).isEqualTo(1);

        assertThat(control.currentState).isEqualTo(State.WAIT_4_PLAYER1_MOVE);
    }

    private PlayerDetails createPlayer1Details() {
        PlayerDetails p1 = new PlayerDetails();
        p1.setScreenHandle("player1");
        p1.setEmailAddr("player1@gmail.com");
        p1.setPlayerSecret("player1secret");
        p1.setPlayerToken("player1token");
        return p1;
    }

    private PlayerDetails createPlayer2Details() {
        PlayerDetails p = new PlayerDetails();
        p.setScreenHandle("player2");
        p.setEmailAddr("player2@gmail.com");
        p.setPlayerSecret("player2secret");
        p.setPlayerToken("player2token");
        return p;
    }

    private void newGameAndRegistration() throws Exception {
        triggerEvent(control, new NewGameEvent());
        triggerEvent(control,new RegisterEvent(createPlayer1Details()));
        triggerEvent(control,new RegisterEvent(createPlayer2Details()));

        Thread.sleep(500);
        GameStatusEvent ps1 = (GameStatusEvent) player1.getEvents().get(0);
        this.player1Id = ps1.getStatus().getPlayer().getDetails().getPlayerId();

        GameStatusEvent ps2 = (GameStatusEvent) player2.getEvents().get(0);
        this.player2Id = ps2.getStatus().getPlayer().getDetails().getPlayerId();
    }

    private void triggerEvent(EventSource playerSource, GameEvent event) throws Exception{
        player1.getEvents().clear();
        player2.getEvents().clear();
        playerSource.triggerEvent(event);
        Thread.sleep(250);
    }
}
