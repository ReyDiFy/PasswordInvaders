package com.example.passwordinvader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Space Invaders loop. Controls: setMoveDir(-1/0/1), fire(), and tapping falling tokens to collect them. */
public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    public interface Listener {   // both called from the game thread
        void onStats(int score, int lives, String password, int passwordLength);
        void onEnd(boolean won, int score, String password);
    }

    private final SurfaceHolder holder;
    private final Paint paint = new Paint();
    private final Random rnd = new Random();
    private Listener listener;

    private Thread thread;
    private volatile boolean running, initialized;
    private volatile int moveDir;
    private volatile boolean fireRequested;
    private volatile boolean paused;
    private volatile boolean autoFire = true;
    private float speedScale = 1f;
    private boolean crt, shake;
    private float shakeT;
    private final Paint scan = new Paint();
    private int dragId = -1;
    private float lastX, pendingDx;   // pendingDx is shared with the game thread (guarded by 'this')
    private final ConcurrentLinkedQueue<float[]> taps = new ConcurrentLinkedQueue<>();

    private int W, H, tokenSize, score, lives = 3, wave = 0, waveSize;
    private boolean ended;
    private float alienDir = 1, fireCooldown;

    private PasswordState password = new PasswordState(8, 2, 2, 2, 2);
    private Sprite player, pBulletProto, aBulletProto;
    private final Map<EnemyType, Sprite> protos = new HashMap<>();
    private final ArrayList<Enemy> enemies = new ArrayList<>();
    private final ArrayList<Sprite> pBullets = new ArrayList<>(), aBullets = new ArrayList<>();
    private final ArrayList<Token> tokens = new ArrayList<>();

    public GameView(Context c) {
        super(c);
        holder = getHolder();
        holder.addCallback(this);
        scan.setColor(0x26000000);
    }

    public void setListener(Listener l) { listener = l; }
    public void setPassword(PasswordState p) { password = p; }   // call before the surface is created
    public void setMoveDir(int d) { moveDir = d; }
    public void fire() { fireRequested = true; }
    public void setPaused(boolean p) { paused = p; }
    public void setAutoFire(boolean a) { autoFire = a; }
    public void setSpeedScale(float s) { speedScale = s; }
    public void setCrt(boolean on) { crt = on; }
    public void setShake(boolean on) { shake = on; }

    // ---------- input ----------

    @Override public boolean onTouchEvent(MotionEvent e) {
        int a = e.getActionMasked(), i = e.getActionIndex();
        switch (a) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                taps.add(new float[]{e.getX(i), e.getY(i)});      // may collect a token (game thread)
                if (dragId == -1) { dragId = e.getPointerId(i); lastX = e.getX(i); }
                break;
            case MotionEvent.ACTION_MOVE:
                int idx = e.findPointerIndex(dragId);             // relative drag: a plain tap never moves the ship
                if (idx >= 0) {
                    float x = e.getX(idx);
                    synchronized (this) { pendingDx += x - lastX; }
                    lastX = x;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (e.getPointerId(i) == dragId) dragId = -1;
                break;
            case MotionEvent.ACTION_CANCEL:
                dragId = -1;
                break;
        }
        return true;
    }

    // ---------- surface lifecycle ----------

    @Override public void surfaceCreated(SurfaceHolder h) {
        running = true;
        thread = new Thread(this, "game-loop");
        thread.start();
    }

    @Override public void surfaceChanged(SurfaceHolder h, int format, int width, int height) {
        if (initialized) return;
        W = width; H = height;
        tokenSize = (int) (W * 0.09f);
        player       = Sprite.loadAny(getContext(), "sprites/player",       (int) (W * 0.12f), -1, 0xFF00F0FF);
        pBulletProto = Sprite.loadAny(getContext(), "sprites/bullet",       Math.max(4, (int) (W * 0.015f)), -1, 0xFFFFFFFF);
        aBulletProto = Sprite.loadAny(getContext(), "sprites/enemy_bullet", Math.max(4, (int) (W * 0.015f)), -1, 0xFFA855F7);
        player.x = (W - player.w) / 2f;
        player.y = H - player.h - H * 0.04f;
        spawnWave();
        initialized = true;
        pushStats();
    }

    @Override public void surfaceDestroyed(SurfaceHolder h) {
        running = false;
        try { if (thread != null) thread.join(); } catch (InterruptedException ignored) {}
    }

    // ---------- spawning ----------

    private Sprite protoFor(EnemyType t) {
        Sprite p = protos.get(t);
        if (p == null) {
            p = Sprite.loadAny(getContext(), t.sprite, Math.max(4, (int) (W * t.size)), -1, t.fallbackColor);
            protos.put(t, p);
        }
        return p;
    }

    /** Spawns `count` enemies of one type centered in a row. Returns the y for the next row. */
    private float spawnRow(EnemyType t, int count, float y) {
        Sprite proto = protoFor(t);
        float gap = proto.w * 0.4f;
        float startX = (W - (count * proto.w + (count - 1) * gap)) / 2f;
        for (int i = 0; i < count; i++) {
            Sprite s = proto.copy();
            s.x = startX + i * (proto.w + gap);
            s.y = y;
            enemies.add(new Enemy(t, s));
        }
        return y + proto.h + gap;
    }

    /** Edit this to design waves. Every 3rd wave adds a boss row. */
    private void spawnWave() {
        wave++;
        alienDir = 1;
        float y = H * 0.10f;
        if (wave % 5 == 0) y = spawnRow(EnemyType.HOODEDFIGURE, 1, y);
        if (wave % 4 == 0) y = spawnRow(EnemyType.THEPHISHERMAN, 2, y);
        if (wave % 3 == 0) y = spawnRow(EnemyType.OUTERVIRUS, 2, y);
        y = spawnRow(EnemyType.MRGREYHET, 4, y);
        y = spawnRow(EnemyType.SCRIPTKIDDY, 6, y);
        spawnRow(EnemyType.SCRIPTKIDDY, 6, y);
        waveSize = enemies.size();
    }

    // ---------- loop ----------

    @Override public void run() {
        long last = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            float dt = Math.min((now - last) / 1e9f, 0.05f);
            last = now;
            if (initialized && !ended && !paused) update(dt);
            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null && initialized) drawGame(c);
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }
            try { Thread.sleep(12); } catch (InterruptedException ignored) {}
        }
    }

    private void update(float dt) {
        long dtMs = (long) (dt * 1000);
        if (shakeT > 0) shakeT -= dt;

        // --- player ---
        float drag;
        synchronized (this) { drag = pendingDx; pendingDx = 0; }
        player.x = Math.max(0, Math.min(W - player.w, player.x + drag + moveDir * W * 0.6f * dt));
        player.update(dtMs);
        fireCooldown -= dt;
        if (fireRequested || autoFire) {
            fireRequested = false;
            if (fireCooldown <= 0) {
                Sprite b = pBulletProto.copy();
                b.x = player.x + player.w / 2f - b.w / 2f;
                b.y = player.y - b.h;
                pBullets.add(b);
                fireCooldown = 0.3f;
            }
        }

        // --- taps collect tokens ---
        float[] tap;
        while ((tap = taps.poll()) != null) {
            for (int i = tokens.size() - 1; i >= 0; i--) {
                Token t = tokens.get(i);
                if (t.hit(tap[0], tap[1]) && password.add(t.type, t.ch)) {
                    tokens.remove(i);
                    pushStats();
                    break;
                }
            }
        }
        if (password.isFull()) { finish(true); return; }

        // --- enemies: formation reverses and drops when any enemy would leave the screen ---
        float speedUp = speedScale * (1f + (waveSize - enemies.size()) * 0.12f);
        boolean edge = false;
        for (Enemy e : enemies) {
            float nx = e.sprite.x + alienDir * W * e.type.speed * speedUp * dt;
            if (nx < 0 || nx + e.sprite.w > W) edge = true;
        }
        if (edge) {
            alienDir = -alienDir;
            for (Enemy e : enemies) e.sprite.y += e.sprite.h * 0.5f;
        } else {
            for (Enemy e : enemies) e.sprite.x += alienDir * W * e.type.speed * speedUp * dt;
        }
        for (Enemy e : enemies) {
            e.sprite.update(dtMs);
            if (e.wantsToFire(rnd, dt)) {
                Sprite b = aBulletProto.copy();
                b.x = e.sprite.x + e.sprite.w / 2f - b.w / 2f;
                b.y = e.sprite.y + e.sprite.h;
                aBullets.add(b);
            }
        }

        // --- player bullets vs enemies (pixel-perfect); HP, score and drops come from the Enemy ---
        for (Iterator<Sprite> bi = pBullets.iterator(); bi.hasNext(); ) {
            Sprite b = bi.next();
            b.y -= H * 0.9f * dt;
            if (b.y + b.h < 0) { bi.remove(); continue; }
            for (Iterator<Enemy> ei = enemies.iterator(); ei.hasNext(); ) {
                Enemy e = ei.next();
                if (b.collides(e.sprite)) {
                    bi.remove();
                    if (e.hit()) {
                        ei.remove();
                        score += e.type.score;
                        tokens.addAll(e.rollDrops(rnd, password, tokenSize));
                        pushStats();
                    }
                    break;
                }
            }
        }

        // --- enemy bullets vs player ---
        for (Iterator<Sprite> bi = aBullets.iterator(); bi.hasNext(); ) {
            Sprite b = bi.next();
            b.y += H * 0.5f * dt;
            if (b.y > H) { bi.remove(); continue; }
            if (b.collides(player)) {
                bi.remove();
                lives--;
                if (shake) shakeT = 0.25f;
                pushStats();
                if (lives <= 0) { finish(false); return; }
            }
        }

        // --- falling tokens ---
        for (Iterator<Token> ti = tokens.iterator(); ti.hasNext(); ) {
            Token t = ti.next();
            t.y += H * 0.22f * dt;
            if (player.collidesRect(t.x, t.y, t.size, t.size) && password.add(t.type, t.ch)) {
                ti.remove();       // caught by the plane
                pushStats();
                continue;
            }
            if (t.y > H) ti.remove();   // missed
        }

        // --- wave / lose checks ---
        if (enemies.isEmpty()) spawnWave();   // password not full yet, so send another wave
        for (Enemy e : enemies) if (e.sprite.y + e.sprite.h >= player.y) { finish(false); return; }
    }

    private void drawGame(Canvas c) {
        c.drawColor(0xFF050F1A);
        c.save();
        if (shakeT > 0) c.translate((rnd.nextFloat() - 0.5f) * W * 0.03f, (rnd.nextFloat() - 0.5f) * W * 0.03f);
        for (Enemy e : enemies) e.sprite.draw(c, paint);
        for (Sprite b : pBullets) b.draw(c, paint);
        for (Sprite b : aBullets) b.draw(c, paint);
        for (Token t : tokens) t.draw(c);
        player.draw(c, paint);
        c.restore();
        if (crt) for (int y = 0; y < H; y += 4) c.drawRect(0, y, W, y + 2, scan);
    }

    private void pushStats() {
        if (listener != null) listener.onStats(score, lives, password.get(), password.length);
    }

    private void finish(boolean won) {
        ended = true;
        if (listener != null) listener.onEnd(won, score, password.get());
    }
}
