package com.example.passwordinvader;

/** Per-level settings. Edit forLevel() to tune difficulty tiers. */
public class Mission {
    public final int level, length, upper, lower, num, symbol;
    public final String difficulty, tagline, speedLabel;
    public final int combatPct, tokenPct, speedPct;   // shown as bars on the mission screen
    public final float speedScale;                    // multiplies every EnemyType.speed
    public final int color;

    private Mission(int level, String difficulty, String tagline, String speedLabel, int length,
                    int upper, int lower, int num, int symbol,
                    int combatPct, int tokenPct, int speedPct, float speedScale, int color) {
        this.level = level; this.difficulty = difficulty; this.tagline = tagline; this.speedLabel = speedLabel;
        this.length = length; this.upper = upper; this.lower = lower; this.num = num; this.symbol = symbol;
        this.combatPct = combatPct; this.tokenPct = tokenPct; this.speedPct = speedPct;
        this.speedScale = speedScale; this.color = color;
    }

    public static Mission forLevel(int level) {
        if (level <= 3) return new Mission(level, "EASY", "Slow enemies", "SLOW", 8, 2, 2, 2, 2, 25, 70, 30, 1.0f, Ui.GREEN);
        if (level <= 7) return new Mission(level, "MEDIUM", "Normal speed", "NORMAL", 12, 3, 3, 3, 3, 50, 55, 55, 1.4f, Ui.YELLOW);
        return new Mission(level, "HARD", "Fast enemies", "FAST", 16, 4, 4, 4, 4, 80, 40, 80, 1.8f, Ui.RED);
    }
}
