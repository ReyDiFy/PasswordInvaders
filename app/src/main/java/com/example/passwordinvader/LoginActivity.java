package com.example.passwordinvader;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private PinPad pad;
    private AccountStore store;
    private TextView hint, agent;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login);
        store = new AccountStore(this);
        hint = findViewById(R.id.hint);
        agent = findViewById(R.id.agentName);
        ((TextView) findViewById(R.id.agentRole)).setText("TAP TO SWITCH AGENT");
        pad = new PinPad(this, 6, pin -> {
            hint.setText("ENTER 6-DIGIT SECURITY KEY");
            hint.setTextColor(getColor(R.color.cyan));
        });
        findViewById(R.id.btnAuth).setOnClickListener(v -> authenticate());
        findViewById(R.id.linkCreate).setOnClickListener(v ->
                startActivity(new Intent(this, CreateAccountActivity.class)));
        findViewById(R.id.agentCard).setOnClickListener(v -> showAccountPicker());
    }

    @Override protected void onResume() {
        super.onResume();
        refreshAgent();   // also picks up a newly created account, which becomes active
    }

    private void refreshAgent() {
        agent.setText(store.exists() ? store.username().toUpperCase() : "NO AGENT FOUND");
        pad.clear();
    }

    private void showAccountPicker() {
        List<String> names = store.usernames();
        String[] items = new String[names.size() + 1];
        for (int i = 0; i < names.size(); i++) items[i] = names.get(i);
        items[names.size()] = "+ CREATE NEW ACCOUNT";

        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle("Switch agent (hold to remove)")
                .setItems(items, (dlg, which) -> {
                    if (which == names.size()) {
                        startActivity(new Intent(this, CreateAccountActivity.class));
                    } else {
                        store.switchTo(names.get(which));
                        refreshAgent();
                    }
                })
                .setNegativeButton("CANCEL", null)
                .create();
        d.show();
        d.getListView().setOnItemLongClickListener((parent, view, pos, id) -> {
            if (pos < names.size()) confirmRemove(names.get(pos), d);
            return true;
        });
    }

    private void confirmRemove(String name, AlertDialog picker) {
        new AlertDialog.Builder(this)
                .setTitle("Remove " + name + "?")
                .setMessage("This deletes the account from this device.")
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("REMOVE", (dlg, w) -> {
                    store.remove(name);
                    picker.dismiss();
                    refreshAgent();
                }).show();
    }

    private void authenticate() {
        if (!store.exists()) { fail("NO ACCOUNT - CREATE ONE FIRST"); return; }
        if (!pad.isComplete()) { fail("KEY MUST BE 6 DIGITS"); return; }
        if (store.verify(pad.get())) {
            startActivity(new Intent(this, MainMenuActivity.class));
            finish();
        } else {
            fail("ACCESS DENIED");
            pad.clear();
            pad.refresh(true);
        }
    }

    private void fail(String msg) {
        hint.setText(msg);
        hint.setTextColor(getColor(R.color.danger));
        TranslateAnimation shake = new TranslateAnimation(0, 14, 0, 0);
        shake.setDuration(300);
        shake.setInterpolator(new CycleInterpolator(4));
        findViewById(R.id.dots).startAnimation(shake);
    }
}
