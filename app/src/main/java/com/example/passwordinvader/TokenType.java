package com.example.passwordinvader;

import java.util.Random;

/** The four password character classes. Colors match the HUD in your mockup. */
public enum TokenType {
    UPPER("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 0xFF00F0FF),
    LOWER("abcdefghijklmnopqrstuvwxyz", 0xFFA78BFA),
    NUM("0123456789", 0xFFFFD60A),
    SYMBOL("!@#$%^&*?+-=_", 0xFF39FF14);

    public final String chars;
    public final int color;

    TokenType(String chars, int color) {
        this.chars = chars;
        this.color = color;
    }

    public char randomChar(Random r) { return chars.charAt(r.nextInt(chars.length())); }
}
