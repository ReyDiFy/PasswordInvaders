package com.example.passwordinvader;

import java.util.Random;

/**
 * Tracks the password being built and its requirements (e.g. 8 chars: 2 upper, 2 lower, 2 num, 2 symbol).
 * Guarantees the finished password meets the requirements: once the only free slots left are needed
 * by unmet requirements, tokens of other classes are refused.
 */
public class PasswordState {
    public final int length;
    private final int[] need = new int[4];
    private final int[] have = new int[4];
    private final StringBuilder pw = new StringBuilder();

    public PasswordState(int length, int upper, int lower, int num, int symbol) {
        this.length = length;
        need[TokenType.UPPER.ordinal()] = upper;
        need[TokenType.LOWER.ordinal()] = lower;
        need[TokenType.NUM.ordinal()] = num;
        need[TokenType.SYMBOL.ordinal()] = symbol;
    }

    private int remaining() { return length - pw.length(); }

    private int unmet() {
        int u = 0;
        for (int i = 0; i < 4; i++) u += Math.max(0, need[i] - have[i]);
        return u;
    }

    public boolean accepts(TokenType t) {
        if (remaining() <= 0) return false;
        boolean needed = have[t.ordinal()] < need[t.ordinal()];
        return needed || unmet() < remaining();
    }

    /** @return false if the token was refused (it should stay in play). */
    public boolean add(TokenType t, char ch) {
        if (!accepts(t)) return false;
        pw.append(ch);
        have[t.ordinal()]++;
        return true;
    }

    /** Random class for a drop, weighted toward classes still needed (weight 3 vs 1). */
    public TokenType pickType(Random r) {
        TokenType[] all = TokenType.values();
        int[] w = new int[all.length];
        int total = 0;
        for (int i = 0; i < all.length; i++) {
            w[i] = have[i] < need[i] ? 3 : (unmet() < remaining() ? 1 : 0);
            total += w[i];
        }
        if (total == 0) return all[r.nextInt(all.length)];
        int roll = r.nextInt(total);
        for (int i = 0; i < all.length; i++) {
            roll -= w[i];
            if (roll < 0) return all[i];
        }
        return all[0];
    }

    public boolean isFull() { return remaining() <= 0; }
    public String get() { return pw.toString(); }
    public int count(TokenType t) { return have[t.ordinal()]; }
    public int required(TokenType t) { return need[t.ordinal()]; }
}
