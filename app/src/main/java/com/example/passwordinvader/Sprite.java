package com.example.passwordinvader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Paint;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A sprite loaded from assets/sprites/<name>.png or .gif (GIF wins if both exist).
 * Every frame is pre-scaled and gets an opaque-pixel mask, so collisions are pixel-perfect.
 * Instances made with copy() share bitmaps/masks but have their own position and animation clock.
 */
public class Sprite {
    private static final int ALPHA_THRESHOLD = 40;   // pixel counts as "solid" above this alpha (0-255)
    private static final int MAX_GIF_FRAMES = 60;

    private final Bitmap[] frames;
    private final boolean[][] masks;
    private final int[] delays;   // ms per frame
    public final int w, h;
    public float x, y;
    private int frame;
    private long clock;

    private Sprite(Bitmap[] frames, boolean[][] masks, int[] delays) {
        this.frames = frames;
        this.masks = masks;
        this.delays = delays;
        this.w = frames[0].getWidth();
        this.h = frames[0].getHeight();
    }
    /** Pixel-perfect test of this sprite's solid pixels against a plain rectangle. */
    public boolean collidesRect(float rx, float ry, int rw, int rh) {
        int ax = Math.round(x), ay = Math.round(y), bx = Math.round(rx), by = Math.round(ry);
        int left = Math.max(ax, bx), right = Math.min(ax + w, bx + rw);
        int top = Math.max(ay, by), bottom = Math.min(ay + h, by + rh);
        if (right <= left || bottom <= top) return false;
        boolean[] a = masks[frame];
        for (int yy = top; yy < bottom; yy++) {
            int ai = (yy - ay) * w + (left - ax);
            for (int xx = left; xx < right; xx++, ai++) if (a[ai]) return true;
        }
        return false;
    }
    // ---------- loading ----------

    /** base like "sprites/alien" (no extension). h <= 0 keeps the source aspect ratio. */
    public static Sprite loadAny(Context c, String base, int w, int h, int fallbackColor) {

        for (String ext : new String[]{".gif", ".png"}) {
            try (InputStream in = c.getAssets().open(base + ext)) {
                System.out.println("Loaded " + base);
                byte[] data = readAll(in);
                Sprite s = ext.equals(".gif") ? fromGif(data, w, h) : fromPng(data, w, h);
                if (s != null) return s;
            } catch (IOException ignored) { System.err.println("Asset NOT found: " + base + ext + " - " + ignored.getMessage()); }
        }
        return placeholder(w, h > 0 ? h : w, fallbackColor);
    }

    private static Sprite fromPng(byte[] data, int w, int h) {
        Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
        return b == null ? null : build(new Bitmap[]{b}, new int[]{1000}, w, h);
    }

    /** Movie is deprecated but still decodes GIFs on every API level; we bake it into plain frames. */
    private static Sprite fromGif(byte[] data, int w, int h) {
        Movie m = Movie.decodeByteArray(data, 0, data.length);
        if (m == null || m.width() <= 0 || m.height() <= 0) return null;
        int dur = m.duration();
        int n = dur <= 0 ? 1 : Math.min(MAX_GIF_FRAMES, Math.max(1, dur / 40));
        int step = dur <= 0 ? 1000 : Math.max(1, dur / n);
        Bitmap[] raw = new Bitmap[n];
        int[] delays = new int[n];
        for (int i = 0; i < n; i++) {
            Bitmap b = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(b);
            m.setTime(i * step);
            m.draw(canvas, 0, 0);
            raw[i] = b;
            delays[i] = step;
        }
        return build(raw, delays, w, h);
    }

    private static Sprite build(Bitmap[] raw, int[] delays, int w, int h) {
        if (h <= 0) h = Math.max(1, w * raw[0].getHeight() / raw[0].getWidth());
        Bitmap[] frames = new Bitmap[raw.length];
        boolean[][] masks = new boolean[raw.length][];
        int[] px = new int[w * h];
        for (int i = 0; i < raw.length; i++) {
            frames[i] = Bitmap.createScaledBitmap(raw[i], w, h, false); // no smoothing = crisp pixel art
            frames[i].getPixels(px, 0, w, 0, 0, w, h);
            boolean[] mask = new boolean[w * h];
            for (int p = 0; p < mask.length; p++) mask[p] = (px[p] >>> 24) > ALPHA_THRESHOLD;
            masks[i] = mask;
        }
        return new Sprite(frames, masks, delays);
    }

    private static Sprite placeholder(int w, int h, int color) {
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        b.eraseColor(color);
        return build(new Bitmap[]{b}, new int[]{1000}, w, h);
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }

    // ---------- use ----------

    public Sprite copy() { return new Sprite(frames, masks, delays); }

    public void update(long dtMs) {
        if (frames.length < 2) return;
        clock += dtMs;
        while (clock >= delays[frame]) {
            clock -= delays[frame];
            frame = (frame + 1) % frames.length;
        }
    }

    public void draw(Canvas c, Paint p) {
        c.drawBitmap(frames[frame], Math.round(x), Math.round(y), p);
    }

    /** Pixel-perfect: true only if an opaque pixel of this overlaps an opaque pixel of o. */
    public boolean collides(Sprite o) {
        int ax = Math.round(x), ay = Math.round(y), bx = Math.round(o.x), by = Math.round(o.y);
        int left = Math.max(ax, bx), right = Math.min(ax + w, bx + o.w);
        int top = Math.max(ay, by), bottom = Math.min(ay + h, by + o.h);
        if (right <= left || bottom <= top) return false;      // bounding boxes don't touch
        boolean[] a = masks[frame], b = o.masks[o.frame];
        for (int yy = top; yy < bottom; yy++) {
            int ai = (yy - ay) * w + (left - ax);
            int bi = (yy - by) * o.w + (left - bx);
            for (int xx = left; xx < right; xx++, ai++, bi++) {
                if (a[ai] && b[bi]) return true;
            }
        }
        return false;
    }
}
