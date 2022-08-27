package com.stettler.scopa.scopaserver.cucumber.steps;

import com.stettler.scopa.events.GameEvent;
import com.stettler.scopa.events.NewGameEvent;
import com.stettler.scopa.events.RegisterEvent;
import com.stettler.scopa.model.PlayerDetails;
import com.stettler.scopa.scopaserver.websockets.WebSocketEventSource;
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
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.SocketHandler;

public class StepDefinitions{
    Logger logger = LoggerFactory.getLogger(getClass().getName());

    private class TestEventSource extends EventSource {

        List<GameEvent> events = new ArrayList<>();

        @Override
        public void handleEvent(GameEvent event) {
            logger.info("Test Source recieved event: {}", event);
            events.add(event);
        }

        public TestEventSource() {
            super();
        }
    }
    private class SocketHandler extends TextWebSocketHandler {

        EventSource source;
        ConversionService converter;

        public EventSource getEventSource() {
            return source;
        }

        public SocketHandler(ConversionService converter) {
            this.converter = converter;
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            super.afterConnectionEstablished(session);
            this.source = new TestEventSource();
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            super.handleTextMessage(session, message);
            GameEvent event = converter.convert(message.getPayload(), GameEvent.class);
            logger.info("Sending {} to the client event source");
            this.source.triggerEvent(event);
        }
    };

    @Autowired
    ConversionService converter;

    @Given("a running game system")
    public void runningGame() throws Exception {
        //Thread.sleep(10000);
    }

    @Given("player {int} registration details")
    public void playerDetails(Integer player, DataTable data) throws Exception {
        logger.info("playerDetails {} {}", player, data);
        PlayerDetails details = new PlayerDetails();
        details.setEmailAddr(data.asMap(String.class, String.class).get("email"));
        details.setScreenHandle(data.asMap(String.class, String.class).get("screenHandle"));

        TestContext.context().setPlayer(player-1, details);
        TestContext.context().setSession(player-1, createSession().getLeft());
        TestContext.context().setEventSource(player-1, createSession().getRight());

    }
    @When("player {int} creates a new game")
    public void createGame(Integer player) throws Exception {
        logger.info("createGame {}", player);

        NewGameEvent game = new NewGameEvent(TestContext.context().getPlayer(0));
        String newGameMsg = converter.convert(game, String.class);
        logger.info("Sending start message event: {}", newGameMsg);
        TestContext.context().getSession(0).sendMessage(new TextMessage(newGameMsg));

    }
    @When("player {int} joins the game")
    public void joinTheGame(Integer player) throws Exception {
        logger.info("joinGame {}", player);
        RegisterEvent join = new RegisterEvent(TestContext.context().getPlayer(player-1));
        join.setGameId(TestContext.context().getGameId());
        String msg = converter.convert(join, String.class);

        TestContext.context().getSession(player-1).sendMessage(new TextMessage(msg));

    }
    @Then("the game state becomes {string}")
    public void verifyCurrentGameState(String state) {
        logger.info("verifyCurrentGameState {}", state);
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
        SocketHandler handler = new SocketHandler(converter);
        ListenableFuture<WebSocketSession> session = client.doHandshake(handler, String.format("ws://localhost:8080/scopaevents"));
        return Pair.of(session.get(), handler.getEventSource());
    }

}
