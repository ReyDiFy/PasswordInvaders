package com.example.passwordinvader;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        ScrollView sv = new ScrollView(this);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(20), dp(36), dp(20), dp(20));
        sv.addView(col);

        col.addView(Ui.text(this, "SETTINGS SYSTEM", Ui.CYAN, 22, true));
        TextView sub = Ui.text(this, "CONFIGURE CORE PARAMETERS", Ui.MUTED, 8, false);
        sub.setPadding(0, dp(4), 0, dp(24));
        col.addView(sub);

        col.addView(section("AUDIO CORE", Ui.CYAN));
        col.addView(volumeRow());
        col.addView(toggleRow("8-BIT MUSIC", Prefs.MUSIC, true));
        col.addView(toggleRow("RETRO SFX", Prefs.SFX, true));

        col.addView(section("CRT DISPLAY ENGINE", Ui.PURPLE));
        col.addView(toggleRow("CRT SCANLINES", Prefs.CRT, true));
        col.addView(toggleRow("SCREEN SHAKE", Prefs.SHAKE, false));

        col.addView(section("SECURE NODE", Ui.CYAN));
        col.addView(link("CHANGE SECURITY PIN", Ui.PURPLE, v -> changePin()));
        col.addView(link("EXPORT ENCRYPTED VAULT", Ui.CYAN,
                v -> Toast.makeText(this, "VAULT EXPORT COMING SOON", Toast.LENGTH_SHORT).show()));
        setContentView(sv);
    }

    private View section(String label, int color) {
        TextView t = Ui.text(this, label, color, 11, true);
        t.setPadding(0, dp(24), 0, dp(10));
        return t;
    }

    private View volumeRow() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        LinearLayout top = new LinearLayout(this);
        top.addView(Ui.text(this, "MASTER VOLUME", 0xFFB8C7D6, 9, false), new LinearLayout.LayoutParams(0, -2, 1f));
        TextView pct = Ui.text(this, Prefs.volume(this) + "%", Ui.CYAN, 9, true);
        top.addView(pct);
        box.addView(top);
        SeekBar sb = new SeekBar(this);
        sb.setMax(100);
        sb.setProgress(Prefs.volume(this));
        sb.setPadding(0, dp(6), 0, dp(6));
        sb.setProgressTintList(ColorStateList.valueOf(Ui.CYAN));
        sb.setThumbTintList(ColorStateList.valueOf(Ui.CYAN));
        sb.setProgressBackgroundTintList(ColorStateList.valueOf(Ui.EDGE));
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean user) { pct.setText(p + "%"); }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) { Prefs.setVolume(SettingsActivity.this, s.getProgress()); }
        });
        box.addView(sb, new LinearLayout.LayoutParams(-1, -2));
        return box;
    }

    private View toggleRow(String label, String key, boolean def) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(6), 0, dp(6));
        row.addView(Ui.text(this, label, 0xFFB8C7D6, 9, false), new LinearLayout.LayoutParams(0, -2, 1f));
        SwitchCompat sw = new SwitchCompat(this);
        int[][] states = {{android.R.attr.state_checked}, {}};
        sw.setThumbTintList(new ColorStateList(states, new int[]{Ui.GREEN, 0xFF8A94A6}));
        sw.setTrackTintList(new ColorStateList(states, new int[]{0x5539FF14, 0x33FFFFFF}));
        sw.setChecked(Prefs.flag(this, key, def));
        sw.setOnCheckedChangeListener((v, on) -> Prefs.setFlag(this, key, on));
        row.addView(sw);
        return row;
    }

    private View link(String label, int color, View.OnClickListener l) {
        TextView t = Ui.text(this, label, color, 11, false);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, dp(14), 0, dp(14));
        t.setOnClickListener(l);
        return t;
    }

    private void changePin() {
        AccountStore store = new AccountStore(this);
        if (!store.exists()) { Toast.makeText(this, "No account yet", Toast.LENGTH_SHORT).show(); return; }
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(8), dp(20), 0);
        EditText oldPin = pinField("Current PIN"), newPin = pinField("New 6-digit PIN");
        box.addView(oldPin);
        box.addView(newPin);
        new AlertDialog.Builder(this)
                .setTitle("Change security PIN")
                .setView(box)
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("SAVE", (d, w) -> {
                    String n = newPin.getText().toString();
                    if (!store.verify(oldPin.getText().toString())) toast("Current PIN is wrong");
                    else if (n.length() != 6) toast("New PIN must be 6 digits");
                    else { store.save(store.username(), n); toast("PIN updated"); }
                }).show();
    }

    private EditText pinField(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        e.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        return e;
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
    private int dp(float v) { return Ui.dp(this, v); }
}
