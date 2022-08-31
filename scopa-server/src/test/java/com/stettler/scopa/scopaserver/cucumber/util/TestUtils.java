package com.stettler.scopa.scopaserver.cucumber.util;

import com.stettler.scopa.exceptions.ScopaRuntimeException;
import com.stettler.scopa.model.Card;
import com.stettler.scopa.model.Suit;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestUtils {

    public List<Card> parseCards(String cardString) {
        List<Card> cards = new ArrayList<>();

        if (cardString == null) {
            return cards;
        }

        String[] items = cardString.split(",");
        for (String i : items) {
            if (i.trim().toLowerCase().startsWith(Suit.CUPS.name().toLowerCase())) {
                cards.add(new Card(parseCardNum(i), Suit.CUPS));
            } else if (i.trim().toLowerCase().startsWith(Suit.SWORDS.name().toLowerCase())) {
                cards.add(new Card(parseCardNum(i), Suit.SWORDS));
            } else if (i.trim().toLowerCase().startsWith(Suit.COINS.name().toLowerCase())) {
                cards.add(new Card(parseCardNum(i), Suit.COINS));
            } else if (i.trim().toLowerCase().startsWith(Suit.SCEPTERS.name().toLowerCase())) {
                cards.add(new Card(parseCardNum(i), Suit.SCEPTERS));
            }
        }
        return cards;
    }
    private int parseCardNum(String cardString) {
        Pattern pattern = Pattern.compile("\\((\\d+)\\)");
        Matcher matcher = pattern.matcher(cardString);

        if(!matcher.find() ) {
            throw new ScopaRuntimeException("invalid syntax. expecting a number");
        }
        int num = Integer.parseInt(matcher.group(1));
        if (num < 1 || num > 40) {
            throw new ScopaRuntimeException("num too large:"+num);
        }
        return num;
    }
}
