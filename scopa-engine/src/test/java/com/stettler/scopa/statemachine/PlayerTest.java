package com.stettler.scopa.statemachine;

import com.stettler.scopa.model.Card;
import com.stettler.scopa.model.Suit;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

import java.util.*;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class PlayerTest {

    @Test
    void testPrimeSum() {
        Player p = new Player();
        p.play(Optional.of(new Card(1, Suit.CUPS)), Arrays.asList(new Card(1, Suit.COINS)));
        p.play(Optional.of(new Card(1, Suit.SWORDS)), Arrays.asList(new Card(1, Suit.SCEPTERS)));
        p.play(Optional.of(new Card(2, Suit.CUPS)), Arrays.asList(new Card(2, Suit.COINS)));
        p.play(Optional.of(new Card(2, Suit.SWORDS)), Arrays.asList(new Card(2, Suit.SCEPTERS)));

        assertThat(p.getPrimesSum()).isEqualTo(16 + 16 + 16 + 16);
    }

    @Test
    void testPrimeCombinations() {
        Map<Integer, Integer> valPrimMap = new HashMap<>();
        valPrimMap.put(1, 16);
        valPrimMap.put(2, 12);
        valPrimMap.put(3, 13);
        valPrimMap.put(4, 14);
        valPrimMap.put(5, 15);
        valPrimMap.put(6, 18);
        valPrimMap.put(7, 21);
        valPrimMap.put(8, 10);
        valPrimMap.put(9, 10);
        valPrimMap.put(10, 10);

        List<Integer> primesLowToHigh = Arrays.asList(10, 9, 8, 2, 3, 4, 5, 1, 6, 7);

        Player p = new Player();

        p.play(Optional.empty(), Arrays.asList(new Card(1, Suit.COINS), new Card(2, Suit.COINS)));
        assertThat(p.getPrimesSum()).isEqualTo(valPrimMap.get(1));

        p.play(Optional.empty(), Arrays.asList(new Card(3, Suit.COINS), new Card(2, Suit.CUPS)));
        assertThat(p.getPrimesSum()).isEqualTo(valPrimMap.get(1) + valPrimMap.get(2));

        p.play(Optional.empty(), Arrays.asList(new Card(4, Suit.COINS), new Card(3, Suit.CUPS), new Card(6, Suit.SCEPTERS)));
        assertThat(p.getPrimesSum()).isEqualTo(valPrimMap.get(1) + valPrimMap.get(3) + valPrimMap.get(6));

        p.play(Optional.empty(), Arrays.asList(new Card(5, Suit.COINS), new Card(4, Suit.CUPS), new Card(7, Suit.SCEPTERS), new Card(9, Suit.SWORDS)));
        assertThat(p.getPrimesSum()).isEqualTo(valPrimMap.get(1) + valPrimMap.get(4) + valPrimMap.get(7) + valPrimMap.get(9));

        // Go through all primes in ascending order. Adding a new one
        // should increase it every time.  Also vary how many suits are changing
        for (int s = 0; s < 4; s++) {
            p = new Player();
            List<Card> cards = new ArrayList<>();
            for (int i = 0; i < primesLowToHigh.size(); i++) {
                int expectedPrime = 0;
                for (int j = 0; j <= s; j++) {
                    Suit suit = (j == 0) ? Suit.COINS : (j == 1) ? Suit.SWORDS : (j == 2) ? Suit.CUPS : Suit.SCEPTERS;
                    cards.add(new Card(primesLowToHigh.get(i), suit));
                    expectedPrime += valPrimMap.get(primesLowToHigh.get(i));
                }
                p.play(Optional.empty(), cards);
                assertThat(p.getPrimesSum()).isEqualTo(expectedPrime);
            }
        }
    }

    @Test
    void testDeal() {
        Player p = new Player();
        for (Suit s : Suit.values()) {
            p.deal(new Card(1, s));
        }
        assertThat(p.getHand()).hasSize(4);
    }
    @Test
    void testPlayPickupAllCoins() {

        Player p = new Player();
        for (int i = 0; i < 10; i++) {
            Card c = new Card(i+1, Suit.COINS);
            p.deal(c);
            p.play(Optional.of(c), Arrays.asList(new Card(i+1, Suit.SCEPTERS)));

            assertThat(p.getCoins()).isEqualTo(i+1);
            assertThat(p.getScore()).isEqualTo(0);
            assertThat(p.getTotal()).isEqualTo((i+1)*2);
            assertThat(p.getHand()).hasSize(0);
        }

    }
    @Test
    void testPlayOptionalAllCoins() {

        Player p = new Player();
        p.deal(new Card(1, Suit.SCEPTERS));
        for (int i = 0; i < 10; i++) {
            p.play(Optional.empty(), Arrays.asList(new Card(i+1, Suit.COINS)));

            assertThat(p.getCoins()).isEqualTo(i+1);
            assertThat(p.getScore()).isEqualTo(0);
            assertThat(p.getTotal()).isEqualTo(i+1);
            assertThat(p.getHand()).hasSize(1);

        }

    }
    @Test
    void testPlaySevenCoinsLogic() {

        Player p = new Player();

        for (int i = 0; i < 10; i++) {
            p.play(Optional.empty(), Arrays.asList(new Card(i+1, Suit.SWORDS)));
            p.play(Optional.empty(), Arrays.asList(new Card(i+1, Suit.CUPS)));
            p.play(Optional.empty(), Arrays.asList(new Card(i+1, Suit.SCEPTERS)));

            if (i+1 != 7) {
                p.play(Optional.empty(), Arrays.asList(new Card(i + 1, Suit.COINS)));
            }
        }
        //Confirm no other card triggered this.
        assertThat(p.isSevenCoins()).isFalse();

        //Confirm 7 of COIN does trigger it.
        p.play(Optional.empty(), Arrays.asList(new Card(7, Suit.COINS)));
        assertThat(p.isSevenCoins()).isTrue();

    }
    @Test
    void testDetails() {
        com.stettler.scopa.model.PlayerDetails d = new com.stettler.scopa.model.PlayerDetails();
        Player p = new Player();
        p.setDetails(d);
        assertThat(d).isEqualTo(p.getDetails());
    }

    @Test
    void testClearScore() {
        Player p = new Player();
        Whitebox.setInternalState(p,"score",10);
        Whitebox.setInternalState(p,"coins",1);
        Whitebox.setInternalState(p,"total",2);
        Whitebox.setInternalState(p,"sevenCoins",true);
        Whitebox.setInternalState(p,"primesCoin",7);
        Whitebox.setInternalState(p,"primesScepter",4);
        Whitebox.setInternalState(p,"primesCups",5);
        Whitebox.setInternalState(p,"primesSwords",6);

        assertThat(p.getScore()).isEqualTo(10);
        assertThat(p.getTotal()).isEqualTo(2);
        assertThat(p.isSevenCoins()).isTrue();
        assertThat(p.getPrimesSum()).isEqualTo(7+4+5+6);

        p.clearScore();

        assertThat(p.getScore()).isEqualTo(10);
        assertThat(p.getTotal()).isEqualTo(0);
        assertThat(p.isSevenCoins()).isFalse();
        assertThat(p.getPrimesSum()).isEqualTo(0);

    }

}
