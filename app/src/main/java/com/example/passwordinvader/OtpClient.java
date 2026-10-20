package com.example.passwordinvader;

import android.os.Handler;
import android.os.Looper;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

/** Talks to the otp-service (sauravhathi/otp-service). Callbacks arrive on the main thread. */
public final class OtpClient {
    private static final String BASE = "https://otp-service-zeta.vercel.app/api/otp";
    private static final ExecutorService EX = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public interface Callback { void done(boolean ok, String message); }

    private OtpClient() {}

    public static void generate(String email, Callback cb) {
        post("/generate", new String[][]{
                {"email", email}, {"type", "numeric"},
                {"organization", "Password Invader"}, {"subject", "Your Password Invader verification code"}}, cb);
    }

    public static void verify(String email, String otp, Callback cb) {
        post("/verify", new String[][]{{"email", email}, {"otp", otp}}, cb);
    }

    private static void post(String path, String[][] fields, Callback cb) {
        EX.execute(() -> {
            boolean ok = false;
            String msg;
            try {
                JSONObject body = new JSONObject();
                for (String[] f : fields) body.put(f[0], f[1]);
                HttpURLConnection c = (HttpURLConnection) new URL(BASE + path).openConnection();
                c.setRequestMethod("POST");
                c.setConnectTimeout(20000);
                c.setReadTimeout(60000);   // generous: the free-tier server/DB may be cold-starting
                c.setRequestProperty("Content-Type", "application/json");
                c.setDoOutput(true);
                try (OutputStream os = c.getOutputStream()) { os.write(body.toString().getBytes("UTF-8")); }
                int code = c.getResponseCode();
                InputStream is = code >= 400 ? c.getErrorStream() : c.getInputStream();
                String raw = is == null ? "" : read(is);
                ok = code >= 200 && code < 300;
                msg = message(raw, code);
            } catch (Exception e) {
                msg = "Network error (" + e.getClass().getSimpleName() + ")";
            }
            final boolean fOk = ok;
            final String fMsg = msg;
            MAIN.post(() -> cb.done(fOk, fMsg));
        });
    }

    private static String message(String raw, int code) {
        try {
            JSONObject j = new JSONObject(raw);
            String m = j.optString("message", j.optString("error", ""));
            return m.isEmpty() ? "HTTP " + code : m;
        } catch (Exception e) {
            return "HTTP " + code;
        }
    }

    private static String read(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        in.close();
        return out.toString("UTF-8");
    }
}
