package com.stettler.scopa.scopaserver.cucumber.steps;

import com.stettler.scopa.events.*;
import com.stettler.scopa.model.GameStatus;
import com.stettler.scopa.model.PlayerDetails;
import com.stettler.scopa.scopaserver.cucumber.util.TestContext;
import com.stettler.scopa.scopaserver.cucumber.util.TestSocketHandler;
import com.stettler.scopa.statemachine.EventSource;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.util.Optional;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class StepDefinitions{
    Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final static int MAX_WAIT = 1000;

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
    public void verifyCurrentGameState(String state) {
        GameStatus status = TestContext.context().getGameStatus();
        logger.info("verifyCurrentGameState {} status:{}", state, status);
        assertThat(status.getCurrentGameState()).isEqualTo(state);
    }

    @And("player {int} receives the game status")
    public void playerReceivesTheGameStatus(Integer player) {
        logger.info("playerReceivesTheGameStatus {}", player);
    }

    @And("player {int} receives a move request")
    public void playerReceivesMoveRequest(Integer player) {
        logger.info("player receives a move request {}", player);
    }

    private Pair<WebSocketSession, EventSource> createSession() throws Exception {
        WebSocketClient client = new StandardWebSocketClient();
        TestSocketHandler handler = new TestSocketHandler(converter);
        ListenableFuture<WebSocketSession> session = client.doHandshake(handler, String.format("ws://localhost:8080/scopaevents"));
        return Pair.of(session.get(), handler.getEventSource());
    }

}
