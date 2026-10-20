package com.example.passwordinvader;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

/** Binds the shared keypad + dot indicator to a PIN buffer. */
public class PinPad {
    public interface Listener { void onChanged(String pin); }

    private final Activity act;
    private final int max;
    private final Listener listener;
    private final LinearLayout dotRow;
    private final StringBuilder pin = new StringBuilder();

    public PinPad(Activity act, int max, Listener listener) {
        this.act = act;
        this.max = max;
        this.listener = listener;
        this.dotRow = act.findViewById(R.id.dots);
        int[] ids = {R.id.key_0, R.id.key_1, R.id.key_2, R.id.key_3, R.id.key_4,
                R.id.key_5, R.id.key_6, R.id.key_7, R.id.key_8, R.id.key_9};
        for (int i = 0; i < ids.length; i++) {
            final String d = String.valueOf(i);
            ((Button) act.findViewById(ids[i])).setOnClickListener(v -> {
                if (pin.length() < max) { pin.append(d); refresh(false); }
            });
        }
        act.findViewById(R.id.key_del).setOnClickListener(v -> {
            if (pin.length() > 0) { pin.deleteCharAt(pin.length() - 1); refresh(false); }
        });
        act.findViewById(R.id.key_clr).setOnClickListener(v -> clear());
        buildDots();
        refresh(false);
    }

    private void buildDots() {
        float dp = act.getResources().getDisplayMetrics().density;
        for (int i = 0; i < max; i++) {
            View dot = new View(act);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) (10 * dp), (int) (10 * dp));
            lp.setMargins((int) (6 * dp), 0, (int) (6 * dp), 0);
            lp.gravity = Gravity.CENTER;
            dotRow.addView(dot, lp);
        }
    }

    public void refresh(boolean error) {
        for (int i = 0; i < max; i++) {
            int res = error ? R.drawable.dot_err : (i < pin.length() ? R.drawable.dot_on : R.drawable.dot_off);
            dotRow.getChildAt(i).setBackgroundResource(res);
        }
        listener.onChanged(pin.toString());
    }

    public void clear() { pin.setLength(0); refresh(false); }
    public String get() { return pin.toString(); }
    public boolean isComplete() { return pin.length() == max; }
}
