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

    public void trackScore(Player p1, Player p2, Player lastTrick) {
        lastTrick.play(Optional.empty(), this.getTableCards());

        if (p1.getPrimesSum() > p2.getPrimesSum()) {
            p1.setScore(p1.getScore() + 1);
        } else if (p1.getPrimesSum() < p2.getPrimesSum()) {
            p2.setScore(p2.getScore() + 1);
        }
        this.scoring(p1);
        this.scoring(p2);
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

    public boolean winner(Player p1, Player p2) {
        if (p1.getScore() == p2.getScore()) {
            return false;
        }
        if (p1.getScore() >= 11 || p2.getScore() >= 11) {
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
}