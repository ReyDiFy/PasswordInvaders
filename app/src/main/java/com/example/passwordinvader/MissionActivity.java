package com.example.passwordinvader;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

/** Mission briefing screen. Launch with intent extra "level" (default 1). */
public class MissionActivity extends AppCompatActivity {
    private static final int LINE = 0xFF17283A, CARD_EDGE = 0xFF1D3A4A;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        final int level = getIntent().getIntExtra("level", 1);
        Mission m = Mission.forLevel(level);

        LinearLayout root = vbox();
        ScrollView sv = new ScrollView(this);
        LinearLayout col = vbox();
        col.setPadding(dp(16), dp(16), dp(16), dp(8));
        sv.addView(col);
        root.addView(sv, lp(-1, 0, 1f));

        // header
        LinearLayout head = row();
        TextView back = Ui.text(this, "‹", Ui.CYAN, 22, true);
        back.setGravity(Gravity.CENTER);
        back.setBackground(Ui.box(this, Ui.PANEL, Ui.CYAN, 4, 1));
        back.setOnClickListener(v -> finish());
        head.addView(back, lp(dp(40), dp(40), 0));
        TextView title = Ui.text(this, String.format(Locale.US, "MISSION %02d", level), Ui.CYAN, 18, true);
        LinearLayout.LayoutParams tl = lp(-2, -2, 0);
        tl.setMarginStart(dp(16));
        head.addView(title, tl);
        col.addView(head);

        // difficulty row
        LinearLayout diff = row();
        diff.setPadding(0, dp(24), 0, dp(16));
        TextView badge = Ui.text(this, m.difficulty, m.color, 13, true);
        badge.setPadding(dp(18), dp(8), dp(18), dp(8));
        badge.setBackground(Ui.box(this, (m.color & 0x00FFFFFF) | 0x22000000, m.color, 3, 2));
        diff.addView(badge);
        View line = new View(this);
        line.setBackgroundColor(LINE);
        LinearLayout.LayoutParams ll = lp(0, dp(1), 1f);
        ll.setMargins(dp(12), 0, dp(12), 0);
        diff.addView(line, ll);
        diff.addView(Ui.text(this, m.tagline, Ui.MUTED, 11, false));
        col.addView(diff);

        // briefing card
        LinearLayout card = vbox();
        card.setPadding(dp(16), dp(14), dp(16), dp(6));
        card.setBackground(Ui.box(this, Ui.PANEL, Ui.CYAN, 4, 2));
        card.addView(Ui.text(this, "MISSION BRIEFING", Ui.MUTED, 10, false));
        card.addView(infoRow("TARGET PASSWORD LENGTH", m.length + " CHARS"));
        card.addView(infoRow("ENEMY SPEED", m.speedLabel));
        card.addView(infoRow("DIFFICULTY", m.difficulty));
        card.addView(infoRow("REWARD", "SECURE PASSWORD"));
        col.addView(card);

        // requirements
        TextView rh = Ui.text(this, "PASSWORD REQUIREMENTS", Ui.MUTED, 10, false);
        rh.setGravity(Gravity.CENTER);
        rh.setPadding(0, dp(22), 0, dp(12));
        col.addView(rh, lp(-1, -2, 0));
        LinearLayout r1 = row(), r2 = row();
        r1.addView(reqCard(m.upper, "UPPERCASE", "A-Z", TokenType.UPPER.color), lp(0, -2, 1f));
        r1.addView(reqCard(m.lower, "LOWERCASE", "a-z", TokenType.LOWER.color), lp(0, -2, 1f));
        r2.addView(reqCard(m.num, "NUMBERS", "0-9", TokenType.NUM.color), lp(0, -2, 1f));
        r2.addView(reqCard(m.symbol, "SYMBOLS", "!@#$%&*", TokenType.SYMBOL.color), lp(0, -2, 1f));
        col.addView(r1);
        col.addView(r2);

        // challenge
        LinearLayout ch = vbox();
        ch.setPadding(dp(16), dp(14), dp(16), dp(14));
        ch.setBackground(Ui.box(this, Ui.PANEL, CARD_EDGE, 4, 1));
        ch.addView(Ui.text(this, "MISSION CHALLENGE", Ui.MUTED, 10, false));
        ch.addView(meter("Combat Intensity", m.combatPct, Ui.RED));
        ch.addView(meter("Token Frequency", m.tokenPct, Ui.CYAN));
        ch.addView(meter("Enemy Speed", m.speedPct, Ui.YELLOW));
        LinearLayout.LayoutParams cl = lp(-1, -2, 0);
        cl.topMargin = dp(20);
        col.addView(ch, cl);

        // bottom buttons
        LinearLayout bar = row();
        bar.setPadding(dp(16), dp(8), dp(16), dp(16));
        TextView backBtn = Ui.text(this, "BACK", Ui.MUTED, 12, true);
        backBtn.setGravity(Gravity.CENTER);
        backBtn.setBackground(Ui.box(this, Ui.PANEL, Ui.EDGE, 3, 1));
        backBtn.setOnClickListener(v -> finish());
        bar.addView(backBtn, lp(0, dp(52), 1f));
        TextView start = Ui.text(this, "START MISSION", Ui.DARK, 14, true);
        start.setGravity(Gravity.CENTER);
        start.setBackground(Ui.box(this, Ui.CYAN, Ui.CYAN, 3, 1));
        start.setOnClickListener(v -> startActivity(new Intent(this, GameActivity.class).putExtra("level", level)));
        LinearLayout.LayoutParams sl = lp(0, dp(52), 3f);
        sl.setMarginStart(dp(12));
        bar.addView(start, sl);
        root.addView(bar);

        setContentView(root);
    }

    private View infoRow(String label, String value) {
        LinearLayout r = row();
        r.setPadding(0, dp(12), 0, dp(12));
        r.addView(Ui.text(this, label, Ui.MUTED, 11, false), lp(0, -2, 1f));
        r.addView(Ui.text(this, value, Ui.CYAN, 12, true));
        r.setBackground(Ui.box(this, 0, 0, 0, 0));
        LinearLayout wrap = vbox();
        wrap.addView(r);
        View d = new View(this);
        d.setBackgroundColor(LINE);
        wrap.addView(d, lp(-1, dp(1), 0));
        return wrap;
    }

    private View reqCard(int n, String label, String chars, int color) {
        LinearLayout c = vbox();
        c.setGravity(Gravity.CENTER);
        c.setPadding(dp(8), dp(14), dp(8), dp(14));
        c.setBackground(Ui.box(this, Ui.PANEL, CARD_EDGE, 4, 1));
        c.addView(Ui.text(this, String.valueOf(n), color, 24, true));
        c.addView(Ui.text(this, label, Ui.MUTED, 9, false));
        TextView ch = Ui.text(this, chars, Ui.MUTED, 12, false);
        ch.setPadding(0, dp(4), 0, 0);
        c.addView(ch);
        LinearLayout holder = new LinearLayout(this);
        holder.setPadding(dp(5), dp(5), dp(5), dp(5));
        holder.addView(c, lp(-1, -2, 1f));
        return holder;
    }

    private View meter(String label, int pct, int color) {
        LinearLayout box = vbox();
        box.setPadding(0, dp(14), 0, 0);
        LinearLayout top = row();
        top.addView(Ui.text(this, label, 0xFFB8C7D6, 12, false), lp(0, -2, 1f));
        top.addView(Ui.text(this, pct + "%", color, 11, true));
        box.addView(top);
        LinearLayout track = new LinearLayout(this);
        track.setBackgroundColor(Ui.TRACK);
        View fill = new View(this);
        fill.setBackgroundColor(color);
        track.addView(fill, lp(0, -1, pct));
        track.addView(new View(this), lp(0, -1, 100 - pct));
        LinearLayout.LayoutParams tl = lp(-1, dp(8), 0);
        tl.topMargin = dp(6);
        box.addView(track, tl);
        return box;
    }

    private LinearLayout vbox() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    private LinearLayout.LayoutParams lp(int w, int h, float weight) {
        return new LinearLayout.LayoutParams(w, h, weight);
    }

    private int dp(float v) { return Ui.dp(this, v); }
}
