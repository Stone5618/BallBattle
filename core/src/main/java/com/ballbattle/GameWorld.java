package com.ballbattle;

import com.badlogic.gdx.math.MathUtils;

/**
 * 游戏世界 - 管理所有实体、碰撞检测、食物生成、AI管理
 */
public class GameWorld {
    public static GameWorld instance; // 供AI访问

    public static final float WORLD_WIDTH = 2000f;
    public static final float WORLD_HEIGHT = 2000f;
    public static final int FOOD_COUNT = 200;
    public static final int AI_COUNT = 10;

    public Player player;
    public AIBall[] aiBalls;
    public Food[] foods;
    public float gameTime;
    public boolean gameOver;
    public int gameMode; // 0=自由, 1=团队, 2=生存

    // AI名字颜色
    private static final float[] AI_HUES = {0f, 30f, 60f, 120f, 180f, 210f, 270f, 300f, 330f, 45f};
    private static final String[] AI_NAMES = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

    public GameWorld() {
        instance = this;
        this.player = new Player();
        this.aiBalls = new AIBall[AI_COUNT];
        this.foods = new Food[FOOD_COUNT];
        this.gameTime = 0;
        this.gameOver = false;
        this.gameMode = 0;

        // 初始化食物数组
        for (int i = 0; i < FOOD_COUNT; i++) {
            foods[i] = new Food();
        }

        // 初始化AI数组
        for (int i = 0; i < AI_COUNT; i++) {
            aiBalls[i] = new AIBall();
        }
    }

    public void init(int mode) {
        this.gameMode = mode;
        this.gameTime = 0;
        this.gameOver = false;
        instance = this;

        // 初始化玩家
        float spawnX = MathUtils.random(200f, WORLD_WIDTH - 200f);
        float spawnY = MathUtils.random(200f, WORLD_HEIGHT - 200f);
        player.init(spawnX, spawnY, 0f, "P");

        // 初始化AI
        for (int i = 0; i < AI_COUNT; i++) {
            float ax = MathUtils.random(100f, WORLD_WIDTH - 100f);
            float ay = MathUtils.random(100f, WORLD_HEIGHT - 100f);
            float hue = AI_HUES[i % AI_HUES.length];
            String name = AI_NAMES[i % AI_NAMES.length];
            aiBalls[i].initAI(ax, ay, hue, name);
        }

        // 初始化食物
        for (int i = 0; i < FOOD_COUNT; i++) {
            foods[i].spawn(WORLD_WIDTH, WORLD_HEIGHT);
        }
    }

    public void update(float delta) {
        if (gameOver) return;

        gameTime += delta;

        // 更新玩家
        player.update(delta);

        // 更新AI
        for (int i = 0; i < aiBalls.length; i++) {
            if (aiBalls[i] != null && aiBalls[i].alive) {
                aiBalls[i].update(delta);
            }
        }

        // 碰撞检测：玩家吃食物
        for (int i = 0; i < foods.length; i++) {
            if (foods[i] != null && foods[i].alive) {
                if (player.canEatFood(foods[i])) {
                    player.addScore(foods[i].radius * 0.5f);
                    foods[i].alive = false;
                } else if (player.splitCanEatFood(foods[i])) {
                    player.addScore(foods[i].radius * 0.5f);
                    foods[i].alive = false;
                }
            }
        }

        // 碰撞检测：AI吃食物
        for (int a = 0; a < aiBalls.length; a++) {
            AIBall ai = aiBalls[a];
            if (ai == null || !ai.alive) continue;
            for (int f = 0; f < foods.length; f++) {
                Food food = foods[f];
                if (food == null || !food.alive) continue;
                if (ai.canEatFood(food)) {
                    ai.addScore(food.radius * 0.5f);
                    food.alive = false;
                } else if (ai.splitCanEatFood(food)) {
                    ai.addScore(food.radius * 0.5f);
                    food.alive = false;
                }
            }
        }

        // 碰撞检测：玩家 vs AI
        for (int i = 0; i < aiBalls.length; i++) {
            AIBall ai = aiBalls[i];
            if (ai == null || !ai.alive) continue;

            // 玩家吃AI
            if (player.canEat(ai)) {
                player.addScore(ai.radius * 2f);
                ai.alive = false;
            } else if (ai.canEat(player)) {
                // AI吃玩家 -> 游戏结束
                ai.addScore(player.radius * 2f);
                player.alive = false;
                gameOver = true;
                return;
            }

            // 分裂球碰撞
            if (player.hasSplitBall && ai.canEat(player)) {
                // AI吃玩家的分裂球
                ai.addScore(player.splitRadius * 2f);
                player.hasSplitBall = false;
                player.isSplit = false;
            }
            if (player.canEat(ai) && ai.hasSplitBall) {
                // 玩家吃AI的分裂球
                player.addScore(ai.splitRadius * 2f);
                ai.hasSplitBall = false;
                ai.isSplit = false;
            }
        }

        // 碰撞检测：AI vs AI
        for (int i = 0; i < aiBalls.length; i++) {
            AIBall a = aiBalls[i];
            if (a == null || !a.alive) continue;
            for (int j = i + 1; j < aiBalls.length; j++) {
                AIBall b = aiBalls[j];
                if (b == null || !b.alive) continue;

                if (a.canEat(b)) {
                    a.addScore(b.radius * 2f);
                    b.alive = false;
                } else if (b.canEat(a)) {
                    b.addScore(a.radius * 2f);
                    a.alive = false;
                }
            }
        }

        // 补充食物
        for (int i = 0; i < foods.length; i++) {
            if (foods[i] != null && !foods[i].alive) {
                foods[i].respawn(WORLD_WIDTH, WORLD_HEIGHT);
            }
        }

        // 重生AI（保持至少8个存活）
        int aliveCount = 0;
        for (int i = 0; i < aiBalls.length; i++) {
            if (aiBalls[i] != null && aiBalls[i].alive) aliveCount++;
        }
        if (aliveCount < 8) {
            for (int i = 0; i < aiBalls.length; i++) {
                if (aiBalls[i] != null && !aiBalls[i].alive) {
                    float ax = MathUtils.random(100f, WORLD_WIDTH - 100f);
                    float ay = MathUtils.random(100f, WORLD_HEIGHT - 100f);
                    // 确保不在玩家附近重生
                    float distToPlayer = (float) Math.sqrt(
                            (ax - player.x) * (ax - player.x) + (ay - player.y) * (ay - player.y)
                    );
                    if (distToPlayer < 300f) {
                        ax = ((ax + 500f) % (WORLD_WIDTH - 200f)) + 100f;
                        ay = ((ay + 500f) % (WORLD_HEIGHT - 200f)) + 100f;
                    }
                    float hue = AI_HUES[i % AI_HUES.length];
                    String name = AI_NAMES[i % AI_NAMES.length];
                    aiBalls[i].initAI(ax, ay, hue, name);
                    aliveCount++;
                    if (aliveCount >= AI_COUNT) break;
                }
            }
        }

        // 生存模式：60秒后检查
        if (gameMode == 2 && gameTime > 60f) {
            // 生存模式无特殊逻辑，只是活得越久分越高
            player.score += delta * 2f;
        }
    }

    /**
     * 获取排行榜数据（按分数排序）
     * 返回一个包含所有存活实体的分数数组
     */
    public float[] getLeaderboardScores() {
        // 计算需要的数组大小
        int count = 1; // 玩家
        for (int i = 0; i < aiBalls.length; i++) {
            if (aiBalls[i] != null && aiBalls[i].alive) count++;
        }

        float[] scores = new float[count];
        int idx = 0;
        if (player.alive) {
            scores[idx++] = player.score;
        }
        for (int i = 0; i < aiBalls.length; i++) {
            if (aiBalls[i] != null && aiBalls[i].alive) {
                if (idx < scores.length) {
                    scores[idx++] = aiBalls[i].score;
                }
            }
        }

        // 简单排序（降序）
        for (int i = 0; i < scores.length - 1; i++) {
            for (int j = i + 1; j < scores.length; j++) {
                if (scores[j] > scores[i]) {
                    float temp = scores[i];
                    scores[i] = scores[j];
                    scores[j] = temp;
                }
            }
        }

        return scores;
    }

    /**
     * 获取玩家排名（1-based）
     */
    public int getPlayerRank() {
        int rank = 1;
        for (int i = 0; i < aiBalls.length; i++) {
            AIBall ai = aiBalls[i];
            if (ai != null && ai.alive && ai.score > player.score) {
                rank++;
            }
        }
        return rank;
    }

    public void dispose() {
        instance = null;
    }
}
