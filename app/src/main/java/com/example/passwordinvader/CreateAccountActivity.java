package com.example.passwordinvader;

import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/** Username + email + 6-digit key. The account is only saved after the emailed OTP is verified. */
public class CreateAccountActivity extends AppCompatActivity {
    private PinPad pad;
    private EditText etUser, etEmail;
    private Button btn;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_create_account);
        TextView tvPin = findViewById(R.id.tvPin);
        etUser = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        btn = findViewById(R.id.btnCreate);
        pad = new PinPad(this, 6, pin -> {
            StringBuilder dots = new StringBuilder();
            for (int i = 0; i < pin.length(); i++) dots.append('\u2022');
            tvPin.setText(pin.isEmpty() ? "Input" : dots);
            tvPin.setTextColor(pin.isEmpty() ? 0xFF79747E : 0xFF1D1B20);
        });
        btn.setOnClickListener(v -> start());
    }

    private void start() {
        String u = etUser.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        if (u.length() < 3) { toast("Username needs 3+ characters"); return; }
        if (new AccountStore(this).usernameTaken(u)) { toast("That username already exists"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { toast("Enter a valid email"); return; }
        if (!pad.isComplete()) { toast("Key must be 6 digits"); return; }

        setBusy(true);
        toast("Sending code... the server can take a moment to wake up");
        OtpClient.generate(email, (ok, msg) -> {
            setBusy(false);
            if (isFinishing() || isDestroyed()) return;
            if (ok) showCodeDialog(u, email, pad.get());
            else toast("Couldn't send code: " + msg);
        });
    }

    private void showCodeDialog(String user, String email, String pin) {
        EditText code = new EditText(this);
        code.setInputType(InputType.TYPE_CLASS_NUMBER);
        code.setHint("Verification code");
        code.setPadding(48, 32, 48, 32);

        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle("Verify your email")
                .setMessage("We sent a code to " + email)
                .setView(code)
                .setPositiveButton("VERIFY", null)     // listeners set below so the dialog stays open on failure
                .setNeutralButton("RESEND", null)
                .setNegativeButton("CANCEL", null)
                .setCancelable(false)
                .create();
        d.show();

        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String otp = code.getText().toString().trim();
            if (otp.isEmpty()) return;
            v.setEnabled(false);
            OtpClient.verify(email, otp, (ok, msg) -> {
                v.setEnabled(true);
                if (ok) {
                    AccountStore store = new AccountStore(this);
                    store.save(user, pin);
                    store.saveEmail(email);
                    d.dismiss();
                    toast("Agent registered");
                    finish();
                } else {
                    toast("Verification failed: " + msg);
                }
            });
        });
        d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v ->
                OtpClient.generate(email, (ok, msg) -> toast(ok ? "Code resent" : "Couldn't resend: " + msg)));
        d.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> d.dismiss());
    }

    private void setBusy(boolean busy) {
        btn.setEnabled(!busy);
        btn.setText(busy ? "SENDING CODE..." : "CREATE");
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}
