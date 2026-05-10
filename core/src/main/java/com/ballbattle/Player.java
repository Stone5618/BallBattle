package com.ballbattle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * 玩家球类 - 包含位置、大小、移动、分裂、加速等核心逻辑
 */
public class Player {

    public float x;
    public float y;
    public float radius;
    public float mass;
    public Color color;
    public String name;
    public boolean alive;
    public int team; // -1表示无队伍，0=红队，1=蓝队
    public int skinIndex;

    // 移动相关
    private float vx;
    private float vy;
    private float baseSpeed = 300f;
    private float moveAngle = 0;
    private boolean moving = false;

    // 加速相关
    private boolean boosting = false;
    private static final float BOOST_MULTIPLIER = 2.0f;
    private static final float BOOST_MASS_COST = 0.01f; // 每秒减少1%质量

    // 分裂相关
    private static final float MIN_SPLIT_RADIUS = 40f;
    private static final float SPLIT_SPEED = 500f;

    // 统计
    public int score;
    public float maxRadius;
    public float survivalTime;

    // 初始半径
    private static final float INITIAL_RADIUS = 30f;

    // 世界尺寸（动态，支持生存模式缩小）
    protected float worldWidth;
    protected float worldHeight;

    public Player(float x, float y, String name, int skinIndex) {
        this.x = x;
        this.y = y;
        this.radius = INITIAL_RADIUS;
        this.mass = radius * radius;
        this.name = name;
        this.skinIndex = skinIndex;
        this.color = colorFromHex(BallBattleGame.SKIN_COLORS[skinIndex % BallBattleGame.SKIN_COLORS.length]);
        this.alive = true;
        this.team = -1;
        this.score = 0;
        this.maxRadius = INITIAL_RADIUS;
        this.survivalTime = 0;
        this.vx = 0;
        this.vy = 0;
        this.worldWidth = BallBattleGame.WORLD_WIDTH;
        this.worldHeight = BallBattleGame.WORLD_HEIGHT;
    }

    /**
     * 设置队伍
     * @param team 0=红队，1=蓝队
     */
    public void setTeam(int team) {
        this.team = team;
        if (team == 0) {
            this.color = new Color(0.9f, 0.3f, 0.3f, 1f);
        } else if (team == 1) {
            this.color = new Color(0.3f, 0.3f, 0.9f, 1f);
        }
    }

    /**
     * 更新玩家状态
     * @param delta 时间增量（秒）
     */
    public void update(float delta) {
        if (!alive) return;

        survivalTime += delta;

        // 计算速度（越大越慢）
        float speed = baseSpeed * (1.0f / (float) Math.sqrt(radius / INITIAL_RADIUS));
        if (boosting && mass > INITIAL_RADIUS * INITIAL_RADIUS * 0.5f) {
            speed *= BOOST_MULTIPLIER;
            // 加速消耗质量
            mass *= (1.0f - BOOST_MASS_COST * delta);
            updateRadiusFromMass();
        }

        // 移动
        if (moving) {
            vx = (float) Math.cos(moveAngle) * speed;
            vy = (float) Math.sin(moveAngle) * speed;
        } else {
            // 减速
            vx *= 0.9f;
            vy *= 0.9f;
        }

        x += vx * delta;
        y += vy * delta;

        // 世界边界限制
        clampToWorld();

        // 更新最大半径
        if (radius > maxRadius) {
            maxRadius = radius;
        }

        // 自然缩小（非常缓慢）
        if (radius > INITIAL_RADIUS) {
            mass *= (1.0f - 0.001f * delta);
            updateRadiusFromMass();
        }
    }

    /**
     * 设置移动方向
     * @param angle 弧度
     * @param isMoving 是否在移动
     */
    public void setMoveDirection(float angle, boolean isMoving) {
        this.moveAngle = angle;
        this.moving = isMoving;
    }

    /**
     * 设置加速状态
     * @param boosting 是否加速
     */
    public void setBoosting(boolean boosting) {
        this.boosting = boosting;
    }

    /**
     * 尝试分裂
     * @return 分裂出的新Player，如果不能分裂返回null
     */
    public Player split() {
        if (radius < MIN_SPLIT_RADIUS) return null;

        float halfMass = mass / 2f;
        mass = halfMass;
        updateRadiusFromMass();

        Player splitBall = new Player(x, y, name, skinIndex);
        splitBall.mass = halfMass;
        splitBall.updateRadiusFromMass();
        splitBall.team = this.team;
        if (team == 0) {
            splitBall.color = new Color(0.9f, 0.3f, 0.3f, 1f);
        } else if (team == 1) {
            splitBall.color = new Color(0.3f, 0.3f, 0.9f, 1f);
        }

        // 向移动方向弹射
        float angle = moving ? moveAngle : (float) Math.random() * MathUtils.PI2;
        splitBall.x = x + (float) Math.cos(angle) * radius * 2;
        splitBall.y = y + (float) Math.sin(angle) * radius * 2;
        splitBall.vx = (float) Math.cos(angle) * SPLIT_SPEED;
        splitBall.vy = (float) Math.sin(angle) * SPLIT_SPEED;

        return splitBall;
    }

    /**
     * 吃食物
     * @param food 被吃的食物
     */
    public void eatFood(Food food) {
        mass += food.mass;
        score += 10;
        updateRadiusFromMass();
        food.setAlive(false);
    }

    /**
     * 吞噬另一个球
     * @param other 被吞噬的球
     */
    public void eatBall(Player other) {
        mass += other.mass * 0.8f;
        score += (int) (other.mass / 10f);
        updateRadiusFromMass();
        other.alive = false;
    }

    /**
     * 检查是否能吞噬另一个球
     * @param other 另一个球
     * @return 是否能吞噬
     */
    public boolean canEat(Player other) {
        if (!other.alive) return false;
        if (this.team >= 0 && this.team == other.team) return false; // 同队不能互吃
        if (this.radius <= other.radius * 1.1f) return false; // 必须比对方大10%以上

        float dx = this.x - other.x;
        float dy = this.y - other.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        return dist < this.radius * 0.8f;
    }

    /**
     * 检查是否能吃到食物
     * @param food 食物
     * @return 是否能吃
     */
    public boolean canEatFood(Food food) {
        if (!food.isAlive()) return false;
        float dx = this.x - food.x;
        float dy = this.y - food.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        return dist < this.radius;
    }

    private void updateRadiusFromMass() {
        radius = (float) Math.sqrt(mass);
        if (radius < 10f) radius = 10f;
    }

    private void clampToWorld() {
        if (x - radius < 0) { x = radius; vx = 0; }
        if (x + radius > worldWidth) { x = worldWidth - radius; vx = 0; }
        if (y - radius < 0) { y = radius; vy = 0; }
        if (y + radius > worldHeight) { y = worldHeight - radius; vy = 0; }
    }

    /**
     * 从十六进制颜色字符串创建Color
     */
    public static Color colorFromHex(String hex) {
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new Color(r / 255f, g / 255f, b / 255f, 1f);
    }

    public float getVx() { return vx; }
    public float getVy() { return vy; }
    public void setVx(float vx) { this.vx = vx; }
    public void setVy(float vy) { this.vy = vy; }
    public boolean isBoosting() { return boosting; }

    /**
     * 设置世界尺寸（用于生存模式地图缩小）
     */
    public void setWorldSize(float worldWidth, float worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }
}
