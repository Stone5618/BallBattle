package com.ballbattle;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * 游戏世界管理类 - 管理所有实体、碰撞检测、食物生成、AI管理
 */
public class GameWorld {

    private int gameMode;
    private Player player;
    private Array<AIBall> aiBalls;
    private Array<Food> foods;
    private Array<ParticleEffect> particles;
    private Array<Player> splitBalls; // 玩家分裂出的额外球

    // 配置
    private static final int TARGET_FOOD_COUNT = 400;
    private static final int MIN_AI_COUNT = 15;
    private static final int MAX_AI_COUNT = 25;
    private static final float FOOD_SPAWN_RATE = 5f; // 每秒生成食物数
    private static final float AI_RESPAWN_INTERVAL = 3f; // AI重生间隔
    private static final float WORLD_SHRINK_RATE = 5f; // 生存模式每秒缩小像素

    // 生存模式
    private float currentWorldWidth;
    private float currentWorldHeight;
    private float worldShrinkTimer;

    // AI重生计时器
    private float aiRespawnTimer;

    // 食物生成计时器
    private float foodSpawnTimer;

    // 游戏是否结束
    private boolean gameOver;

    public GameWorld(int gameMode) {
        this.gameMode = gameMode;
        this.aiBalls = new Array<>();
        this.foods = new Array<>();
        this.particles = new Array<>();
        this.splitBalls = new Array<>();
        this.gameOver = false;
        this.aiRespawnTimer = 0;
        this.foodSpawnTimer = 0;

        if (gameMode == BallBattleGame.MODE_SURVIVAL) {
            this.currentWorldWidth = BallBattleGame.WORLD_WIDTH;
            this.currentWorldHeight = BallBattleGame.WORLD_HEIGHT;
            this.worldShrinkTimer = 0;
        } else {
            this.currentWorldWidth = BallBattleGame.WORLD_WIDTH;
            this.currentWorldHeight = BallBattleGame.WORLD_HEIGHT;
        }

        // 创建玩家
        createPlayer();

        // 初始化食物
        for (int i = 0; i < TARGET_FOOD_COUNT; i++) {
            spawnFood();
        }

        // 初始化AI
        for (int i = 0; i < MIN_AI_COUNT; i++) {
            spawnAI();
        }
    }

    /**
     * 创建玩家
     */
    private void createPlayer() {
        float px = BallBattleGame.WORLD_WIDTH / 2f + MathUtils.random(-500, 500);
        float py = BallBattleGame.WORLD_HEIGHT / 2f + MathUtils.random(-500, 500);
        int skinIdx = MathUtils.random(BallBattleGame.SKIN_COLORS.length - 1);
        player = new Player(px, py, "我", skinIdx);

        if (gameMode == BallBattleGame.MODE_TEAMS) {
            player.setTeam(MathUtils.random(1)); // 随机分配红蓝队
        }
    }

    /**
     * 生成一个食物
     */
    private void spawnFood() {
        float margin = 50;
        float fx = MathUtils.random(margin, currentWorldWidth - margin);
        float fy = MathUtils.random(margin, currentWorldHeight - margin);
        foods.add(new Food(fx, fy));
    }

    /**
     * 生成一个AI
     */
    private void spawnAI() {
        float margin = 100;
        float ax = MathUtils.random(margin, currentWorldWidth - margin);
        float ay = MathUtils.random(margin, currentWorldHeight - margin);
        int nameIdx = MathUtils.random(BallBattleGame.AI_NAMES.length - 1);
        int skinIdx = MathUtils.random(BallBattleGame.SKIN_COLORS.length - 1);
        int difficulty = MathUtils.random(1, 5);

        AIBall ai = new AIBall(ax, ay, BallBattleGame.AI_NAMES[nameIdx], skinIdx, difficulty);

        if (gameMode == BallBattleGame.MODE_TEAMS) {
            ai.setTeam(MathUtils.random(1));
        }

        // 给AI一些初始大小变化
        float sizeMultiplier = 0.5f + MathUtils.random(0, 2.0f);
        ai.mass = ai.mass * sizeMultiplier;
        ai.radius = (float) Math.sqrt(ai.mass);

        aiBalls.add(ai);
    }

    /**
     * 更新游戏世界
     * @param delta 时间增量（秒）
     */
    public void update(float delta) {
        if (gameOver) return;

        // 更新玩家
        player.update(delta);

        // 更新分裂球
        for (int i = splitBalls.size - 1; i >= 0; i--) {
            Player sb = splitBalls.get(i);
            sb.update(delta);
            // 分裂球减速后合并回玩家
            if (sb.getVx() * sb.getVx() + sb.getVy() * sb.getVy() < 100) {
                player.mass += sb.mass;
                player.radius = (float) Math.sqrt(player.mass);
                sb.alive = false;
                splitBalls.removeIndex(i);
            }
        }

        // 收集所有玩家用于AI决策
        Array<Player> allPlayers = new Array<>();
        allPlayers.add(player);
        allPlayers.addAll(splitBalls);
        allPlayers.addAll(aiBalls);

        // 更新AI
        for (int i = 0; i < aiBalls.size; i++) {
            AIBall ai = aiBalls.get(i);
            ai.updateAI(delta, allPlayers, foods);

            // 检查AI分裂
            if (ai.shouldSplit()) {
                Player split = ai.split();
                if (split != null) {
                    // 分裂出的球作为临时AI
                    AIBall splitAI = new AIBall(split.x, split.y, ai.name, ai.skinIndex, ai.getDifficulty());
                    splitAI.mass = split.mass;
                    splitAI.radius = split.radius;
                    splitAI.team = ai.team;
                    splitAI.setVx(split.getVx());
                    splitAI.setVy(split.getVy());
                    splitAI.setMoveDirection((float) Math.atan2(split.getVy(), split.getVx()), true);
                    aiBalls.add(splitAI);
                }
            }
        }

        // 碰撞检测 - 玩家吃食物
        checkPlayerEatFood();

        // 碰撞检测 - AI吃食物
        checkAIEatFood();

        // 碰撞检测 - 球与球
        checkBallCollisions();

        // 移除死亡AI
        removeDeadAI();

        // 食物生成
        foodSpawnTimer += delta;
        float foodPerTick = FOOD_SPAWN_RATE * delta;
        while (foodSpawnTimer >= 1.0f / FOOD_SPAWN_RATE && foods.size < TARGET_FOOD_COUNT) {
            spawnFood();
            foodSpawnTimer -= 1.0f / FOOD_SPAWN_RATE;
        }
        if (foods.size < TARGET_FOOD_COUNT) {
            int toSpawn = Math.min(3, TARGET_FOOD_COUNT - foods.size);
            for (int i = 0; i < toSpawn; i++) {
                spawnFood();
            }
        }

        // AI重生
        aiRespawnTimer += delta;
        if (aiRespawnTimer >= AI_RESPAWN_INTERVAL && aiBalls.size < MIN_AI_COUNT) {
            spawnAI();
            aiRespawnTimer = 0;
        }

        // 生存模式 - 地图缩小
        if (gameMode == BallBattleGame.MODE_SURVIVAL) {
            updateWorldShrink(delta);
        }

        // 更新粒子
        updateParticles(delta);

        // 检查游戏结束
        checkGameOver();
    }

    /**
     * 检查玩家吃食物
     */
    private void checkPlayerEatFood() {
        for (int i = 0; i < foods.size; i++) {
            Food f = foods.get(i);
            if (!f.isAlive()) continue;

            if (player.canEatFood(f)) {
                // 生成吸收粒子
                spawnAbsorbParticles(f.x, f.y, player.x, player.y, f.color, 5);
                player.eatFood(f);
            }

            // 分裂球也能吃食物
            for (int j = 0; j < splitBalls.size; j++) {
                Player sb = splitBalls.get(j);
                if (sb.alive && sb.canEatFood(f) && f.isAlive()) {
                    sb.eatFood(f);
                }
            }
        }
    }

    /**
     * 检查AI吃食物
     */
    private void checkAIEatFood() {
        for (int i = 0; i < aiBalls.size; i++) {
            AIBall ai = aiBalls.get(i);
            if (!ai.alive) continue;

            for (int j = 0; j < foods.size; j++) {
                Food f = foods.get(j);
                if (f.isAlive() && ai.canEatFood(f)) {
                    ai.eatFood(f);
                }
            }
        }
    }

    /**
     * 检查球与球之间的碰撞
     */
    private void checkBallCollisions() {
        // 玩家 vs AI
        for (int i = 0; i < aiBalls.size; i++) {
            AIBall ai = aiBalls.get(i);
            if (!ai.alive) continue;

            // 玩家吃AI
            if (player.canEat(ai)) {
                spawnExplosionParticles(ai.x, ai.y, ai.color, 15);
                player.eatBall(ai);
                continue;
            }

            // AI吃玩家
            if (ai.canEat(player)) {
                spawnExplosionParticles(player.x, player.y, player.color, 20);
                ai.eatBall(player);
                gameOver = true;
                return;
            }

            // 分裂球 vs AI
            for (int j = 0; j < splitBalls.size; j++) {
                Player sb = splitBalls.get(j);
                if (!sb.alive || !ai.alive) continue;

                if (sb.canEat(ai)) {
                    spawnExplosionParticles(ai.x, ai.y, ai.color, 10);
                    sb.eatBall(ai);
                } else if (ai.canEat(sb)) {
                    spawnExplosionParticles(sb.x, sb.y, sb.color, 10);
                    ai.eatBall(sb);
                    splitBalls.removeIndex(j);
                    j--;
                }
            }
        }

        // AI vs AI
        for (int i = 0; i < aiBalls.size; i++) {
            AIBall ai1 = aiBalls.get(i);
            if (!ai1.alive) continue;

            for (int j = i + 1; j < aiBalls.size; j++) {
                AIBall ai2 = aiBalls.get(j);
                if (!ai2.alive) continue;

                if (ai1.canEat(ai2)) {
                    spawnExplosionParticles(ai2.x, ai2.y, ai2.color, 10);
                    ai1.eatBall(ai2);
                } else if (ai2.canEat(ai1)) {
                    spawnExplosionParticles(ai1.x, ai1.y, ai1.color, 10);
                    ai2.eatBall(ai1);
                    break;
                }
            }
        }
    }

    /**
     * 移除死亡AI并清理食物
     */
    private void removeDeadAI() {
        for (int i = aiBalls.size - 1; i >= 0; i--) {
            if (!aiBalls.get(i).alive) {
                aiBalls.removeIndex(i);
            }
        }

        // 清理死亡食物
        for (int i = foods.size - 1; i >= 0; i--) {
            if (!foods.get(i).isAlive()) {
                foods.removeIndex(i);
            }
        }
    }

    /**
     * 生存模式 - 地图缩小
     */
    private void updateWorldShrink(float delta) {
        worldShrinkTimer += delta;

        // 每30秒开始缩小一次
        if (worldShrinkTimer >= 30f) {
            worldShrinkTimer = 0;
            currentWorldWidth -= WORLD_SHRINK_RATE * 30f;
            currentWorldHeight -= WORLD_SHRINK_RATE * 30f;

            // 最小地图大小
            float minSize = 1000f;
            if (currentWorldWidth < minSize) currentWorldWidth = minSize;
            if (currentWorldHeight < minSize) currentWorldHeight = minSize;

            // 将超出边界的实体推回
            pushEntitiesInsideBounds();
        }
    }

    /**
     * 将超出边界的实体推回
     */
    private void pushEntitiesInsideBounds() {
        float cx = currentWorldWidth / 2f;
        float cy = currentWorldHeight / 2f;

        // 更新玩家世界尺寸并推回
        if (player.alive) {
            player.setWorldSize(currentWorldWidth, currentWorldHeight);
            if (player.x - player.radius < 0) player.x = player.radius;
            if (player.x + player.radius > currentWorldWidth) player.x = currentWorldWidth - player.radius;
            if (player.y - player.radius < 0) player.y = player.radius;
            if (player.y + player.radius > currentWorldHeight) player.y = currentWorldHeight - player.radius;
        }

        // 更新分裂球世界尺寸
        for (Player sb : splitBalls) {
            sb.setWorldSize(currentWorldWidth, currentWorldHeight);
        }

        // 更新AI世界尺寸并推回
        for (AIBall ai : aiBalls) {
            ai.setWorldSize(currentWorldWidth, currentWorldHeight);
            if (ai.x + ai.radius > currentWorldWidth) ai.x = currentWorldWidth - ai.radius;
            if (ai.y + ai.radius > currentWorldHeight) ai.y = currentWorldHeight - ai.radius;
        }
    }

    /**
     * 更新粒子效果
     */
    private void updateParticles(float delta) {
        for (int i = particles.size - 1; i >= 0; i--) {
            particles.get(i).update(delta);
            if (!particles.get(i).isAlive()) {
                particles.removeIndex(i);
            }
        }
    }

    /**
     * 生成爆炸粒子
     */
    private void spawnExplosionParticles(float x, float y, com.badlogic.gdx.graphics.Color color, int count) {
        for (int i = 0; i < count; i++) {
            particles.add(new ParticleEffect(x, y, color, 200f, 0.5f + MathUtils.random(0.5f)));
        }
    }

    /**
     * 生成吸收粒子
     */
    private void spawnAbsorbParticles(float fromX, float fromY, float toX, float toY,
                                       com.badlogic.gdx.graphics.Color color, int count) {
        for (int i = 0; i < count; i++) {
            particles.add(new ParticleEffect(fromX, fromY, toX, toY, color, 0.3f + MathUtils.random(0.2f)));
        }
    }

    /**
     * 检查游戏结束
     */
    private void checkGameOver() {
        if (!player.alive) {
            gameOver = true;
        }
    }

    // ========== 玩家操作 ==========

    /**
     * 玩家分裂
     * @return 分裂出的球
     */
    public Player playerSplit() {
        if (!player.alive || gameOver) return null;
        return player.split();
    }

    /**
     * 设置玩家移动方向
     */
    public void setPlayerMoveDirection(float angle, boolean moving) {
        if (player.alive) {
            player.setMoveDirection(angle, moving);
        }
    }

    /**
     * 设置玩家加速
     */
    public void setPlayerBoosting(boolean boosting) {
        if (player.alive) {
            player.setBoosting(boosting);
        }
    }

    // ========== Getter方法 ==========

    public Player getPlayer() { return player; }
    public Array<AIBall> getAIBalls() { return aiBalls; }
    public Array<Food> getFoods() { return foods; }
    public Array<ParticleEffect> getParticles() { return particles; }
    public Array<Player> getSplitBalls() { return splitBalls; }
    public boolean isGameOver() { return gameOver; }
    public int getGameMode() { return gameMode; }
    public float getCurrentWorldWidth() { return currentWorldWidth; }
    public float getCurrentWorldHeight() { return currentWorldHeight; }

    /**
     * 获取排行榜（按质量排序）
     * @return 排序后的玩家列表
     */
    public Array<Player> getLeaderboard() {
        Array<Player> all = new Array<>();
        all.add(player);
        all.addAll(aiBalls);

        // 简单冒泡排序（列表不大）
        for (int i = 0; i < all.size - 1; i++) {
            for (int j = 0; j < all.size - 1 - i; j++) {
                if (all.get(j).mass < all.get(j + 1).mass) {
                    all.swap(j, j + 1);
                }
            }
        }
        return all;
    }

    /**
     * 获取团队分数
     * @param team 队伍ID
     * @return 该队伍总质量
     */
    public float getTeamScore(int team) {
        float totalMass = 0;
        if (player.team == team && player.alive) {
            totalMass += player.mass;
        }
        for (AIBall ai : aiBalls) {
            if (ai.team == team && ai.alive) {
                totalMass += ai.mass;
            }
        }
        return totalMass;
    }
}
