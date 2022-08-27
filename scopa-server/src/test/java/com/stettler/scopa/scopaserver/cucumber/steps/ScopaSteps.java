package com.stettler.scopa.scopaserver.cucumber.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScopaSteps {

    Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Given("player {int} registration details")
    void playerDetails(Integer player, DataTable details) {
        logger.info("playerDetails {} {}", player, details);
    }
    @When("player {int} creates a new game with these details")
    void createGame(Integer player) {
        logger.info("createGame {}", player);

    }
    @When("player {int} joins the game with these details")
    void joinTheGame(Integer player) {
        logger.info("joinGame {}", player);

    }
    @Then("the game state becomes {string}")
    void verifyCurrentGameState(String state) {
        logger.info("verifyCurrentGameState {}", state);
    }

    @And("player {int} receives the game status")
    void playerReceivesTheGameStatus(Integer player) {
        logger.info("playerReceivesTheGameStatus {}", player);
    }

    @And("player {int} receives a move request")
    void playerReceivesMoveRequest(Integer player) {
        logger.info("player receives a move request {}", player);
    }

}
