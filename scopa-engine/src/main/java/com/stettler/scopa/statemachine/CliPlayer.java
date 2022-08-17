package com.stettler.scopa.statemachine;

import com.stettler.scopa.events.*;
import com.stettler.scopa.exceptions.InvalidMoveException;
import com.stettler.scopa.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CliPlayer extends EventSource {

    Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private Display board = null;

    private PlayerDetails details;


    private final EventSource controller;
    private static final String TABLE_CMD = "table";

    Optional<GameStatus> lastStatus = Optional.empty();

    public CliPlayer(EventSource controller, Display display) {
        logger.info("Creating player");
        this.controller = controller;
        this.board = display;
        this.addHandler(EventType.PLAY_REQ, this::handlePlayRequest);
        this.addHandler(EventType.STATUS, this::handleStatus);
        this.addHandler(EventType.ERROR, this::handleError);
        this.addHandler(EventType.GAMEOVER, this::handleGameOver);
        this.addHandler(EventType.SCOPA, this::handleScopa);
    }

    protected void handleError(GameEvent event) {
        this.board.println(String.format("ERROR: %s", ((ErrorEvent) event).getMessage()));
    }

    protected void handleScopa(GameEvent event) {
        this.board.println("Scopa!!");
    }

    protected void handleGameOver(GameEvent event) {
        GameOverEvent gameOverEvent = (GameOverEvent) event;
        this.board.println(String.format("It looks like we have a winner! Yay David! The final score %s -> %d and %s -> %d",
                gameOverEvent.getWinningPlayer(), gameOverEvent.getWinningScore(),
                gameOverEvent.getLosingPlayer(), gameOverEvent.getLosingScore()));
    }

    protected void handleStatus(GameEvent event) {
        logger.info("handleStatus event {}", event);
        GameStatusEvent gameStatusEvent = (GameStatusEvent) event;
        lastStatus = Optional.of(gameStatusEvent.getStatus());

        //Only redraw the screen if I am the current player.
        if (lastStatus.isPresent()) {
            this.board.clearScreen();
            this.board.updateGameStatus(lastStatus.get());
        }

        if (gameStatusEvent.getStatus().getCurrentPlayerId().equals(this.details.getPlayerId())) {
            logger.info("rendering {}", this.details.getPlayerId());
            this.board.render();
        }
    }

    protected void handlePlayRequest(GameEvent event) {
        logger.debug("handlePlayRequest event {}", event);
        PlayRequestEvent playEvent = (PlayRequestEvent) event;
        int playedCard = -1;
        String pickup = null;

        if (event.getEventType().equals(EventType.PLAY_REQ)) {

            Move move = Move.INVALID;
            board.println("It is " + playEvent.getDetails().getScreenHandle() + "'s turn.");
            while (move.equals(Move.INVALID)) {
                Scanner input = new Scanner(System.in);
                board.println("Play: ");
                try {
                    playedCard = input.nextInt();
                    input.nextLine();
                } catch (InputMismatchException ex) {
                    board.println("Please use position numbers to play a card.");
                    continue;
                }

                board.println("For: ");
                pickup = input.nextLine();

                try {
                    move = createMoveCommand(playedCard, Arrays.asList(pickup.split("\\s+")));
                } catch (InvalidMoveException ex) {
                    board.println(ex.getMessage());
                    move = Move.INVALID;
                }
            }
            logger.debug("triggering play response: {}", move);
            this.controller.triggerEvent(new PlayResponseEvent(this.details.getPlayerId(), move));
        }
    }

    public PlayerDetails promptForPlayerDetails() {
        PlayerDetails playerDetails = new PlayerDetails();
        Scanner input = new Scanner(System.in);
        boolean done = false;
        while (!done) {
            board.println("Enter player name:");
            String name = input.nextLine();
            if (name.length() > 10) {
                board.println("ERROR: Screen name should be <= 10 characters.");
                continue;
            }
            playerDetails.setScreenHandle(name);
            break;
        }
        this.details = playerDetails;
        logger.info("Player Name:{} Player ID:{}", details.getScreenHandle(),
                details.getPlayerId());

        return playerDetails;
    }

    public void instructions() {
        synchronized (board) {
            board.println("Welcome to Scopa! On the scopa board, each card has a number next to it that corresponds to its position.");
            board.println("When taking your turn, use these numbers to indicate the cards you wish to play.");
            board.println("When playing, you will be prompted to (Play:). Here you will enter the position number of the card in your hand you wish to play.");
            board.println("You will then be prompted (For:). Here you will enter the position number of the cards on the table you wish to pickup or enter (table) to discard.");
        }
    }

    public Move createMoveCommand(int play, List<String> pickups) {
        // User entered table.
        if (pickups.get(0).toLowerCase().trim().equals(TABLE_CMD) && pickups.size() == 1) {
            return new Discard(this.lastStatus.get().getPlayer().getHand().get(play - 1));
        }

        Pickup pickup = new Pickup();
        pickup.setPlayerCard(this.lastStatus.get().getPlayer().getHand().get(play - 1));

        for (String c : pickups) {

            int tmp = -1;

            try {
                tmp = Integer.parseInt(c);
            } catch (NumberFormatException e) {
                throw new InvalidMoveException(this.details.getPlayerId(), Move.INVALID, String.format("Enter list of numbers 1 to %d or 'table' to discard.", this.lastStatus.get().getTable().size()));
            }

            if (tmp < 0) {
                throw new InvalidMoveException(this.details.getPlayerId(), Move.INVALID, String.format("Not a valid card location. Valid entry is 1 to %d", this.lastStatus.get().getTable().size()));
            } else if (tmp > this.lastStatus.get().getTable().size() - 1) {
                throw new InvalidMoveException(this.details.getPlayerId(), Move.INVALID, String.format("Table does not contain that card. Valid entry is 1 to %d", this.lastStatus.get().getTable().size()));
            }

            pickup.addCardToPickUp(this.lastStatus.get().getTable().get(tmp));
        }
        return pickup;
    }
}
