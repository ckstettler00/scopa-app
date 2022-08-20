package com.stettler.scopa.statemachine;

import com.stettler.scopa.model.Card;
import com.stettler.scopa.model.Deck;
import com.stettler.scopa.model.Suit;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DeckTest {
    @Test
    void testDeck() {
        Deck deck = new Deck();
        assertThat(deck.size()).isEqualTo(40);

        Set<Card> card = new HashSet<>();
        while (deck.hasNext()) {
            card.add(deck.draw());
        }
        for (Suit s : Suit.values()) {
            for (int i = 1; i < 11; i++) {
                assertThat(card).contains(new Card(i, s));
            }
        }
        assertThat(card).hasSize(40);
    }
}
