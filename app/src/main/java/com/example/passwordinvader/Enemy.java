package com.example.passwordinvader;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** One live enemy: a sprite plus current HP. All behavior comes from its EnemyType. */
public class Enemy {
    public final EnemyType type;
    public final Sprite sprite;
    public int hp;

    public Enemy(EnemyType type, Sprite sprite) {
        this.type = type;
        this.sprite = sprite;
        this.hp = type.hp;
    }

    /** @return true if this hit killed it. */
    public boolean hit() { return --hp <= 0; }

    /** Call every frame; chance to fire scales with fireRate and frame time. */
    public boolean wantsToFire(Random rnd, float dt) {
        return type.fireRate > 0 && rnd.nextFloat() < type.fireRate * dt;
    }

    /** Roll the drop once; on success returns dropAmount tokens spread out around the enemy. */
    public List<Token> rollDrops(Random rnd, PasswordState pw, int tokenSize) {
        List<Token> out = new ArrayList<>();
        if (rnd.nextFloat() >= type.dropChance) return out;
        int n = Math.max(1, type.dropAmount);
        float cx = sprite.x + sprite.w / 2f, cy = sprite.y + sprite.h / 2f;
        float spacing = tokenSize * 1.4f;
        for (int i = 0; i < n; i++) {
            TokenType t = pw != null ? pw.pickType(rnd) : TokenType.values()[rnd.nextInt(4)];
            float x = cx - tokenSize / 2f + (i - (n - 1) / 2f) * spacing;
            out.add(new Token(t, t.randomChar(rnd), tokenSize, x, cy - tokenSize / 2f));
        }
        return out;
    }
}
