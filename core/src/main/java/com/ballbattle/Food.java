package com.ballbattle;

import com.badlogic.gdx.graphics.Color;

/**
 * 食物类 - 游戏中被球吞噬的小圆点
 */
public class Food {

    public float x;
    public float y;
    public float radius;
    public Color color;
    public boolean alive;
    public float mass;

    private static final float MIN_RADIUS = 5f;
    private static final float MAX_RADIUS = 10f;

    public Food(float x, float y) {
        this.x = x;
        this.y = y;
        this.radius = MIN_RADIUS + (float) Math.random() * (MAX_RADIUS - MIN_RADIUS);
        this.mass = radius * radius;
        this.color = randomColor();
        this.alive = true;
    }

    private Color randomColor() {
        float hue = (float) Math.random();
        Color tmpColor = new Color();
        tmpColor.fromHsv(hue, 0.7f, 0.9f);
        return tmpColor;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isAlive() {
        return alive;
    }
}
