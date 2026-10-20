package com.example.passwordinvader;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {
    private TextView tvLevel, tvXp;
    private View barFill, barEmpty;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(20), dp(32), dp(20), dp(20));
        sv.addView(col);

        // level / XP strip
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        tvLevel = Ui.text(this, "LVL 1", 0xFFFFFFFF, 9, true);
        tvLevel.setPadding(dp(8), dp(4), dp(8), dp(4));
        tvLevel.setBackground(Ui.box(this, Ui.PURPLE, Ui.PURPLE, 2, 0));
        top.addView(tvLevel);
        LinearLayout meter = new LinearLayout(this);
        meter.setOrientation(LinearLayout.VERTICAL);
        LinearLayout track = new LinearLayout(this);
        track.setBackgroundColor(Ui.TRACK);
        barFill = new View(this);
        barFill.setBackgroundColor(Ui.PURPLE);
        barEmpty = new View(this);
        track.addView(barFill, lp(0, -1, 1f));
        track.addView(barEmpty, lp(0, -1, 1f));
        meter.addView(track, lp(-1, dp(6), 0));
        LinearLayout caps = new LinearLayout(this);
        caps.addView(Ui.text(this, "STATUS: ACTIVE", Ui.MUTED, 7, false), lp(0, -2, 1f));
        tvXp = Ui.text(this, "", Ui.MUTED, 7, false);
        caps.addView(tvXp);
        meter.addView(caps);
        LinearLayout.LayoutParams ml = lp(0, -2, 1f);
        ml.setMarginStart(dp(10));
        top.addView(meter, ml);
        col.addView(top);

        // title
        TextView t1 = center(Ui.text(this, "PASSWORD", Ui.CYAN, 34, true));
        t1.setPadding(0, dp(40), 0, 0);
        col.addView(t1, lp(-1, -2, 0));
        col.addView(center(Ui.text(this, "INVADER", Ui.PURPLE, 26, true)), lp(-1, -2, 0));
        TextView ver = center(Ui.text(this, "— RETRO SEC OPS v1.42 —", Ui.GREEN, 8, false));
        ver.setPadding(0, dp(6), 0, dp(36));
        col.addView(ver, lp(-1, -2, 0));

        // preview box
        LinearLayout prev = new LinearLayout(this);
        prev.setOrientation(LinearLayout.VERTICAL);
        prev.setGravity(Gravity.CENTER);
        prev.setBackground(Ui.box(this, Ui.PANEL, 0xFF1D3A4A, 6, 1));
        TextView icon = center(Ui.text(this, "✕", Ui.CYAN, 16, true));
        icon.setBackground(Ui.box(this, Ui.DARK, Ui.CYAN, 22, 2));
        prev.addView(icon, lp(dp(44), dp(44), 0));
        LinearLayout dots = new LinearLayout(this);
        dots.setGravity(Gravity.CENTER);
        dots.setPadding(0, dp(10), 0, 0);
        int[] widths = {8, 14, 8};
        for (int w : widths) {
            View v = new View(this);
            v.setBackgroundColor(w == 14 ? Ui.CYAN : 0xFF0A8F9A);
            LinearLayout.LayoutParams dl = lp(dp(w), dp(3), 0);
            dl.setMargins(dp(3), 0, dp(3), 0);
            dots.addView(v, dl);
        }
        prev.addView(dots);
        LinearLayout.LayoutParams pl = lp(dp(180), dp(110), 0);
        pl.gravity = Gravity.CENTER_HORIZONTAL;
        col.addView(prev, pl);

        // main buttons
        col.addView(bigButton("PLAY CAMPAIGN", Ui.CYAN, v -> startActivity(
                new Intent(this, MissionActivity.class).putExtra("level", Prefs.unlocked(this)))), margin(28));
        col.addView(bigButton("PASSWORD VAULT", Ui.PURPLE, v -> soon()), margin(12));

        LinearLayout r1 = new LinearLayout(this), r2 = new LinearLayout(this);
        r1.addView(smallButton("COLLECTION", v -> soon()), lp(0, dp(44), 1f));
        r1.addView(smallButton("CUSTOMIZE", v -> soon()), lp(0, dp(44), 1f));
        r2.addView(smallButton("STATISTICS", v -> soon()), lp(0, dp(44), 1f));
        r2.addView(smallButton("SETTINGS", v -> startActivity(new Intent(this, SettingsActivity.class))), lp(0, dp(44), 1f));
        col.addView(r1, margin(8));
        col.addView(r2, margin(0));

        View spacer = new View(this);
        col.addView(spacer, lp(0, 0, 1f));
        TextView alert = center(Ui.text(this,
                "ALERT: CRITICAL PASSWORD ENTROPY RECOMMENDED. ALWAYS SECURE YOUR CODES.", Ui.EDGE, 7, false));
        alert.setPadding(dp(16), dp(24), dp(16), 0);
        col.addView(alert, lp(-1, -2, 0));

        setContentView(sv);
    }

    @Override protected void onResume() {
        super.onResume();
        int xp = Prefs.xp(this), level = Prefs.levelFor(xp), into = xp % 500;
        tvLevel.setText("LVL " + level);
        tvXp.setText(xp + "/" + Prefs.nextXp(level) + " XP");
        LinearLayout.LayoutParams f = (LinearLayout.LayoutParams) barFill.getLayoutParams();
        LinearLayout.LayoutParams e = (LinearLayout.LayoutParams) barEmpty.getLayoutParams();
        f.weight = into;
        e.weight = 500 - into;
        barFill.setLayoutParams(f);
        barEmpty.setLayoutParams(e);
    }

    private void soon() { Toast.makeText(this, "COMING SOON", Toast.LENGTH_SHORT).show(); }

    private TextView bigButton(String label, int color, View.OnClickListener l) {
        TextView t = Ui.text(this, label, color, 12, true);
        t.setGravity(Gravity.CENTER);
        t.setBackground(Ui.box(this, (color & 0x00FFFFFF) | 0x1A000000, color, 4, 1));
        t.setOnClickListener(l);
        return t;
    }

    private TextView smallButton(String label, View.OnClickListener l) {
        TextView t = Ui.text(this, label, 0xFF7FD6E0, 9, false);
        t.setGravity(Gravity.CENTER);
        t.setBackground(Ui.box(this, Ui.PANEL, 0xFF1D3A4A, 3, 1));
        t.setOnClickListener(l);
        LinearLayout.LayoutParams p = lp(0, dp(44), 1f);
        p.setMargins(dp(4), 0, dp(4), 0);
        t.setLayoutParams(p);
        return t;
    }

    private LinearLayout.LayoutParams margin(int topDp) {
        LinearLayout.LayoutParams p = lp(-1, dp(46), 0);
        p.topMargin = dp(topDp);
        return p;
    }

    private TextView center(TextView t) { t.setGravity(Gravity.CENTER); return t; }
    private LinearLayout.LayoutParams lp(int w, int h, float wt) { return new LinearLayout.LayoutParams(w, h, wt); }
    private int dp(float v) { return Ui.dp(this, v); }
}
