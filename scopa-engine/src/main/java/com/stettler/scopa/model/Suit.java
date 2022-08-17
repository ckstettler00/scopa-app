package com.stettler.scopa.model;

public enum Suit {
    COINS,
    SWORDS,
    CUPS,
    SCEPTERS;

    static public char symbol(Suit s) {
        char val = 0;
        switch(s) {
            case COINS:
                val = (char)0xa2;
                break;
            case SCEPTERS:
                val = (char)0x00a1;
                break;
            case SWORDS:
                val = (char)(0x03c4);
                break;
            case CUPS:
                val = (char)(0xfc);
        }
        return val;
    }
}


