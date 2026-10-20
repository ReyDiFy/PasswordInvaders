package com.example.passwordinvader;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Multiple local accounts (username, email, salted SHA-256 of the PIN).
 * exists()/username()/email()/verify()/saveEmail() all act on the ACTIVE account.
 */
public class AccountStore {
    private final SharedPreferences sp;

    public AccountStore(Context c) {
        sp = c.getSharedPreferences("account", Context.MODE_PRIVATE);
        migrateLegacy();
    }

    // ---------- public API ----------

    public boolean exists() { return current() != null; }

    public String username() {
        JSONObject o = current();
        return o == null ? null : o.optString("user");
    }

    public String email() {
        JSONObject o = current();
        String e = o == null ? "" : o.optString("email", "");
        return e.isEmpty() ? null : e;
    }

    public List<String> usernames() {
        List<String> out = new ArrayList<>();
        JSONArray a = all();
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o != null) out.add(o.optString("user"));
        }
        return out;
    }

    public boolean usernameTaken(String user) { return find(user) != null; }

    public void switchTo(String user) {
        JSONObject o = find(user);
        if (o != null) sp.edit().putString("active", o.optString("user")).apply();
    }

    /** Creates the account (or updates its PIN if the username exists) and makes it active. */
    public void save(String user, String pin) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        String s = Base64.encodeToString(salt, Base64.NO_WRAP);
        JSONObject old = find(user);
        String email = old == null ? "" : old.optString("email", "");
        try {
            JSONObject o = new JSONObject();
            o.put("user", user);
            o.put("salt", s);
            o.put("hash", hash(s, pin));
            o.put("email", email);
            put(o);
        } catch (JSONException e) { throw new RuntimeException(e); }
        sp.edit().putString("active", user).apply();
    }

    public void saveEmail(String email) {
        JSONObject o = current();
        if (o == null) return;
        try { o.put("email", email); put(o); } catch (JSONException ignored) {}
    }

    public boolean verify(String pin) {
        JSONObject o = current();
        return o != null && hash(o.optString("salt"), pin).equals(o.optString("hash"));
    }

    public void remove(String user) {
        JSONArray a = all(), out = new JSONArray();
        String first = null;
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o == null || o.optString("user").equalsIgnoreCase(user)) continue;
            out.put(o);
            if (first == null) first = o.optString("user");
        }
        SharedPreferences.Editor ed = sp.edit().putString("accounts", out.toString());
        if (user.equalsIgnoreCase(sp.getString("active", ""))) ed.putString("active", first == null ? "" : first);
        ed.apply();
    }

    // ---------- internals ----------

    private JSONArray all() {
        try { return new JSONArray(sp.getString("accounts", "[]")); }
        catch (JSONException e) { return new JSONArray(); }
    }

    private JSONObject find(String user) {
        if (user == null) return null;
        JSONArray a = all();
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o != null && o.optString("user").equalsIgnoreCase(user)) return o;
        }
        return null;
    }

    private JSONObject current() {
        JSONObject o = find(sp.getString("active", ""));
        if (o != null) return o;
        JSONArray a = all();
        return a.length() > 0 ? a.optJSONObject(0) : null;
    }

    private void put(JSONObject account) {
        JSONArray a = all(), out = new JSONArray();
        boolean replaced = false;
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o == null) continue;
            if (o.optString("user").equalsIgnoreCase(account.optString("user"))) { out.put(account); replaced = true; }
            else out.put(o);
        }
        if (!replaced) out.put(account);
        sp.edit().putString("accounts", out.toString()).apply();
    }

    /** Converts the old single-account format. */
    private void migrateLegacy() {
        if (sp.contains("accounts") || !sp.contains("hash")) return;
        try {
            JSONObject o = new JSONObject();
            o.put("user", sp.getString("user", "agent"));
            o.put("salt", sp.getString("salt", ""));
            o.put("hash", sp.getString("hash", ""));
            o.put("email", sp.getString("email", ""));
            sp.edit().putString("accounts", new JSONArray().put(o).toString())
                    .putString("active", o.getString("user"))
                    .remove("user").remove("salt").remove("hash").remove("email").apply();
        } catch (JSONException ignored) {}
    }

    private static String hash(String salt, String pin) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest((salt + pin).getBytes("UTF-8"));
            return Base64.encodeToString(d, Base64.NO_WRAP);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
