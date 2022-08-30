Feature: Test scopa server.

  Scenario: Successful Player Registration.
    Given a running game system
    And player 1 registration details
      | screenHandle | nathan       |
      | email        | nathan@email |
    And player 2 registration details
      | screenHandle | nathan       |
      | email        | nathan@email |
    When player 1 creates a new game
    Then the game state becomes "WAIT_FOR_PLAYER2"
    When player 2 joins the game
    Then the game state becomes "WAIT_4_PLAYER1_MOVE"
    And player 1 receives the game status
    And player 1 has 3 cards in their hand
    And player 2 receives the game status
    And player 2 has 3 cards in their hand
    And player 1 receives a move request
    And the table contains 4 cards
    And player 1 is the current player
