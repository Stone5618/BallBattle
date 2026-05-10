package com.ballbattle;

import com.badlogic.gdx.math.MathUtils;

/**
 * 食物 - 小彩色圆点，被玩家/AI吃掉后增长
 */
public class Food {
    public float x;
    public float y;
    public float radius;
    public float colorHue;
    public float colorSaturation;
    public float colorValue;
    public float r, g, b;
    public boolean alive;

    public Food() {
        this.alive = false;
    }

    public void spawn(float worldWidth, float worldHeight) {
        this.x = MathUtils.random(10f, Math.max(11f, worldWidth - 10f));
        this.y = MathUtils.random(10f, Math.max(11f, worldHeight - 10f));
        this.radius = MathUtils.random(5f, 8f);
        this.colorHue = MathUtils.random(0f, 360f);
        this.colorSaturation = 0.7f;
        this.colorValue = 0.9f;
        hsvToRgb();
        this.alive = true;
    }

    private void hsvToRgb() {
        float h = this.colorHue;
        float s = this.colorSaturation;
        float v = this.colorValue;
        float c = v * s;
        float x = c * (1 - Math.abs(((h / 60f) % 2f) - 1f));
        float m = v - c;
        float rr, gg, bb;
        if (h < 60) {
            rr = c; gg = x; bb = 0;
        } else if (h < 120) {
            rr = x; gg = c; bb = 0;
        } else if (h < 180) {
            rr = 0; gg = c; bb = x;
        } else if (h < 240) {
            rr = 0; gg = x; bb = c;
        } else if (h < 300) {
            rr = x; gg = 0; bb = c;
        } else {
            rr = c; gg = 0; bb = x;
        }
        this.r = rr + m;
        this.g = gg + m;
        this.b = bb + m;
    }

    public void respawn(float worldWidth, float worldHeight) {
        spawn(worldWidth, worldHeight);
    }
}
