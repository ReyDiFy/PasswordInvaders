package com.example.passwordinvader;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

/** Small helpers so screens can be built in code with the neon look. */
public final class Ui {
    public static final int CYAN = 0xFF00F0FF, PURPLE = 0xFFA855F7, YELLOW = 0xFFFFD60A,
            GREEN = 0xFF39FF14, RED = 0xFFFF3B5C, MUTED = 0xFF7A8FA6,
            PANEL = 0xFF0A1624, DARK = 0xFF050F1A, EDGE = 0xFF2A4A5A, TRACK = 0xFF11202E;

    private Ui() {}

    public static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    public static GradientDrawable box(Context c, int fill, int stroke, float radiusDp, float strokeDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(c, radiusDp));
        if (strokeDp > 0) g.setStroke(dp(c, strokeDp), stroke);
        return g;
    }

    public static TextView text(Context c, String s, int color, float sp, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextColor(color);
        t.setTextSize(sp);
        t.setTypeface(Typeface.MONOSPACE, bold ? Typeface.BOLD : Typeface.NORMAL);
        t.setLetterSpacing(0.05f);
        return t;
    }
}
