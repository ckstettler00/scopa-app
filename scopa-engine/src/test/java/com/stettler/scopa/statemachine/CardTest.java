package com.stettler.scopa.statemachine;

import com.stettler.scopa.model.Card;
import com.stettler.scopa.model.Suit;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CardTest {
    @Test
    void testCardValues() {
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

        for (int i = 1; i < 11; i++) {
            Card c = new Card(i, Suit.COINS);
            assertThat(c.getVal()).isEqualTo(i);
            assertThat(c.getSuit()).isEqualTo(Suit.COINS);
            assertThat(c.getPrime()).isEqualTo(valPrimMap.get(i));

            c = new Card(i, Suit.SWORDS);
            assertThat(c.getVal()).isEqualTo(i);
            assertThat(c.getSuit()).isEqualTo(Suit.SWORDS);
            assertThat(c.getPrime()).isEqualTo(valPrimMap.get(i));

            c = new Card(i, Suit.SCEPTERS);
            assertThat(c.getVal()).isEqualTo(i);
            assertThat(c.getSuit()).isEqualTo(Suit.SCEPTERS);
            assertThat(c.getPrime()).isEqualTo(valPrimMap.get(i));

            c = new Card(i, Suit.CUPS);
            assertThat(c.getVal()).isEqualTo(i);
            assertThat(c.getSuit()).isEqualTo(Suit.CUPS);
            assertThat(c.getPrime()).isEqualTo(valPrimMap.get(i));
        }
    }
}
