package com.ballbattle;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * 玩家球 - 包含位置、半径、颜色、速度、分裂等逻辑
 */
public class Player {
    public static final float INITIAL_RADIUS = 25f;
    public static final float MIN_RADIUS = 5f;
    public static final float MAX_SPEED = 300f;
    public static final float MIN_SPEED = 60f;
    public static final float WORLD_WIDTH = 2000f;
    public static final float WORLD_HEIGHT = 2000f;

    public float x;
    public float y;
    public float radius;
    public float r, g, b;
    public float score;
    public boolean alive;
    public String name;
    public float nameHue; // 用于显示名字颜色

    // 分裂相关
    public boolean isSplit;
    public float splitTimer;
    public static final float SPLIT_COOLDOWN = 3f;
    public float splitX;
    public float splitY;
    public float splitRadius;
    public boolean hasSplitBall;
    public float splitMergeTimer;

    // 移动方向
    protected float moveDirX;
    protected float moveDirY;

    public Player() {
        this.radius = INITIAL_RADIUS;
        this.alive = true;
        this.score = 0;
        this.isSplit = false;
        this.splitTimer = 0;
        this.hasSplitBall = false;
        this.splitMergeTimer = 0;
        this.moveDirX = 0;
        this.moveDirY = 0;
    }

    public void init(float x, float y, float hue, String name) {
        this.x = x;
        this.y = y;
        this.radius = INITIAL_RADIUS;
        this.alive = true;
        this.score = 0;
        this.isSplit = false;
        this.hasSplitBall = false;
        this.splitTimer = 0;
        this.splitMergeTimer = 0;
        this.nameHue = hue;
        this.name = name;
        this.moveDirX = 0;
        this.moveDirY = 0;
        setColorFromHue(hue);
    }

    protected void setColorFromHue(float hue) {
        float h = hue % 360f;
        float s = 0.8f;
        float v = 0.9f;
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

    public void setMoveDirection(float dirX, float dirY) {
        float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (len > 0.01f) {
            this.moveDirX = dirX / len;
            this.moveDirY = dirY / len;
        } else {
            this.moveDirX = 0;
            this.moveDirY = 0;
        }
    }

    public float getSpeed() {
        float ratio = Math.max(0.01f, radius / INITIAL_RADIUS);
        float speed = MAX_SPEED / ratio;
        return Math.max(MIN_SPEED, Math.min(MAX_SPEED, speed));
    }

    public void update(float delta) {
        if (!alive) return;

        // 更新分裂冷却
        if (splitTimer > 0) {
            splitTimer -= delta;
            if (splitTimer < 0) splitTimer = 0;
        }

        // 移动主球
        float speed = getSpeed();
        x += moveDirX * speed * delta;
        y += moveDirY * speed * delta;

        // 边界限制
        x = Math.max(radius, Math.min(WORLD_WIDTH - radius, x));
        y = Math.max(radius, Math.min(WORLD_HEIGHT - radius, y));

        // 分裂球移动
        if (hasSplitBall) {
            splitMergeTimer -= delta;
            // 分裂球也向同方向移动，但稍慢
            splitX += moveDirX * speed * 0.8f * delta;
            splitY += moveDirY * speed * 0.8f * delta;
            splitX = Math.max(splitRadius, Math.min(WORLD_WIDTH - splitRadius, splitX));
            splitY = Math.max(splitRadius, Math.min(WORLD_HEIGHT - splitRadius, splitY));

            // 合并时间到了就合并
            if (splitMergeTimer <= 0) {
                mergeSplit();
            }
        }
    }

    public void trySplit() {
        if (splitTimer > 0 || radius < INITIAL_RADIUS * 1.5f || hasSplitBall) return;

        isSplit = true;
        hasSplitBall = true;
        splitTimer = SPLIT_COOLDOWN;
        splitMergeTimer = 5f;

        // 分裂球从主球位置射出
        float splitRatio = 0.7f;
        splitRadius = radius * splitRatio;
        radius = radius * splitRatio;

        // 向移动方向射出
        float launchDist = radius + splitRadius + 20f;
        if (Math.abs(moveDirX) < 0.01f && Math.abs(moveDirY) < 0.01f) {
            splitX = x + launchDist;
            splitY = y;
        } else {
            splitX = x + moveDirX * launchDist;
            splitY = y + moveDirY * launchDist;
        }
    }

    private void mergeSplit() {
        if (!hasSplitBall) return;
        // 合并面积
        float area1 = radius * radius;
        float area2 = splitRadius * splitRadius;
        float totalArea = area1 + area2;
        radius = Math.max(MIN_RADIUS, (float) Math.sqrt(totalArea));
        hasSplitBall = false;
        isSplit = false;
    }

    public void addScore(float amount) {
        this.score += amount;
        // 面积增长
        float area = radius * radius + amount * 0.5f;
        radius = Math.max(MIN_RADIUS, (float) Math.sqrt(area));
    }

    public boolean canEat(Player other) {
        if (other == null || !other.alive || !this.alive) return false;
        float dist = Vector2.dst(this.x, this.y, other.x, other.y);
        return this.radius > other.radius * 1.15f && dist < this.radius - other.radius * 0.4f;
    }

    public boolean canEatSplit(Player other) {
        if (other == null || !other.alive || !this.alive) return false;
        float dist = Vector2.dst(this.x, this.y, other.splitX, other.splitY);
        return this.radius > other.splitRadius * 1.15f && dist < this.radius - other.splitRadius * 0.4f;
    }

    public boolean canEatFood(Food food) {
        if (food == null || !food.alive || !this.alive) return false;
        float dist = Vector2.dst(this.x, this.y, food.x, food.y);
        return dist < this.radius;
    }

    public boolean splitCanEatFood(Food food) {
        if (food == null || !food.alive || !this.alive || !this.hasSplitBall) return false;
        float dist = Vector2.dst(this.splitX, this.splitY, food.x, food.y);
        return dist < this.splitRadius;
    }

    public float getTotalRadius() {
        if (hasSplitBall) {
            float area = radius * radius + splitRadius * splitRadius;
            return (float) Math.sqrt(area);
        }
        return radius;
    }

    public float getCenterX() {
        if (hasSplitBall) {
            return (x + splitX) * 0.5f;
        }
        return x;
    }

    public float getCenterY() {
        if (hasSplitBall) {
            return (y + splitY) * 0.5f;
        }
        return y;
    }
}
