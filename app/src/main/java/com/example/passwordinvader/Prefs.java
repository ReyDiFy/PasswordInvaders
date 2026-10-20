package com.example.passwordinvader;

import android.content.Context;
import android.content.SharedPreferences;

/** XP / campaign progress and settings flags. */
public final class Prefs {
    public static final String MUSIC = "music", SFX = "sfx", CRT = "crt", SHAKE = "shake";

    private Prefs() {}

    private static SharedPreferences sp(Context c) { return c.getSharedPreferences("game", Context.MODE_PRIVATE); }

    /** Progress is stored per account. */
    private static String k(Context c, String base) {
        String u = new AccountStore(c).username();
        return u == null ? base : base + "_" + u.toLowerCase();
    }

    public static int xp(Context c) { return sp(c).getInt(k(c, "xp"), 0); }
    public static void addXp(Context c, int n) { sp(c).edit().putInt(k(c, "xp"), xp(c) + n).apply(); }
    public static int levelFor(int xp) { return xp / 500 + 1; }
    public static int nextXp(int level) { return level * 500; }

    public static int unlocked(Context c) { return sp(c).getInt(k(c, "unlocked"), 1); }
    public static void unlock(Context c, int level) {
        if (level > unlocked(c)) sp(c).edit().putInt(k(c, "unlocked"), level).apply();
    }

    public static boolean flag(Context c, String key, boolean def) { return sp(c).getBoolean(key, def); }
    public static void setFlag(Context c, String key, boolean v) { sp(c).edit().putBoolean(key, v).apply(); }
    public static int volume(Context c) { return sp(c).getInt("volume", 80); }
    public static void setVolume(Context c, int v) { sp(c).edit().putInt("volume", v).apply(); }
}
