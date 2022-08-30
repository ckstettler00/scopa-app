package com.stettler.scopa.scopaserver;

import com.stettler.scopa.events.GameEvent;
import com.stettler.scopa.events.NewGameEvent;
import com.stettler.scopa.events.PlayResponseEvent;
import com.stettler.scopa.events.RegisterEvent;
import com.stettler.scopa.model.Card;
import com.stettler.scopa.model.Discard;
import com.stettler.scopa.model.PlayerDetails;
import com.stettler.scopa.model.Suit;
import com.stettler.scopa.scopaserver.websockets.WebSocketEventSource;
import com.stettler.scopa.statemachine.EventSource;
import com.stettler.scopa.statemachine.GameEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ScopaServerApplicationTests {

	Logger logger = LoggerFactory.getLogger(getClass().getName());

	private class RecordListener implements GameEventListener {
		LinkedBlockingQueue<GameEvent> queue = new LinkedBlockingQueue<>();

		@Override
		public void notify(GameEvent event) {
			queue.add(event);
		}

		public GameEvent waitFor(int secs) {
			while (true) {
				try {
					GameEvent event = queue.poll(secs, TimeUnit.SECONDS);
					if (event != null) {
						return event;
					}
				} catch (InterruptedException e) {
					// do nothing
				}
			}
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
			this.source = new WebSocketEventSource(session);
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

	WebSocketSession session;

	RecordListener listener = new RecordListener();

	SocketHandler handler;
	@BeforeEach
	void setup() throws Exception {
		WebSocketClient client = new StandardWebSocketClient();
		handler = new SocketHandler(converter);
		ListenableFuture<WebSocketSession> session = client.doHandshake(handler, String.format("ws://localhost:8080/scopaevents"));
		this.session = session.get();
	}//  w   w   w  .  d e   mo   2   s.  c   o m


	@Test
	void startNewGame() throws Exception {
		PlayerDetails details = new PlayerDetails();
		details.setPlayerSecret("playersecret");
		details.setEmailAddr("player@gmail.com");
		details.setPlayerToken("playertoken");
		String msg = converter.convert(new NewGameEvent(details), String.class);
		session.sendMessage(new TextMessage(msg));
	}
	@Test
	void registerClient() throws Exception {
		PlayerDetails details = new PlayerDetails();
		details.setPlayerSecret("playersecret");
		details.setEmailAddr("player@gmail.com");
		details.setPlayerToken("playertoken");
		RegisterEvent event = new RegisterEvent(details);
		String msg = converter.convert(event, String.class);
		session.sendMessage(new TextMessage(msg));
	}

	@Test
	void playResponse() throws Exception {
		PlayerDetails details = new PlayerDetails();
		PlayResponseEvent event = new PlayResponseEvent(details.getPlayerId(),
				new Discard(new Card(1, Suit.COINS)));
		String msg = converter.convert(event, String.class);
		session.sendMessage(new TextMessage(msg));

	}
}
