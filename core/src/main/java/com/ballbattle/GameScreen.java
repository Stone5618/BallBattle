package com.ballbattle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * 游戏主屏幕 - 实现Screen和InputProcessor接口
 */
public class GameScreen implements Screen, InputProcessor {

    private BallBattleGame game;
    private GameWorld world;
    private CameraController cameraController;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera hudCamera;

    private int screenWidth;
    private int screenHeight;
    private boolean initialized;

    // 触摸控制
    private int touchPointer;
    private float touchX;
    private float touchY;
    private boolean isTouching;

    // 双击检测
    private float lastTapTime;
    private static final float DOUBLE_TAP_INTERVAL = 0.3f;

    // 暂停
    private boolean paused;
    private float pauseButtonX;
    private float pauseButtonY;
    private float pauseButtonSize;

    // HUD颜色
    private static final Color BG_COLOR = new Color(0.05f, 0.05f, 0.1f, 1f);
    private static final Color GRID_COLOR = new Color(0.12f, 0.12f, 0.18f, 1f);
    private static final Color BORDER_COLOR = new Color(0.8f, 0.2f, 0.2f, 1f);
    private static final Color HUD_BG_COLOR = new Color(0f, 0f, 0f, 0.5f);
    private static final Color SCORE_COLOR = new Color(1f, 1f, 1f, 1f);
    private static final Color RANK_COLOR = new Color(1f, 1f, 0.5f, 1f);
    private static final Color PAUSE_COLOR = new Color(0.7f, 0.7f, 0.7f, 1f);

    public GameScreen(BallBattleGame game, int gameMode) {
        this.game = game;
        this.world = new GameWorld();
        this.cameraController = new CameraController();
        this.shapeRenderer = new ShapeRenderer();
        this.hudCamera = new OrthographicCamera();
        this.initialized = false;
        this.touchPointer = -1;
        this.isTouching = false;
        this.lastTapTime = 0;
        this.paused = false;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        world.init(0); // 默认自由模式
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        this.screenWidth = width;
        this.screenHeight = height;
        this.initialized = true;
        cameraController.resize(width, height);
        hudCamera.setToOrtho(false, width, height);
        hudCamera.update();

        // 计算暂停按钮位置（右上角）
        pauseButtonSize = Math.max(20f, Math.min(width, height) * 0.06f);
        pauseButtonX = width - pauseButtonSize - 10f;
        pauseButtonY = height - pauseButtonSize - 10f;
    }

    @Override
    public void render(float delta) {
        if (!initialized) return;

        // 限制delta防止大跳
        delta = Math.min(delta, 0.05f);

        if (!paused && !world.gameOver) {
            world.update(delta);
        }

        // 更新相机
        if (world.player.alive) {
            cameraController.update(world.player, delta);
        }

        // 清屏
        Gdx.gl.glClearColor(BG_COLOR.r, BG_COLOR.g, BG_COLOR.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 开始渲染
        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);

        // 画世界边界和网格
        drawWorld();

        // 画食物
        drawFoods();

        // 画AI
        drawAIBalls();

        // 画玩家
        drawPlayer();

        // 画HUD（使用正交相机，屏幕坐标）
        drawHUD();

        // 检查游戏结束
        if (world.gameOver) {
            saveScore();
            game.setScreen(new GameOverScreen(game, (int) world.player.score));
        }
    }

    private void drawWorld() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // 画世界边界
        shapeRenderer.setColor(BORDER_COLOR);
        shapeRenderer.rect(0, 0, GameWorld.WORLD_WIDTH, GameWorld.WORLD_HEIGHT);

        // 画网格
        shapeRenderer.setColor(GRID_COLOR);
        float gridSize = 100f;
        for (float x = gridSize; x < GameWorld.WORLD_WIDTH; x += gridSize) {
            shapeRenderer.line(x, 0, x, GameWorld.WORLD_HEIGHT);
        }
        for (float y = gridSize; y < GameWorld.WORLD_HEIGHT; y += gridSize) {
            shapeRenderer.line(0, y, GameWorld.WORLD_WIDTH, y);
        }

        shapeRenderer.end();
    }

    private void drawFoods() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (world.foods != null) {
            for (int i = 0; i < world.foods.length; i++) {
                Food food = world.foods[i];
                if (food == null || !food.alive) continue;
                shapeRenderer.setColor(food.r, food.g, food.b, 1f);
                shapeRenderer.circle(food.x, food.y, Math.max(1f, food.radius));
            }
        }
        shapeRenderer.end();
    }

    private void drawAIBalls() {
        if (world.aiBalls == null) return;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < world.aiBalls.length; i++) {
            AIBall ai = world.aiBalls[i];
            if (ai == null || !ai.alive) continue;

            // 画边框（先画大的深色圆，再画球体覆盖上去）
            shapeRenderer.setColor(ai.r * 0.5f, ai.g * 0.5f, ai.b * 0.5f, 1f);
            shapeRenderer.circle(ai.x, ai.y, Math.max(2f, ai.radius + 3f));

            // 画AI球体
            shapeRenderer.setColor(ai.r, ai.g, ai.b, 0.8f);
            shapeRenderer.circle(ai.x, ai.y, Math.max(1f, ai.radius));

            // 画分裂球
            if (ai.hasSplitBall) {
                shapeRenderer.setColor(ai.r, ai.g, ai.b, 0.7f);
                shapeRenderer.circle(ai.splitX, ai.splitY, Math.max(1f, ai.splitRadius));
            }

            // 画名字指示点（在球上方）
            float indicatorY = ai.y + ai.radius + 8f;
            shapeRenderer.setColor(ai.r, ai.g, ai.b, 1f);
            shapeRenderer.circle(ai.x, indicatorY, 4f);
        }
        shapeRenderer.end();
    }

    private void drawPlayer() {
        if (!world.player.alive) return;
        Player p = world.player;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 画边框（先画大的深色圆，再画球体覆盖上去）
        shapeRenderer.setColor(p.r * 0.5f, p.g * 0.5f, p.b * 0.5f, 1f);
        shapeRenderer.circle(p.x, p.y, Math.max(2f, p.radius + 3f));

        // 画玩家球体
        shapeRenderer.setColor(p.r, p.g, p.b, 0.9f);
        shapeRenderer.circle(p.x, p.y, Math.max(1f, p.radius));

        // 画分裂球
        if (p.hasSplitBall) {
            shapeRenderer.setColor(p.r * 0.5f, p.g * 0.5f, p.b * 0.5f, 1f);
            shapeRenderer.circle(p.splitX, p.splitY, Math.max(2f, p.splitRadius + 3f));
            shapeRenderer.setColor(p.r, p.g, p.b, 0.8f);
            shapeRenderer.circle(p.splitX, p.splitY, Math.max(1f, p.splitRadius));
        }

        // 画玩家名字指示（白色圆点在上方）
        float indicatorY = p.y + p.radius + 8f;
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.circle(p.x, indicatorY, 5f);

        // 画分数在球内（用小方块表示）
        int scoreInt = (int) p.score;
        if (scoreInt > 0 && p.radius > 15f) {
            float pixelSize = Math.max(1f, p.radius * 0.12f);
            PixelDrawer.drawNumber(shapeRenderer, scoreInt, p.x, p.y + pixelSize, pixelSize, Color.WHITE);
        }

        shapeRenderer.end();
    }

    private void drawHUD() {
        // 切换到屏幕坐标
        shapeRenderer.setProjectionMatrix(hudCamera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 左上角：分数背景
        float scoreBoxW = 120f;
        float scoreBoxH = 40f;
        shapeRenderer.setColor(HUD_BG_COLOR);
        shapeRenderer.rect(5f, screenHeight - scoreBoxH - 5f, scoreBoxW, scoreBoxH);

        // 分数标签（用黄色小方块表示）
        shapeRenderer.setColor(1f, 1f, 0f, 1f);
        shapeRenderer.rect(12f, screenHeight - 20f, 8f, 8f); // 标签指示

        // 分数数字
        int scoreInt = (int) world.player.score;
        PixelDrawer.drawNumberLeft(shapeRenderer, scoreInt, 28f, screenHeight - 12f, 5f, SCORE_COLOR);

        // 右上角：排行榜背景
        float rankBoxW = 100f;
        float rankBoxH = 30f;
        float rankBoxX = screenWidth - rankBoxW - 5f;
        float rankBoxY = screenHeight - rankBoxH - 5f;
        shapeRenderer.setColor(HUD_BG_COLOR);
        shapeRenderer.rect(rankBoxX, rankBoxY, rankBoxW, rankBoxH);

        // 排名数字
        int rank = world.getPlayerRank();
        PixelDrawer.drawNumber(shapeRenderer, rank, rankBoxX + rankBoxW * 0.5f, rankBoxY + rankBoxH - 5f, 4f, RANK_COLOR);

        // 排名指示（小菱形）
        shapeRenderer.setColor(1f, 0.8f, 0f, 1f);
        float cx = rankBoxX + 12f;
        float cy = rankBoxY + rankBoxH * 0.5f;
        shapeRenderer.rect(cx - 3f, cy, 6f, 3f);
        shapeRenderer.rect(cx, cy - 3f, 3f, 6f);

        // 暂停按钮
        shapeRenderer.setColor(PAUSE_COLOR);
        shapeRenderer.rect(pauseButtonX, pauseButtonY, pauseButtonSize, pauseButtonSize);
        // 暂停图标（两条竖线）
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        float barW = pauseButtonSize * 0.2f;
        float barH = pauseButtonSize * 0.5f;
        float barGap = pauseButtonSize * 0.15f;
        shapeRenderer.rect(
                pauseButtonX + pauseButtonSize * 0.3f - barW * 0.5f,
                pauseButtonY + (pauseButtonSize - barH) * 0.5f,
                barW, barH
        );
        shapeRenderer.rect(
                pauseButtonX + pauseButtonSize * 0.7f - barW * 0.5f,
                pauseButtonY + (pauseButtonSize - barH) * 0.5f,
                barW, barH
        );

        // 小地图（右下角）
        drawMinimap();

        // 暂停覆盖层
        if (paused) {
            shapeRenderer.setColor(0f, 0f, 0f, 0.6f);
            shapeRenderer.rect(0, 0, screenWidth, screenHeight);

            // 暂停文字指示（大黄色圆点）
            shapeRenderer.setColor(1f, 1f, 0f, 1f);
            shapeRenderer.circle(screenWidth * 0.5f, screenHeight * 0.5f + 30f, 15f);

            // 继续按钮（绿色）
            shapeRenderer.setColor(0.2f, 0.8f, 0.2f, 1f);
            shapeRenderer.rect(screenWidth * 0.5f - 60f, screenHeight * 0.5f - 40f, 120f, 40f);

            // 返回按钮（红色）
            shapeRenderer.setColor(0.8f, 0.2f, 0.2f, 1f);
            shapeRenderer.rect(screenWidth * 0.5f - 60f, screenHeight * 0.5f - 100f, 120f, 40f);
        }

        shapeRenderer.end();
    }

    private void drawMinimap() {
        float mapSize = Math.min(screenWidth, screenHeight) * 0.2f;
        float mapX = screenWidth - mapSize - 10f;
        float mapY = 10f;
        float scaleX = Math.max(0.001f, mapSize / GameWorld.WORLD_WIDTH);
        float scaleY = Math.max(0.001f, mapSize / GameWorld.WORLD_HEIGHT);

        // 背景
        shapeRenderer.setColor(0f, 0f, 0f, 0.5f);
        shapeRenderer.rect(mapX, mapY, mapSize, mapSize);

        // 边框
        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
        shapeRenderer.rect(mapX, mapY, mapSize, mapSize);

        // AI点
        if (world.aiBalls != null) {
            for (int i = 0; i < world.aiBalls.length; i++) {
                AIBall ai = world.aiBalls[i];
                if (ai == null || !ai.alive) continue;
                shapeRenderer.setColor(ai.r, ai.g, ai.b, 1f);
                float dotSize = Math.max(2f, ai.radius * scaleX * 0.5f);
                shapeRenderer.circle(mapX + ai.x * scaleX, mapY + ai.y * scaleY, dotSize);
            }
        }

        // 玩家点（白色）
        if (world.player.alive) {
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            float dotSize = Math.max(3f, world.player.radius * scaleX * 0.5f);
            shapeRenderer.circle(mapX + world.player.x * scaleX, mapY + world.player.y * scaleY, dotSize);
        }
    }

    private void saveScore() {
        try {
            int score = (int) world.player.score;
            com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("ballbattle_scores");
            // 获取现有分数
            int count = prefs.getInteger("score_count", 0);
            // 添加新分数
            prefs.putInteger("score_" + count, score);
            prefs.putInteger("score_count", count + 1);
            prefs.flush();
        } catch (Exception e) {
            Gdx.app.log("GameScreen", "Failed to save score: " + e.getMessage());
        }
    }

    // --- InputProcessor ---

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == com.badlogic.gdx.Input.Keys.ESCAPE || keycode == com.badlogic.gdx.Input.Keys.BACK) {
            if (paused) {
                paused = false;
            } else {
                paused = true;
            }
            return true;
        }
        if (keycode == com.badlogic.gdx.Input.Keys.SPACE) {
            if (!paused && !world.gameOver) {
                world.player.trySplit();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pointer == 0) {
            touchPointer = pointer;
            touchX = screenX;
            touchY = screenY;
            isTouching = true;

            if (paused) {
                handlePauseTouch(screenX, screenY);
                return true;
            }

            // 双击检测
            float now = System.nanoTime() / 1e9f;
            if (now - lastTapTime < DOUBLE_TAP_INTERVAL) {
                world.player.trySplit();
                lastTapTime = 0;
            } else {
                lastTapTime = now;
            }

            // 检查暂停按钮
            if (screenX >= pauseButtonX && screenX <= pauseButtonX + pauseButtonSize
                    && screenY >= pauseButtonY && screenY <= pauseButtonY + pauseButtonSize) {
                paused = true;
                return true;
            }
        }
        return true;
    }

    private void handlePauseTouch(float screenX, float screenY) {
        float centerX = screenWidth * 0.5f;
        float centerY = screenHeight * 0.5f;

        // 继续按钮
        if (screenX >= centerX - 60f && screenX <= centerX + 60f
                && screenY >= centerY - 40f && screenY <= centerY) {
            paused = false;
            return;
        }

        // 返回按钮
        if (screenX >= centerX - 60f && screenX <= centerX + 60f
                && screenY >= centerY - 100f && screenY <= centerY - 60f) {
            world.dispose();
            game.setScreen(new MainMenuScreen(game));
            return;
        }
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer == touchPointer) {
            isTouching = false;
            touchPointer = -1;
            // 停止移动
            if (world.player != null) {
                world.player.setMoveDirection(0, 0);
            }
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (pointer == touchPointer && !paused) {
            touchX = screenX;
            touchY = screenY;
            updatePlayerDirection();
        }
        return true;
    }

    private void updatePlayerDirection() {
        if (!isTouching || !world.player.alive) return;

        // 屏幕中心
        float centerX = screenWidth * 0.5f;
        float centerY = screenHeight * 0.5f;

        // 方向 = 手指位置 - 屏幕中心
        float dirX = touchX - centerX;
        float dirY = -(touchY - centerY); // Y轴翻转

        // 只有手指离中心足够远才移动
        float dist = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (dist > 10f) {
            world.player.setMoveDirection(dirX, dirY);
        } else {
            world.player.setMoveDirection(0, 0);
        }
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        if (pointer == touchPointer) {
            isTouching = false;
            touchPointer = -1;
            if (world.player != null) {
                world.player.setMoveDirection(0, 0);
            }
        }
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        // 不自动恢复，需要用户操作
    }

    @Override
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (world != null) {
            world.dispose();
        }
    }
}
