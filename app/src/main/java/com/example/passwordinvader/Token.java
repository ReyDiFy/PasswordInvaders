package com.example.passwordinvader;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

/** A falling box holding one password character. Drawn in code, no sprite needed. */
public class Token {
    private static final Paint FILL = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint STROKE = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint TEXT = new Paint(Paint.ANTI_ALIAS_FLAG);
    static {
        FILL.setColor(0xDD0D1B2A);
        STROKE.setStyle(Paint.Style.STROKE);
        STROKE.setStrokeWidth(3f);
        TEXT.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        TEXT.setTextAlign(Paint.Align.CENTER);
    }

    public final TokenType type;
    public final char ch;
    public final int size;
    public float x, y;

    public Token(TokenType type, char ch, int size, float x, float y) {
        this.type = type;
        this.ch = ch;
        this.size = size;
        this.x = x;
        this.y = y;
    }

    /** Forgiving tap target: the box plus a margin around it. */
    public boolean hit(float px, float py) {
        float pad = size * 0.35f;
        return px >= x - pad && px <= x + size + pad && py >= y - pad && py <= y + size + pad;
    }

    public void draw(Canvas c) {
        c.drawRoundRect(x, y, x + size, y + size, 8f, 8f, FILL);
        STROKE.setColor(type.color);
        c.drawRoundRect(x, y, x + size, y + size, 8f, 8f, STROKE);
        TEXT.setColor(type.color);
        TEXT.setTextSize(size * 0.6f);
        float baseline = y + size / 2f - (TEXT.ascent() + TEXT.descent()) / 2f;
        c.drawText(String.valueOf(ch), x + size / 2f, baseline, TEXT);
    }
}
