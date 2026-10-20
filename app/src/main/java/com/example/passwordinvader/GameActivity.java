package com.example.passwordinvader;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

/** Gameplay screen: HUD on top, GameView in the middle, password progress below. Extra: "level". */
public class GameActivity extends AppCompatActivity {
    private GameView game;
    private PasswordState pw;
    private Mission mission;
    private TextView tvScore, tvHearts, btnPause;
    private final View[] fills = new View[4], empties = new View[4];
    private final TextView[] counts = new TextView[4];
    private TextView[] slots;
    private boolean paused;
    private long startMs, pausedAt, pausedTotal;
    private volatile int lastLives = 3;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        mission = Mission.forLevel(getIntent().getIntExtra("level", 1));
        pw = new PasswordState(mission.length, mission.upper, mission.lower, mission.num, mission.symbol);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.addView(topBar());
        root.addView(reqGrid());
        game = new GameView(this);
        game.setPassword(pw);
        game.setSpeedScale(mission.speedScale);
        game.setCrt(Prefs.flag(this, Prefs.CRT, true));
        game.setShake(Prefs.flag(this, Prefs.SHAKE, false));
        startMs = SystemClock.elapsedRealtime();
        root.addView(game, lp(-1, 0, 1f));
        root.addView(bottomBar());
        setContentView(root);
        refresh(0, 3, "");

        game.setListener(new GameView.Listener() {
            @Override public void onStats(int score, int lives, String password, int length) {
                lastLives = lives;
                runOnUiThread(() -> refresh(score, lives, password));
            }
            @Override public void onEnd(boolean won, int score, String password) {
                runOnUiThread(() -> {
                    long played = SystemClock.elapsedRealtime() - startMs - pausedTotal;
                    startActivity(new Intent(GameActivity.this, ResultActivity.class)
                            .putExtra("won", won).putExtra("score", score).putExtra("password", password)
                            .putExtra("level", mission.level).putExtra("lives", lastLives).putExtra("elapsedMs", played));
                    finish();
                });
            }
        });
    }

    private View topBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(16), dp(12), dp(16), dp(8));
        bar.addView(Ui.text(this, String.format(Locale.US, "LV.%02d", mission.level), Ui.CYAN, 14, true));
        TextView badge = Ui.text(this, mission.difficulty, mission.color, 9, true);
        badge.setPadding(dp(8), dp(3), dp(8), dp(3));
        badge.setBackground(Ui.box(this, (mission.color & 0x00FFFFFF) | 0x22000000, mission.color, 2, 1));
        LinearLayout.LayoutParams bl = lp(-2, -2, 0);
        bl.setMarginStart(dp(8));
        bar.addView(badge, bl);
        bar.addView(new View(this), lp(0, 1, 1f));
        tvHearts = Ui.text(this, "", Ui.RED, 12, false);
        bar.addView(tvHearts);
        tvScore = Ui.text(this, "0", Ui.YELLOW, 14, true);
        LinearLayout.LayoutParams sl = lp(-2, -2, 0);
        sl.setMarginStart(dp(12));
        bar.addView(tvScore, sl);
        btnPause = Ui.text(this, "❚❚", Ui.CYAN, 10, true);
        btnPause.setGravity(Gravity.CENTER);
        btnPause.setBackground(Ui.box(this, Ui.PANEL, Ui.EDGE, 3, 1));
        btnPause.setOnClickListener(v -> {
            paused = !paused;
            game.setPaused(paused);
            if (paused) pausedAt = SystemClock.elapsedRealtime();
            else pausedTotal += SystemClock.elapsedRealtime() - pausedAt;
            btnPause.setText(paused ? "▶" : "❚❚");
        });
        LinearLayout.LayoutParams pl = lp(dp(34), dp(34), 0);
        pl.setMarginStart(dp(12));
        bar.addView(btnPause, pl);
        return bar;
    }

    private View reqGrid() {
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setPadding(dp(16), 0, dp(16), dp(8));
        String[] names = {"UPPER", "LOWER", "NUM", "SYM"};
        TokenType[] ts = TokenType.values();
        for (int r = 0; r < 2; r++) {
            LinearLayout row = new LinearLayout(this);
            row.setPadding(0, dp(3), 0, dp(3));
            for (int k = 0; k < 2; k++) {
                int i = r * 2 + k;
                LinearLayout.LayoutParams cl = lp(0, -2, 1f);
                if (k == 0) cl.setMarginEnd(dp(14));
                row.addView(reqCell(i, names[i], ts[i].color), cl);
            }
            grid.addView(row);
        }
        return grid;
    }

    private View reqCell(int i, String name, int color) {
        LinearLayout cell = new LinearLayout(this);
        cell.setGravity(Gravity.CENTER_VERTICAL);
        cell.addView(Ui.text(this, name, color, 9, true), lp(dp(44), -2, 0));
        LinearLayout track = new LinearLayout(this);
        track.setBackgroundColor(Ui.TRACK);
        fills[i] = new View(this);
        fills[i].setBackgroundColor(color);
        empties[i] = new View(this);
        track.addView(fills[i], lp(0, -1, 0));
        track.addView(empties[i], lp(0, -1, 1f));
        cell.addView(track, lp(0, dp(6), 1f));
        counts[i] = Ui.text(this, "0/0", Ui.MUTED, 9, false);
        counts[i].setGravity(Gravity.END);
        cell.addView(counts[i], lp(dp(32), -2, 0));
        return cell;
    }

    private View bottomBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.VERTICAL);
        bar.setPadding(dp(16), dp(8), dp(16), dp(12));
        bar.addView(Ui.text(this, "PASSWORD PROGRESS", Ui.MUTED, 9, false));
        LinearLayout row = new LinearLayout(this);
        row.setPadding(0, dp(6), 0, dp(8));
        slots = new TextView[mission.length];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = Ui.text(this, "", Ui.CYAN, mission.length > 8 ? 11 : 14, true);
            slots[i].setGravity(Gravity.CENTER);
            slots[i].setBackground(Ui.box(this, Ui.PANEL, Ui.EDGE, 3, 1));
            LinearLayout.LayoutParams sl = lp(0, dp(36), 1f);
            sl.setMargins(dp(2), 0, dp(2), 0);
            row.addView(slots[i], sl);
        }
        bar.addView(row);
        TextView hint = Ui.text(this, "TAP FALLING TOKENS TO COLLECT • DRAG SHIP TO DODGE", Ui.EDGE, 8, false);
        hint.setGravity(Gravity.CENTER);
        bar.addView(hint, lp(-1, -2, 0));
        return bar;
    }

    private void refresh(int score, int lives, String password) {
        tvScore.setText(String.valueOf(score));
        StringBuilder h = new StringBuilder();
        for (int i = 0; i < lives; i++) h.append("♥");
        tvHearts.setText(h);

        TokenType[] ts = TokenType.values();
        for (int i = 0; i < 4; i++) {
            int need = pw.required(ts[i]), have = pw.count(ts[i]);
            LinearLayout.LayoutParams f = (LinearLayout.LayoutParams) fills[i].getLayoutParams();
            LinearLayout.LayoutParams e = (LinearLayout.LayoutParams) empties[i].getLayoutParams();
            f.weight = Math.min(have, need);
            e.weight = need == 0 ? 1f : Math.max(0, need - have);
            fills[i].setLayoutParams(f);
            empties[i].setLayoutParams(e);
            counts[i].setText(have + "/" + need);
        }
        for (int i = 0; i < slots.length; i++) {
            if (i < password.length()) {
                char ch = password.charAt(i);
                int color = colorFor(ch);
                slots[i].setText(String.valueOf(ch));
                slots[i].setTextColor(color);
                slots[i].setBackground(Ui.box(this, Ui.PANEL, color, 3, 1));
            } else {
                slots[i].setText("");
                slots[i].setBackground(Ui.box(this, Ui.PANEL, Ui.EDGE, 3, 1));
            }
        }
    }

    private int colorFor(char ch) {
        if (Character.isUpperCase(ch)) return TokenType.UPPER.color;
        if (Character.isLowerCase(ch)) return TokenType.LOWER.color;
        if (Character.isDigit(ch)) return TokenType.NUM.color;
        return TokenType.SYMBOL.color;
    }

    private LinearLayout.LayoutParams lp(int w, int h, float weight) {
        return new LinearLayout.LayoutParams(w, h, weight);
    }

    private int dp(float v) { return Ui.dp(this, v); }
}
