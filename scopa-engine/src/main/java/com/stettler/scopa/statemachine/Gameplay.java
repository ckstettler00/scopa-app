package com.stettler.scopa.statemachine;

import com.stettler.scopa.exceptions.InvalidMoveException;
import com.stettler.scopa.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class Gameplay {

    Logger logger = LoggerFactory.getLogger(getClass().getName());
    Deck deck = new Deck();
    Player player1 = new Player();
    Player player2 = new Player();
    Player currentPlayer;

    Display table = new Display();
    int lastTrick = 0;

    public List<Card> getTableCards() {
        List<Card> tmp = new ArrayList<>();
        tmp.addAll(tableCards);
        return tmp;
    }

    List<Card> tableCards = new ArrayList<>();



    /**
     * Deal all cards for the table.
     */
    public void deal() {
        tableCards.clear();
        for (int i = 0; i < 4; i++) {
            tableCards.add(deck.draw());
        }
    }

    /**
     * Return the deck
     * @return
     */
    public Deck getDeck() {
        return this.deck;
    }

    /**
     * Creates a new decks randomly
     */
    public void shuffle() {
        this.deck = new Deck();
    }

    /**
     * Deal a single card to a player.
     * @param player
     */
    public void deal(Player player) {
        player.deal(deck.draw());
    }

//    void playTheGame() {
//
//        int turnCount = 0;
//        int playedCard = 0;
//        int roundCount = 0;
//        String pickup;
//
//
//        while (!winner()) {
//
//            for (int i = 0; i < 4; i++) {
//                tableCards.add(deck.draw());
//            }
//
//            while (deck.hasNext()) {
//
//                turnCount = 0;
//
//
//                for (int i = 0; i < 3; i++) {
//                    player1.deal(deck.draw());
//                    player2.deal(deck.draw());
//                }
//
//                while (turnCount < 6) {
//
//                    if (roundCount % 2 == 0) {
//                        table.clearScreen();
//                        player1First(turnCount);
//                    } else {
//                        table.clearScreen();
//                        player2First(turnCount);
//                    }
//
//                    Move move = Move.INVALID;
//                    System.out.println("It is " + currentPlayer.getDetails().getScreenHandle() + "'s turn.");
//                    while (move.equals(Move.INVALID)) {
//                        Scanner input = new Scanner(System.in);
//                        System.out.println("Play: ");
//                        try {
//                            playedCard = input.nextInt();
//                            input.nextLine();
//                        } catch (InputMismatchException ex) {
//                            System.out.println("Please use position numbers to play a card.");
//                            continue;
//                        }
//
//                        System.out.println("For: ");
//                        pickup = input.nextLine();
//
//                        move = createMoveCommand(playedCard, Arrays.asList(pickup.split("\\s+")));
//                    }
//                    if (tableCards.isEmpty()) {
//                        currentPlayer.setScore(currentPlayer.getScore() + 1);
//                        System.out.println(currentPlayer.getDetails().getScreenHandle() + " got a Scopa!");
//                    }
//                    turnCount++;
//                }
//            }
//            if (!tableCards.isEmpty()) {
//                if (lastTrick == 1) {
//                    player1.play(Optional.empty(), tableCards);
//                    tableCards.clear();
//                } else {
//                    player2.play(Optional.empty(), tableCards);
//                    tableCards.clear();
//                }
//            }
//
//            if (player1.getPrimesSum() > player2.getPrimesSum()) {
//                player1.setScore(player1.getScore() + 1);
//            } else if (player1.getPrimesSum() < player2.getPrimesSum()) {
//                player2.setScore(player2.getScore() + 1);
//            }
//            scoring(player1);
//            scoring(player2);
//
//            System.out.println("At the end of round " + (roundCount + 1) + " the  score is:");
//            System.out.println(player1.getDetails().getScreenHandle() + " " + player1.getScore());
//            System.out.println(player2.getDetails().getScreenHandle() + " " + player2.getScore());
//
//            deck = new Deck();
//            roundCount++;
//        }
//
//        System.out.println("It looks like we have a winner! Yay David! The final score is " + player1.getDetails().getScreenHandle() + " " + player1.getScore() + " and " + player2.getDetails().getScreenHandle() + " " + player2.getScore());
//
//    }

    public Move handlePickup(Player player, Pickup pickup) {

        int sum = 0;

        if (pickup.getTableCards().size() != 1) {
            for (Card C : tableCards) {
                if (C.getVal() == pickup.getPlayerCard().getVal()) {
                    throw new InvalidMoveException(player.getDetails().getPlayerId(), pickup, "You must take the single card for that card.");
                }
            }
        }
        for (int i = 0; i < pickup.getTableCards().size(); i++) {
            sum = sum + (pickup.getTableCards().get(i).getVal());
        }
        if (sum == pickup.getPlayerCard().getVal()) {
            player.play(Optional.of(pickup.getPlayerCard()), pickup.getTableCards());
            logger.debug("Player hand after play {}", player.getHand());
            logger.debug("card to pickup from table {}", pickup);
            for (Card c : pickup.getTableCards()) {
                logger.debug("Removing card {} from table {}", c, tableCards);
                tableCards.remove(c);
                logger.debug("Table after play {}", tableCards);
            }
            return pickup;
        }

        throw new InvalidMoveException(player.getDetails().getPlayerId(), pickup, "Those do not add up! Please try again: ");
    }

    public Move handleDiscard(Player player, Discard discard) {

        List<Card> sorted = this.tableCards.stream().sorted().collect(Collectors.toList());
        int maxSet = findMaxSetSize(sorted, discard.getDiscarded().getVal());

        for (int i = 1; i <= maxSet; i++) {
            List<Set<Card>> s = computeLegalMoves(sorted, i, discard.getDiscarded().getVal());
            if (s.size() > 0) {
                throw new InvalidMoveException(player.getDetails().getPlayerId(), discard, "You can not discard that card because you can take a trick with it! Please try again.");
            }
        }
        player.getHand().remove(discard.getDiscarded());
        tableCards.add(discard.getDiscarded());
        return discard;

    }

    public void scoring(Player p) {
        if (p.getTotal() > 20) {
            p.setScore(p.getScore() + 1);
        }
        if (p.getCoins() > 5) {
            p.setScore(p.getScore() + 1);
        }
        if (p.isSevenCoins()) {
            p.setScore(p.getScore() + 1);
        }
        p.clearScore();

    }

    public boolean winner() {
        if (player1.getScore() == player2.getScore()) {
            return false;
        }
        if (player1.getScore() >= 11 || player2.getScore() >= 11) {
            return true;
        } else {
            return false;
        }
    }

    List<Set<Card>> computeLegalMoves(List<Card> table, int size, int sumToMatch) {

        int[] counters = new int[size];
        List<Set<Card>> allSets = new ArrayList<>();


        processLoopsRecursively(table, counters, 0, allSets, sumToMatch);
        return allSets;
    }

    int findMaxSetSize(List<Card> table, int maxSum) {
        int sum = 0;
        int maxSize = 1;
        for (int i = 0; i < table.size(); i++) {
            sum += table.get(i).getVal();
            if (sum > maxSum) {
                break;
            }
            maxSize++;
        }
        return maxSize;
    }

    void processLoopsRecursively(List<Card> a, int[] counters, int level, List<Set<Card>> allSets, Integer sumToMatch) {
        if (level == counters.length) performSumCheck(a, counters, allSets, sumToMatch);
        else {
            // Optimization to skip duplication.
            int startVal = 0;
            if (level > 0)
                startVal = counters[level - 1] + 1;

            for (counters[level] = startVal; counters[level] < a.size(); counters[level]++) {
                processLoopsRecursively(a, counters, level + 1, allSets, sumToMatch);
            }
        }
    }

    void performSumCheck(List<Card> a, int[] counters, List<Set<Card>> allSets, Integer sumToMatch) {
        Set<Card> solution = new HashSet<>();
        int sum = 0;
        for (int level = 0; level < counters.length; level++) {

            // Short circuit loops if the exceeds the match value.
            sum += a.get(counters[level]).getVal();
            if (sum > sumToMatch) {
                break;
            }
            solution.add(a.get(counters[level]));
        }
        // Add to the solution only if the sum matches.
        // Using Set to eliminate any dupicates.
        if (solution.size() == counters.length && solution.stream().mapToInt(c -> c.getVal()).sum() == sumToMatch) {
            allSets.add(solution);
        }
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }
}