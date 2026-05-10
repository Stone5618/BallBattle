package com.ballbattle;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * AI对手类 - 具有智能行为的电脑控制球
 */
public class AIBall extends Player {

    // AI行为状态
    private enum AIState {
        WANDER,       // 随机漫游
        SEEK_FOOD,    // 寻找食物
        CHASE,        // 追击小球
        FLEE,         // 逃离大球
        SPLIT_ATTACK  // 分裂攻击
    }

    private AIState currentState;
    private float stateTimer;
    private float decisionInterval;
    private float targetX;
    private float targetY;
    private Player chaseTarget;
    private Player fleeTarget;
    private Food nearestFood;

    // AI难度等级 (1-5)
    private int difficulty;

    // 感知范围
    private float visionRange;
    private float fleeRange;

    public AIBall(float x, float y, String name, int skinIndex, int difficulty) {
        super(x, y, name, skinIndex);
        this.difficulty = MathUtils.clamp(difficulty, 1, 5);
        this.currentState = AIState.WANDER;
        this.stateTimer = 0;
        this.decisionInterval = getDecisionInterval();
        this.targetX = x;
        this.targetY = y;

        // 难度影响感知范围
        this.visionRange = 300f + difficulty * 100f;
        this.fleeRange = 200f + difficulty * 80f;

        // 随机初始状态
        pickRandomWanderTarget();
    }

    private float getDecisionInterval() {
        // 难度越高，决策越频繁
        return 1.0f - (difficulty - 1) * 0.15f;
    }

    /**
     * 更新AI决策和行为
     * @param delta 时间增量
     * @param allPlayers 所有玩家列表（包括其他AI）
     * @param foods 所有食物列表
     */
    public void updateAI(float delta, Array<Player> allPlayers, Array<Food> foods) {
        if (!alive) return;

        stateTimer += delta;

        // 定期重新决策
        if (stateTimer >= decisionInterval) {
            stateTimer = 0;
            makeDecision(allPlayers, foods);
        }

        // 执行当前状态的行为
        executeBehavior(delta);

        // 调用父类更新
        super.update(delta);
    }

    /**
     * AI决策逻辑
     */
    private void makeDecision(Array<Player> allPlayers, Array<Food> foods) {
        // 优先级1：逃离比自己大的球
        Player nearestThreat = findNearestThreat(allPlayers);
        if (nearestThreat != null) {
            currentState = AIState.FLEE;
            fleeTarget = nearestThreat;
            return;
        }

        // 优先级2：追击比自己小的球（高难度AI更积极）
        if (difficulty >= 3) {
            Player prey = findNearestPrey(allPlayers);
            if (prey != null && radius > prey.radius * 1.3f) {
                currentState = AIState.CHASE;
                chaseTarget = prey;
                return;
            }
        }

        // 优先级3：寻找食物
        Food food = findNearestFood(foods);
        if (food != null) {
            currentState = AIState.SEEK_FOOD;
            nearestFood = food;
            return;
        }

        // 优先级4：分裂攻击（高难度AI偶尔使用）
        if (difficulty >= 4 && radius > 60 && Math.random() < 0.1f) {
            Player prey = findNearestPrey(allPlayers);
            if (prey != null && radius > prey.radius * 1.5f) {
                currentState = AIState.SPLIT_ATTACK;
                chaseTarget = prey;
                return;
            }
        }

        // 默认：随机漫游
        currentState = AIState.WANDER;
        pickRandomWanderTarget();
    }

    /**
     * 执行当前状态的行为
     */
    private void executeBehavior(float delta) {
        float angle = 0;
        boolean isMoving = true;

        switch (currentState) {
            case WANDER:
                angle = angleTo(targetX, targetY);
                float distToTarget = distanceTo(targetX, targetY);
                if (distToTarget < 50) {
                    pickRandomWanderTarget();
                }
                break;

            case SEEK_FOOD:
                if (nearestFood != null && nearestFood.isAlive()) {
                    angle = angleTo(nearestFood.x, nearestFood.y);
                } else {
                    currentState = AIState.WANDER;
                    pickRandomWanderTarget();
                }
                break;

            case CHASE:
                if (chaseTarget != null && chaseTarget.alive && radius > chaseTarget.radius * 1.1f) {
                    angle = angleTo(chaseTarget.x, chaseTarget.y);
                    // 高难度AI在追击时加速
                    if (difficulty >= 4 && mass > radius * radius * 0.6f) {
                        setBoosting(true);
                    }
                } else {
                    setBoosting(false);
                    currentState = AIState.WANDER;
                    pickRandomWanderTarget();
                }
                break;

            case FLEE:
                if (fleeTarget != null && fleeTarget.alive) {
                    // 向相反方向逃跑
                    float fleeAngle = angleTo(fleeTarget.x, fleeTarget.y);
                    angle = fleeAngle + (float) Math.PI; // 反方向
                    // 高难度AI逃跑时也加速
                    if (difficulty >= 3 && mass > radius * radius * 0.5f) {
                        setBoosting(true);
                    }
                } else {
                    setBoosting(false);
                    currentState = AIState.WANDER;
                    pickRandomWanderTarget();
                }
                break;

            case SPLIT_ATTACK:
                if (chaseTarget != null && chaseTarget.alive) {
                    angle = angleTo(chaseTarget.x, chaseTarget.y);
                    // AI分裂攻击 - 返回分裂指令由GameWorld处理
                    // 这里只设置方向
                } else {
                    currentState = AIState.WANDER;
                    pickRandomWanderTarget();
                }
                break;

            default:
                isMoving = false;
                break;
        }

        if (currentState != AIState.FLEE && currentState != AIState.CHASE) {
            setBoosting(false);
        }

        setMoveDirection(angle, isMoving);
    }

    /**
     * 检查AI是否需要分裂（由GameWorld调用）
     * @return 是否执行分裂
     */
    public boolean shouldSplit() {
        return currentState == AIState.SPLIT_ATTACK && radius > 60;
    }

    public int getDifficulty() { return difficulty; }

    /**
     * 找到最近的威胁（比自己大的球）
     */
    private Player findNearestThreat(Array<Player> allPlayers) {
        Player nearest = null;
        float nearestDist = fleeRange;

        for (int i = 0; i < allPlayers.size; i++) {
            Player p = allPlayers.get(i);
            if (p == this || !p.alive) continue;
            if (team >= 0 && team == p.team) continue; // 同队不是威胁

            if (p.radius > radius * 1.1f) {
                float dist = distanceTo(p.x, p.y);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = p;
                }
            }
        }
        return nearest;
    }

    /**
     * 找到最近的猎物（比自己小的球）
     */
    private Player findNearestPrey(Array<Player> allPlayers) {
        Player nearest = null;
        float nearestDist = visionRange;

        for (int i = 0; i < allPlayers.size; i++) {
            Player p = allPlayers.get(i);
            if (p == this || !p.alive) continue;
            if (team >= 0 && team == p.team) continue;

            if (radius > p.radius * 1.2f) {
                float dist = distanceTo(p.x, p.y);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = p;
                }
            }
        }
        return nearest;
    }

    /**
     * 找到最近的食物
     */
    private Food findNearestFood(Array<Food> foods) {
        Food nearest = null;
        float nearestDist = visionRange * 1.5f;

        for (int i = 0; i < foods.size; i++) {
            Food f = foods.get(i);
            if (!f.isAlive()) continue;

            float dist = distanceTo(f.x, f.y);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = f;
            }
        }
        return nearest;
    }

    /**
     * 选择随机漫游目标
     */
    private void pickRandomWanderTarget() {
        float range = 500f;
        float minX = radius;
        float maxX = Math.max(radius, worldWidth - radius);
        float minY = radius;
        float maxY = Math.max(radius, worldHeight - radius);
        targetX = MathUtils.clamp(x + MathUtils.random(-range, range), minX, maxX);
        targetY = MathUtils.clamp(y + MathUtils.random(-range, range), minY, maxY);
    }

    private float angleTo(float tx, float ty) {
        return (float) Math.atan2(ty - y, tx - x);
    }

    private float distanceTo(float tx, float ty) {
        float dx = tx - x;
        float dy = ty - y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
