package com.stettler.scopa.statemachine;

import com.stettler.scopa.events.*;
import com.stettler.scopa.exceptions.InvalidStateTransitionException;
import com.stettler.scopa.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;

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
    void testPlayOutOfTurn() {
        assert(false);
    }

    @Test
    void testEndOfRound() {
        assert(false);
    }

    @Test
    void testEndOfGame() {
        assert(false);
    }

    @Test
    void testTieGame() {
        assert(false);
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
