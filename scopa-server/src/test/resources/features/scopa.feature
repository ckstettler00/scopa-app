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

  Scenario: Play1 and Play2 pickup cards
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
    And test game setup with
    | player1Hand | CUPS(1), CUPS(2), CUPS(3) |
    | player2Hand | SWORDS(1), SWORDS(2), SWORDS(3)|
    | tableCards | COINS(1), COINS(2), COINS(3)|
    And player 1 picks up "coins(1)" with "cups(1)"
    And player 1 play was successful
    And the game state becomes "WAIT_4_PLAYER2_MOVE"
    Then player 2 picks up "coins(2)" with "swords(2)"
    And player 2 play was successful
    And the game state becomes "WAIT_4_PLAYER1_MOVE"
    And cards on table are "coins(3)"

  Scenario: Play1 and Play2 make invalid pickup moves
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
    And test game setup with
      | player1Hand | CUPS(1), CUPS(2), CUPS(3) |
      | player2Hand | SWORDS(1), SWORDS(2), SWORDS(3)|
      | tableCards | COINS(1), COINS(2), COINS(3)|
    And player 1 picks up "coins(1)" with "cups(3)"
    And the game state becomes "WAIT_4_PLAYER1_MOVE"
    And player 1 receives error containing "Those do not add up!"
    And cards on table are "coins(1), coins(2), coins(3)"
    Then player 1 picks up "coins(1)" with "cups(1)"
    And player 1 play was successful
    And the game state becomes "WAIT_4_PLAYER2_MOVE"
    And cards on table are "coins(2), coins(3)"
    And player 2 picks up "coins(2)" with "swords(3)"
    And the game state becomes "WAIT_4_PLAYER2_MOVE"
    And player 2 receives error containing "Those do not add up!"
    And cards on table are "coins(2), coins(3)"
    And player 2 picks up "coins(2)" with "swords(2)"
    And player 2 play was successful
    And the game state becomes "WAIT_4_PLAYER1_MOVE"
    And cards on table are "coins(3)"

  Scenario: Play1 and Play2 discard cards
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
    And test game setup with
      | player1Hand | CUPS(1), CUPS(2), CUPS(3) |
      | player2Hand | SWORDS(1), SWORDS(2), SWORDS(3)|
      | tableCards | COINS(8), COINS(9), COINS(10)|
    And player 1 discards "cups(3)"
    And player 1 play was successful
    And the game state becomes "WAIT_4_PLAYER2_MOVE"
    Then player 2 discards "swords(1)"
    And player 2 play was successful
    And the game state becomes "WAIT_4_PLAYER1_MOVE"
    And cards on table are "coins(8), coins(9), coins(10), cups(3), swords(1)"

  Scenario: Play1 and Play2 make invalid discard move
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
    And test game setup with
      | player1Hand | CUPS(8), CUPS(2), CUPS(3) |
      | player2Hand | SWORDS(1), SWORDS(2), SWORDS(3)|
      | tableCards | COINS(8), COINS(9), COINS(10)|
    And player 1 discards "cups(8)"
    And player 1 receives error containing "You can not discard that card because you can take a trick with it!"
    And the game state becomes "WAIT_4_PLAYER1_MOVE"
    And cards on table are "coins(8), coins(9), coins(10)"
    Then player 1 discards "cups(2)"
    And player 1 play was successful
    And the game state becomes "WAIT_4_PLAYER2_MOVE"
    And cards on table are "coins(8), coins(9), coins(10), cups(2)"
    And player 2 discards "swords(2)"
    And player 2 receives error containing "You can not discard that card because you can take a trick with it!"
    And the game state becomes "WAIT_4_PLAYER2_MOVE"
    And cards on table are "coins(8), coins(9), coins(10), cups(2)"
    And player 2 discards "swords(3)"
    And player 2 play was successful
    And the game state becomes "WAIT_4_PLAYER1_MOVE"
    And cards on table are "coins(8), coins(9), coins(10), cups(2), swords(3)"

  Scenario: Playing out of turn
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
    And test game setup with
      | player1Hand | CUPS(1), CUPS(2), CUPS(3) |
      | player2Hand | SWORDS(1), SWORDS(2), SWORDS(3)|
      | tableCards | COINS(8), COINS(9), COINS(10)|
    And player 2 discards "swords(3)"
    And player 2 receives error containing "out of turn"
    And the game state becomes "WAIT_4_PLAYER1_MOVE"
    And player 1 discards "cups(1)"
    And player 1 play was successful
    And the game state becomes "WAIT_4_PLAYER2_MOVE"
    And player 1 discards "cups(2)"
    Then player 1 receives error containing "out of turn"
    And the game state becomes "WAIT_4_PLAYER2_MOVE"
    And cards on table are "coins(8), coins(9), coins(10), cups(1)"

  Scenario: Scopas Player1
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
    And test game setup with
      | player1Hand | CUPS(1), CUPS(2), CUPS(3) |
      | player2Hand | SWORDS(1), SCEPTER(3), SWORDS(3)|
      | tableCards | COINS(3)|
    And player 1 picks up "coins(3)" with "cups(3)"
    And player 1 play was successful
    And player 1 receives "SCOPA" event
    Then the game state becomes "WAIT_4_PLAYER2_MOVE"
    And cards on table are ""

  Scenario: Scopas Player 2
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
    And test game setup with
      | player1Hand | CUPS(1), CUPS(2), CUPS(3) |
      | player2Hand | SWORDS(1), SCEPTER(3), SWORDS(3)|
      | tableCards | |
    And player 1 discards "cups(3)"
    And player 1 play was successful
    Then the game state becomes "WAIT_4_PLAYER2_MOVE"
    And player 2 picks up "cups(3)" with "swords(3)"
    And player 2 receives "SCOPA" event
    Then the game state becomes "WAIT_4_PLAYER1_MOVE"
    And cards on table are ""