package com.stettler.scopa;

import com.stettler.scopa.events.NewGameEvent;
import com.stettler.scopa.events.RegisterEvent;
import com.stettler.scopa.model.PlayerDetails;
import com.stettler.scopa.statemachine.CliPlayer;
import com.stettler.scopa.statemachine.Display;
import com.stettler.scopa.statemachine.GameControl;

public class scopaCliMain {
    public static void main(String[] args) {
        GameControl control = new GameControl();
        Display board = new Display();
        CliPlayer player1Source = new CliPlayer(control, board);
        CliPlayer player2Source = new CliPlayer(control, board);


        // Start up the event loops.
        control.start();
        player1Source.start();
        player2Source.start();

        // Register with the controller.
        player1Source.instructions();
        PlayerDetails p1d = player1Source.promptForPlayerDetails();
        PlayerDetails p2d = player2Source.promptForPlayerDetails();

        control.registerPlayer(p1d, player1Source);
        control.registerPlayer(p2d, player2Source);

        control.triggerEvent(new NewGameEvent());

        control.triggerEvent(new RegisterEvent(p1d));
        control.triggerEvent(new RegisterEvent(p2d));

        control.waitForGameComplete();

    }
}
