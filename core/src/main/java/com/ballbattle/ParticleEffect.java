package com.ballbattle;

import com.badlogic.gdx.graphics.Color;

/**
 * 粒子效果类 - 球被吃时的爆炸效果和吃食物时的吸收效果
 */
public class ParticleEffect {

    public float x;
    public float y;
    public float vx;
    public float vy;
    public float radius;
    public Color color;
    public float life;
    public float maxLife;
    public boolean alive;

    /**
     * 创建一个爆炸粒子
     * @param x 粒子起始X坐标
     * @param y 粒子起始Y坐标
     * @param color 粒子颜色
     * @param speed 粒子速度
     * @param life 粒子生命时间（秒）
     */
    public ParticleEffect(float x, float y, Color color, float speed, float life) {
        this.x = x;
        this.y = y;
        this.color = new Color(color);
        this.life = life;
        this.maxLife = life;
        this.alive = true;

        float angle = (float) Math.random() * (float) Math.PI * 2;
        float spd = speed * (0.5f + (float) Math.random() * 0.5f);
        this.vx = (float) Math.cos(angle) * spd;
        this.vy = (float) Math.sin(angle) * spd;
        this.radius = 3f + (float) Math.random() * 5f;
    }

    /**
     * 创建一个向目标移动的吸收粒子
     * @param x 粒子起始X坐标
     * @param y 粒子起始Y坐标
     * @param targetX 目标X坐标
     * @param targetY 目标Y坐标
     * @param color 粒子颜色
     * @param life 粒子生命时间（秒）
     */
    public ParticleEffect(float x, float y, float targetX, float targetY, Color color, float life) {
        this.x = x;
        this.y = y;
        this.color = new Color(color);
        this.life = life;
        this.maxLife = life;
        this.alive = true;
        this.radius = 2f + (float) Math.random() * 3f;

        float dx = targetX - x;
        float dy = targetY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            float spd = dist / life * 0.8f;
            this.vx = (dx / dist) * spd;
            this.vy = (dy / dist) * spd;
        } else {
            this.vx = 0;
            this.vy = 0;
        }
    }

    public void update(float delta) {
        if (!alive) return;

        life -= delta;
        if (life <= 0) {
            alive = false;
            return;
        }

        x += vx * delta;
        y += vy * delta;

        // 减速
        vx *= 0.98f;
        vy *= 0.98f;

        // 缩小
        float lifeRatio = life / maxLife;
        radius *= (0.95f + 0.05f * lifeRatio);
        if (radius < 0.5f) {
            radius = 0.5f;
        }
    }

    public float getAlpha() {
        return Math.max(0, life / maxLife);
    }

    public boolean isAlive() {
        return alive;
    }
}
