package com.stettler.scopa.scopaserver.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stettler.scopa.events.*;
import com.stettler.scopa.model.*;
import com.stettler.scopa.scopaserver.cucumber.util.TestContext;
import com.stettler.scopa.scopaserver.cucumber.util.TestSocketHandler;
import com.stettler.scopa.scopaserver.cucumber.util.TestUtils;
import com.stettler.scopa.scopaserver.model.TestGameSetup;
import com.stettler.scopa.statemachine.EventSource;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class StepDefinitions{
    Logger logger = LoggerFactory.getLogger(getClass().getName());

    ObjectMapper mapper = new ObjectMapper();

    private final static int MAX_WAIT = 1000;

    TestRestTemplate template = new TestRestTemplate();

    TestUtils utils = new TestUtils();


    @Autowired
    ConversionService converter;

    @Given("a running game system")
    public void runningGame() throws Exception {

    }

    @Given("player {int} registration details")
    public void playerDetails(Integer player, DataTable data) throws Exception {
        logger.info("playerDetails {} {}", player, data);
        PlayerDetails details = new PlayerDetails();
        details.setEmailAddr(data.asMap(String.class, String.class).get("email"));
        details.setScreenHandle(data.asMap(String.class, String.class).get("screenHandle"));

        Pair<WebSocketSession, EventSource> session = createSession();

        TestContext.context().setPlayer(player-1, details);
        TestContext.context().setSession(player-1, session.getLeft());
        TestContext.context().setEventSource(player-1, session.getRight());


    }
    @When("player {int} creates a new game")
    public void createGame(Integer player) throws Exception {
        logger.info("createGame {}", player);
        TestContext context = TestContext.context();

        NewGameEvent game = new NewGameEvent(TestContext.context().getPlayer(0));
        String newGameMsg = converter.convert(game, String.class);
        logger.info("Sending new game request to the server: {}", newGameMsg);
        TestContext.context().getSession(0).sendMessage(new TextMessage(newGameMsg));

        logger.info("Wait for the server response to new game.");
        Optional<GameEvent> event = TestContext.context().getEventSource(0)
                .waitForEvent(EventType.NEWGAME_RESP, MAX_WAIT);

        assertThat(event.isPresent()).as("NewGameEventResp not received").isTrue();

        NewGameEventResp t = (NewGameEventResp)event.get();
        logger.info("Server returned game id {}", t.getGameId());
        TestContext.context().setGameId(t.getGameId());


    }
    @When("player {int} joins the game")
    public void joinTheGame(Integer player) throws Exception {
        logger.info("joinGame {}", player);
        RegisterEvent join = new RegisterEvent(TestContext.context().getPlayer(player-1));
        join.setGameId(TestContext.context().getGameId());
        String msg = converter.convert(join, String.class);

        TestContext.context().getSession(player-1).sendMessage(new TextMessage(msg));

        Optional<GameEvent> resp = TestContext.context().getEventSource(player-1)
                .waitForEvent(EventType.STATUS, MAX_WAIT);

        assertThat(resp.isPresent()).as("game status not received").isTrue();

        GameStatusEvent t = (GameStatusEvent)resp.get();
        logger.info("Last GameStatus {}", t.getStatus());
        TestContext.context().setGameStatus(t.getStatus());


    }
    @Then("the game state becomes {string}")
    public void verifyCurrentGameState(String state) throws Exception {

        ResponseEntity<List> statuses = template.getForEntity("http://localhost:8080/scopa/gamelist/"+TestContext.context().getGameId(),
                List.class);
        assertThat(statuses.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(statuses.getBody().size()).isGreaterThan(0);
        GameStatus status = mapper.convertValue(statuses.getBody().get(0), GameStatus.class);
        logger.info("verifyCurrentGameState {} status:{}", state, status);
        assertThat(status.getCurrentGameState()).isEqualTo(state);
        TestContext.context().setGameStatus(status);
    }

    @And("player {int} receives the game status")
    public void playerReceivesTheGameStatus(Integer player) {
        logger.info("player {} receives the game status", player);
        Optional<GameEvent> status = TestContext.context().getEventSource(player-1).waitForEvent(EventType.STATUS, 1000);
        assertThat(status.isPresent()).isTrue();
    }

    @And("player {int} receives a move request")
    public void playerReceivesMoveRequest(Integer player) {
        logger.info("player {} receives a move request", player);
        Optional<GameEvent> status = TestContext.context().getEventSource(player-1).waitForEvent(EventType.PLAY_REQ, 1000);
        assertThat(status.isPresent()).isTrue();
    }

    @And("player {int} has {int} cards in their hand")
    public void playerHasCardsRemaining(int player, int cards) {
        Optional<GameEvent> statusEvent = TestContext.context().getEventSource(player-1).waitForEvent(EventType.STATUS, 1000);
        assertThat(statusEvent.isPresent()).isTrue();

        GameStatus status = ((GameStatusEvent)statusEvent.get()).getStatus();
        assertThat(status.getPlayerHand()).hasSize(cards);
    }

    @And ("the table contains {int} cards")
    public void tableContainsCards(int cards) {
        assertThat(TestContext.context().getGameStatus().getTable()).hasSize(cards);
    }
    @And("player {int} is the current player")
    public void currentPlayer(int player){
        assertThat(TestContext.context().getPlayer(player -1).getPlayerId()).isEqualTo(TestContext.context().getGameStatus().getCurrentPlayerId());
    }

    @And("test game setup with")
    public void gameSetup(DataTable table) throws Exception {
        TestGameSetup setup = new TestGameSetup();
        Map<String, String> data = table.asMap(String.class, String.class);
        if (data.containsKey("player1Hand")) {
            setup.setPlayer1Hand(utils.parseCards(data.get("player1Hand")));
        }
        if (data.containsKey("player2Hand")) {
            setup.setPlayer2Hand(utils.parseCards(data.get("player2Hand")));
        }
        if (data.containsKey("tableCards")) {
            setup.setTableCards(utils.parseCards(data.get("tableCards")));
        }
        if (data.containsKey("cardsRemaining")) {
            setup.setCardsRemaining(Integer.parseInt(data.get("cardsRemaining")));
        }
        logger.info("gameSetup data {}", setup);
        ResponseEntity<Void> resp = template.postForEntity("http://localhost:8080/testhelper/initgame/"+TestContext.context().getGameId(), setup,  Void.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
    }
    @And("player {int} picks up {string} with {string}")
    public void playerPicksUp(int player, String tableString, String playerString) throws Exception {
        List<Card> playerCard = this.utils.parseCards(playerString);
        List<Card> tableCards = this.utils.parseCards(tableString);
        PlayResponseEvent play = new PlayResponseEvent(TestContext.context().getPlayer(player-1).getPlayerId(),
                new Pickup(playerCard.get(0), tableCards), TestContext.context().getGameId());
        TestContext.context().getEventSource(player-1).clearEvents();
        TestContext.context().getSession(player-1).sendMessage(new TextMessage(converter.convert(play, String.class)));
    }
    @And("player {int} play was successful")
    public void playerPlaySuccessful(int player) {
        Optional<GameEvent> event = TestContext.context().getEventSource(player-1).waitForEvent(EventType.STATUS, 1000);
        assertThat(event.isPresent()).isTrue();
    }

    @And("player {int} receives {string} event")
    public void receivesEvent(int player, String eventString) {
        EventType type = EventType.valueOf(eventString.toUpperCase());
        Optional<GameEvent> event = TestContext.context().getEventSource(player-1).waitForEvent(type, 1000);
        assertThat(event.isPresent()).isTrue();
    }

    @And("player {int} discards {string}")
    public void playerDiscards(int player, String playerString) throws Exception {
        List<Card> playerCard = this.utils.parseCards(playerString);
        PlayResponseEvent play = new PlayResponseEvent(TestContext.context().getPlayer(player-1).getPlayerId(),
                new Discard(playerCard.get(0)), TestContext.context().getGameId());
        TestContext.context().getEventSource(player-1).clearEvents();
        TestContext.context().getSession(player-1).sendMessage(new TextMessage(converter.convert(play, String.class)));
        TestContext.context().getEventSource(player-1).clearEvents();
    }

    @And("player {int} receives error containing {string}")
    public void playerReceivesError(int player, String msg) {
        Optional<GameEvent> event = TestContext.context().getEventSource(player-1).waitForEvent(EventType.ERROR, 1000);
        assertThat(event.isPresent()).isTrue();
        ErrorEvent err = (ErrorEvent)event.get();
        assertThat(err.getMessage()).contains(msg);
    }
    @And("cards on table are {string}")
    public void cardsOnTable(String cardsString) {
        List<Card> table = utils.parseCards(cardsString);
        assertThat(table).isEqualTo(TestContext.context().getGameStatus().getTable());
    }

    private Pair<WebSocketSession, EventSource> createSession() throws Exception {
        WebSocketClient client = new StandardWebSocketClient();
        TestSocketHandler handler = new TestSocketHandler(converter);
        ListenableFuture<WebSocketSession> session = client.doHandshake(handler, String.format("ws://localhost:8080/scopaevents"));
        return Pair.of(session.get(), handler.getEventSource());
    }

}
