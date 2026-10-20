package com.example.passwordinvader;

/**
 * Stats for one kind of enemy. To add a new enemy or boss: add a preset below (and optionally
 * drop assets/sprites/<name>.png|gif). Then spawn it with GameView.spawnRow(EnemyType.X, count, y).
 * Fields are fluent so presets read like a spec sheet.
 */
public class EnemyType {
    public final String sprite;         // asset base name, no extension
    public int hp = 1;                  // shots needed to kill
    public float fireRate = 0.12f;      // shots per second (0 = never fires)
    public float dropChance = 0.25f;    // 0..1, rolled once when it dies
    public int dropAmount = 1;          // tokens dropped when the roll succeeds
    public float speed = 0.06f;         // horizontal speed in screen-widths per second
    public float size = 0.09f;          // sprite width as a fraction of screen width
    public int score = 10;
    public int fallbackColor = 0xFF39FF14;  // box color used if the sprite file is missing

    public EnemyType(String sprite) { this.sprite = sprite; }

    public EnemyType hp(int v) { hp = v; return this; }
    public EnemyType fireRate(float v) { fireRate = v; return this; }
    public EnemyType dropChance(float v) { dropChance = v; return this; }
    public EnemyType dropAmount(int v) { dropAmount = v; return this; }
    public EnemyType speed(float v) { speed = v; return this; }
    public EnemyType size(float v) { size = v; return this; }
    public EnemyType score(int v) { score = v; return this; }
    public EnemyType color(int v) { fallbackColor = v; return this; }

    // ---- presets ----
    public static final EnemyType SCRIPTKIDDY = new EnemyType("sprites/MYSCRIPTKIDDY/MYSCRIPTKIDDY")
            .hp(1).fireRate(0.12f).dropChance(0.15f).dropAmount(1).speed(0.06f).size(0.09f).score(10);

    public static final EnemyType MRGREYHET = new EnemyType("sprites/Mr.GrayHet/Mr.GrayHet")
            .hp(5).fireRate(0.20f).dropChance(0.25f).dropAmount(1).speed(0.05f).size(0.10f).score(25);

    public static final EnemyType OUTERVIRUS = new EnemyType("sprites/OuterVirus/OuterVirus")
            .hp(6).fireRate(1.2f).dropChance(0.50f).dropAmount(2).speed(0.03f).size(0.20f).score(500);

    public static final EnemyType THEPHISHERMAN = new EnemyType("sprites/ThePhisherMan/ThePhisherMan").hp(20).fireRate(0.67f).dropChance(0.75f).dropAmount(3).speed(0.01f).size(0.40f).score(750);
    public static final EnemyType HOODEDFIGURE = new EnemyType("sprites/HoodedFigure/HoodedFigure").hp(10).fireRate(2f).dropChance(1.0f).dropAmount(5).speed(0.03f).size(0.30f).score(1000);
}
