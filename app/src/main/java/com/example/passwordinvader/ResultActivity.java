package com.example.passwordinvader;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

/** Battle end screen. Extras: won, score, password, level, lives, elapsedMs. */
public class ResultActivity extends AppCompatActivity {
    private static final int[] SEG = {Ui.RED, Ui.YELLOW, 0xFF9BE000, Ui.GREEN, Ui.GREEN};
    private int level;
    private String pw;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Intent in = getIntent();
        boolean won = in.getBooleanExtra("won", false);
        int score = in.getIntExtra("score", 0), lives = in.getIntExtra("lives", 0);
        long sec = in.getLongExtra("elapsedMs", 0) / 1000;
        level = in.getIntExtra("level", 1);
        pw = in.getStringExtra("password");
        if (pw == null) pw = "";
        if (won && b == null) {                       // award once, not on rotation
            Prefs.addXp(this, score / 5);
            Prefs.unlock(this, level + 1);
        }
        int accent = won ? Ui.GREEN : Ui.RED;

        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        LinearLayout col = vbox();
        col.setPadding(dp(20), dp(36), dp(20), dp(20));
        sv.addView(col);

        col.addView(center(Ui.text(this, won ? "PASSWORD GENERATED" : "MISSION FAILED", accent, 20, true)));
        TextView sub = center(Ui.text(this, won ? "DECRYPTION SUCCESSFUL" : "CONNECTION TERMINATED", Ui.MUTED, 8, false));
        sub.setPadding(0, dp(6), 0, dp(20));
        col.addView(sub);

        if (won) {
            // password panel
            LinearLayout panel = vbox();
            panel.setPadding(dp(10), dp(12), dp(10), dp(10));
            panel.setBackground(Ui.box(this, Ui.PANEL, 0xFF1D3A4A, 6, 1));
            LinearLayout slots = new LinearLayout(this);
            for (char ch : pw.toCharArray()) {
                TextView t = Ui.text(this, String.valueOf(ch), colorFor(ch), pw.length() > 12 ? 11 : 14, true);
                t.setGravity(Gravity.CENTER);
                t.setBackground(Ui.box(this, Ui.DARK, colorFor(ch), 3, 1));
                LinearLayout.LayoutParams l = lp(0, dp(34), 1f);
                l.setMargins(dp(2), 0, dp(2), 0);
                slots.addView(t, l);
            }
            panel.addView(slots);
            LinearLayout actions = new LinearLayout(this);
            actions.setGravity(Gravity.CENTER);
            actions.setPadding(0, dp(8), 0, 0);
            actions.addView(action("❐ COPY", Ui.CYAN, v -> copy()));
            actions.addView(action("▣ SAVE TO VAULT", Ui.PURPLE, v ->
                    Toast.makeText(this, "VAULT COMING SOON", Toast.LENGTH_SHORT).show()));   // TODO: encrypted storage
            panel.addView(actions);
            col.addView(panel);

            // strength
            double bits = 0;
            int pool = 0;
            boolean u = false, l = false, d = false, s = false;
            for (char ch : pw.toCharArray()) {
                if (Character.isUpperCase(ch)) u = true;
                else if (Character.isLowerCase(ch)) l = true;
                else if (Character.isDigit(ch)) d = true;
                else s = true;
            }
            pool = (u ? 26 : 0) + (l ? 26 : 0) + (d ? 10 : 0) + (s ? 13 : 0);
            if (pool > 0) bits = pw.length() * Math.log(pool) / Math.log(2);
            int pct = (int) Math.max(5, Math.min(99, bits / 80 * 100));
            String label = pct < 35 ? "WEAK" : pct < 60 ? "FAIR" : pct < 80 ? "STRONG" : "ULTRA SECURE";
            int lit = Math.max(1, (pct + 19) / 20);

            LinearLayout sr = new LinearLayout(this);
            sr.setPadding(0, dp(18), 0, dp(6));
            sr.addView(Ui.text(this, "STRENGTH RATING:", Ui.MUTED, 8, false), lp(0, -2, 1f));
            sr.addView(Ui.text(this, label + " (" + pct + "%)", SEG[lit - 1], 9, true));
            col.addView(sr);
            LinearLayout seg = new LinearLayout(this);
            for (int i = 0; i < 5; i++) {
                View v = new View(this);
                v.setBackgroundColor(i < lit ? SEG[i] : Ui.TRACK);
                LinearLayout.LayoutParams sl = lp(0, dp(6), 1f);
                sl.setMargins(dp(2), 0, dp(2), 0);
                seg.addView(v, sl);
            }
            col.addView(seg);
        }

        // stars
        TextView earned = center(Ui.text(this, "RATING EARNED", Ui.MUTED, 8, false));
        earned.setPadding(0, dp(20), 0, dp(6));
        col.addView(earned);
        int stars = won ? Math.max(1, Math.min(3, lives)) : 0;
        LinearLayout starRow = new LinearLayout(this);
        starRow.setGravity(Gravity.CENTER);
        for (int i = 0; i < 3; i++) {
            TextView st = Ui.text(this, "★", i < stars ? Ui.YELLOW : Ui.EDGE, 26, false);
            st.setPadding(dp(5), 0, dp(5), 0);
            starRow.addView(st);
        }
        col.addView(starRow);

        // stats
        LinearLayout stats = vbox();
        stats.setPadding(dp(14), dp(12), dp(14), dp(8));
        stats.setBackground(Ui.box(this, Ui.PANEL, Ui.PURPLE, 6, 1));
        stats.addView(Ui.text(this, "MISSION STATISTICS", Ui.PURPLE, 10, true));
        stats.addView(statRow("TOKENS ACQUIRED", pw.length() + " Characters", 0xFFFFFFFF));
        stats.addView(statRow("ELAPSED TIME", String.format(Locale.US, "%02d:%02d Min", sec / 60, sec % 60), 0xFFFFFFFF));
        stats.addView(statRow("FINAL MATCH SCORE", "+" + String.format(Locale.US, "%,d", score) + " PTS", Ui.CYAN));
        LinearLayout.LayoutParams sp = lp(-1, -2, 0);
        sp.topMargin = dp(22);
        col.addView(stats, sp);

        // buttons
        col.addView(button(won ? "ACCEPT & RETURN" : "RETURN TO MENU", won ? Ui.GREEN : Ui.MUTED, v -> {
            startActivity(new Intent(this, MainMenuActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
        }), btnLp(24));
        col.addView(button(won ? "REGENERATE PASSWORD" : "RETRY MISSION", Ui.CYAN, v -> {
            startActivity(new Intent(this, GameActivity.class).putExtra("level", level));
            finish();
        }), btnLp(10));
        setContentView(sv);
    }

    private void copy() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("password", pw);
        if (Build.VERSION.SDK_INT >= 33) {            // hide the password from the clipboard preview
            PersistableBundle pb = new PersistableBundle();
            pb.putBoolean("android.content.extra.IS_SENSITIVE", true);
            clip.getDescription().setExtras(pb);
        }
        cm.setPrimaryClip(clip);
        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
    }

    private View statRow(String label, String value, int valueColor) {
        LinearLayout r = new LinearLayout(this);
        r.setPadding(0, dp(9), 0, dp(9));
        r.addView(Ui.text(this, label, Ui.MUTED, 8, false), lp(0, -2, 1f));
        r.addView(Ui.text(this, value, valueColor, 9, true));
        return r;
    }

    private TextView action(String label, int color, View.OnClickListener l) {
        TextView t = Ui.text(this, label, color, 8, false);
        t.setPadding(dp(14), dp(6), dp(14), dp(6));
        t.setOnClickListener(l);
        return t;
    }

    private TextView button(String label, int color, View.OnClickListener l) {
        TextView t = Ui.text(this, label, color, 11, true);
        t.setGravity(Gravity.CENTER);
        t.setBackground(Ui.box(this, (color & 0x00FFFFFF) | 0x14000000, color, 4, 1));
        t.setOnClickListener(l);
        return t;
    }

    private LinearLayout.LayoutParams btnLp(int topDp) {
        LinearLayout.LayoutParams p = lp(-1, dp(44), 0);
        p.topMargin = dp(topDp);
        return p;
    }

    private int colorFor(char ch) {
        if (Character.isUpperCase(ch)) return TokenType.UPPER.color;
        if (Character.isLowerCase(ch)) return TokenType.LOWER.color;
        if (Character.isDigit(ch)) return TokenType.NUM.color;
        return TokenType.SYMBOL.color;
    }

    private LinearLayout vbox() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private TextView center(TextView t) { t.setGravity(Gravity.CENTER); return t; }
    private LinearLayout.LayoutParams lp(int w, int h, float wt) { return new LinearLayout.LayoutParams(w, h, wt); }
    private int dp(float v) { return Ui.dp(this, v); }
}
