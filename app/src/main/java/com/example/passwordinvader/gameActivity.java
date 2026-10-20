package com.example.passwordinvader;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class gameActivity extends AppCompatActivity {
    private GameView game;
    private TextView hud;
    private boolean left, right;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        FrameLayout root = new FrameLayout(this);
        game = new GameView(this);
        root.addView(game);

        hud = new TextView(this);
        hud.setTextColor(getColor(R.color.cyan));
        hud.setTypeface(android.graphics.Typeface.MONOSPACE);
        hud.setTextSize(13);
        hud.setPadding(dp(16), dp(12), dp(16), 0);
        root.addView(hud, new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.START));

        // Overlay controls (regular Views so they can be styled like the mockup)
        Button l = pad("◀"), r = pad("▶"), fire = pad("FIRE");
        fire.setTextColor(getColor(R.color.danger));
        hold(l, true);
        hold(r, false);
        fire.setOnClickListener(v -> game.fire());
        root.addView(l, ctl(Gravity.BOTTOM | Gravity.START, 16, 16, 64));
        root.addView(r, ctl(Gravity.BOTTOM | Gravity.START, 88, 16, 64));
        root.addView(fire, ctl(Gravity.BOTTOM | Gravity.END, 16, 16, 80));
        setContentView(root);

        game.setListener(new GameView.Listener() {
            @Override
            public void onStats(int score, int lives, String password, int passwordLength) {

            }

            @Override
            public void onEnd(boolean won, int score, String password) {

            }

            public void onStats(int score, int lives) {
                runOnUiThread(() -> hud.setText("SCORE " + score + "   HP " + "♥".repeat(Math.max(0, lives))));
            }
            public void onEnd(boolean won, int score) {
                runOnUiThread(() -> {
                    setResult(RESULT_OK, new Intent().putExtra("won", won).putExtra("score", score));
                    finish();   // TODO: go to the password-result screen instead
                });
            }
        });
    }

    private void hold(Button btn, boolean isLeft) {
        btn.setOnTouchListener((v, e) -> {
            int a = e.getActionMasked();
            if (a == MotionEvent.ACTION_DOWN) { if (isLeft) left = true; else right = true; }
            else if (a == MotionEvent.ACTION_UP || a == MotionEvent.ACTION_CANCEL) { if (isLeft) left = false; else right = false; }
            game.setMoveDir((right ? 1 : 0) - (left ? 1 : 0));
            return true;
        });
    }

    private Button pad(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setBackgroundResource(R.drawable.key_bg);
        b.setTextColor(getColor(R.color.cyan));
        b.setTypeface(android.graphics.Typeface.MONOSPACE);
        b.setAlpha(0.85f);
        return b;
    }

    private FrameLayout.LayoutParams ctl(int gravity, int startDp, int bottomDp, int sizeDp) {
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(dp(sizeDp), dp(56), gravity);
        p.setMarginStart(dp(startDp)); p.setMarginEnd(dp(startDp)); p.bottomMargin = dp(bottomDp);
        return p;
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}
