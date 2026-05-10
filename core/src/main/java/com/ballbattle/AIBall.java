package com.ballbattle;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * AI球 - 继承Player，实现简单AI行为
 */
public class AIBall extends Player {
    private float aiThinkTimer;
    private static final float AI_THINK_INTERVAL = 0.5f;
    private float targetX;
    private float targetY;
    private int aiPersonality; // 0=激进, 1=保守, 2=平衡

    public AIBall() {
        super();
        aiThinkTimer = 0;
        aiPersonality = MathUtils.random(0, 2);
    }

    public void initAI(float x, float y, float hue, String name) {
        init(x, y, hue, name);
        aiThinkTimer = 0;
        aiPersonality = MathUtils.random(0, 2);
        targetX = x;
        targetY = y;
    }

    @Override
    public void update(float delta) {
        if (!alive) return;

        aiThinkTimer -= delta;
        if (aiThinkTimer <= 0) {
            aiThinkTimer = AI_THINK_INTERVAL + MathUtils.random(-0.1f, 0.1f);
            think();
        }

        super.update(delta);
    }

    private void think() {
        // 默认：找最近的食物
        Food nearestFood = findNearestFood();
        Player nearestThreat = findNearestThreat();
        Player nearestPrey = findNearestPrey();

        float priority = 0; // 0=食物, 1=逃跑, 2=追猎

        if (nearestThreat != null) {
            float threatDist = Vector2.dst(x, y, nearestThreat.x, nearestThreat.y);
            if (threatDist < 200f) {
                priority = 1; // 逃跑优先
            }
        }

        if (priority != 1 && nearestPrey != null && aiPersonality != 1) {
            float preyDist = Vector2.dst(x, y, nearestPrey.x, nearestPrey.y);
            if (preyDist < 300f && aiPersonality == 0) {
                priority = 2; // 激进AI追猎
            }
        }

        if (priority == 1) {
            // 逃跑
            if (nearestThreat != null) {
                float dx = x - nearestThreat.x;
                float dy = y - nearestThreat.y;
                float len = (float) Math.sqrt(dx * dx + dy * dy);
                if (len > 0.01f) {
                    targetX = x + (dx / len) * 300f;
                    targetY = y + (dy / len) * 300f;
                }
            }
        } else if (priority == 2) {
            // 追猎
            if (nearestPrey != null) {
                targetX = nearestPrey.x;
                targetY = nearestPrey.y;
            }
        } else {
            // 找食物
            if (nearestFood != null) {
                targetX = nearestFood.x;
                targetY = nearestFood.y;
            } else {
                // 随机漫游
                targetX = MathUtils.random(100f, WORLD_WIDTH - 100f);
                targetY = MathUtils.random(100f, WORLD_HEIGHT - 100f);
            }
        }

        // 限制目标在世界内
        targetX = Math.max(50f, Math.min(WORLD_WIDTH - 50f, targetX));
        targetY = Math.max(50f, Math.min(WORLD_HEIGHT - 50f, targetY));

        // 设置移动方向
        float dx = targetX - x;
        float dy = targetY - y;
        setMoveDirection(dx, dy);
    }

    private Food findNearestFood() {
        if (GameWorld.instance == null) return null;
        Food[] foods = GameWorld.instance.foods;
        if (foods == null) return null;

        Food nearest = null;
        float nearestDist = Float.MAX_VALUE;
        for (int i = 0; i < foods.length; i++) {
            Food f = foods[i];
            if (f == null || !f.alive) continue;
            float dist = Vector2.dst(x, y, f.x, f.y);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = f;
            }
        }
        return nearest;
    }

    private Player findNearestThreat() {
        if (GameWorld.instance == null) return null;
        Player player = GameWorld.instance.player;
        AIBall[] ais = GameWorld.instance.aiBalls;
        if (ais == null) return null;

        Player nearest = null;
        float nearestDist = Float.MAX_VALUE;

        // 检查玩家
        if (player != null && player.alive && player.radius > this.radius * 1.15f) {
            float dist = Vector2.dst(x, y, player.x, player.y);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = player;
            }
        }

        // 检查其他AI
        for (int i = 0; i < ais.length; i++) {
            AIBall other = ais[i];
            if (other == null || other == this || !other.alive) continue;
            if (other.radius > this.radius * 1.15f) {
                float dist = Vector2.dst(x, y, other.x, other.y);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = other;
                }
            }
        }
        return nearest;
    }

    private Player findNearestPrey() {
        if (GameWorld.instance == null) return null;
        Player player = GameWorld.instance.player;
        AIBall[] ais = GameWorld.instance.aiBalls;
        if (ais == null) return null;

        Player nearest = null;
        float nearestDist = Float.MAX_VALUE;

        // 检查玩家
        if (player != null && player.alive && this.radius > player.radius * 1.15f) {
            float dist = Vector2.dst(x, y, player.x, player.y);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = player;
            }
        }

        // 检查其他AI
        for (int i = 0; i < ais.length; i++) {
            AIBall other = ais[i];
            if (other == null || other == this || !other.alive) continue;
            if (this.radius > other.radius * 1.15f) {
                float dist = Vector2.dst(x, y, other.x, other.y);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = other;
                }
            }
        }
        return nearest;
    }
}
