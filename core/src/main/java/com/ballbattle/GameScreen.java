package com.ballbattle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * 游戏主屏幕 - 核心游戏逻辑和渲染
 * 包含：GameWorld更新、Camera跟随、渲染所有对象、HUD、触摸控制、游戏结束检测
 */
public class GameScreen implements Screen, InputProcessor {

    private BallBattleGame game;
    private int gameMode;
    private GameWorld gameWorld;
    private CameraController cameraController;

    // 触摸控制
    private Vector2 touchPos;
    private boolean isTouching;
    private boolean isBoosting;
    private float doubleTapTimer;
    private static final float DOUBLE_TAP_INTERVAL = 0.3f;

    // 游戏结束延迟
    private float gameOverDelay;
    private static final float GAME_OVER_DELAY = 1.5f;

    // 网格绘制
    private static final int GRID_SIZE = 100;

    // 小地图
    private float minimapSize;
    private float minimapMargin;
    private float minimapX;
    private float minimapY;

    // HUD
    private float hudMargin;

    // 文字测量
    private GlyphLayout glyphLayout;

    public GameScreen(BallBattleGame game, int gameMode) {
        this.game = game;
        this.gameMode = gameMode;
        this.touchPos = new Vector2();
        this.isTouching = false;
        this.isBoosting = false;
        this.doubleTapTimer = 0;
        this.gameOverDelay = 0;
        this.glyphLayout = new GlyphLayout();

        // 计算UI尺寸
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        this.minimapSize = Math.min(screenWidth, screenHeight) * 0.2f;
        this.minimapMargin = 15;
        this.minimapX = screenWidth - minimapSize - minimapMargin;
        this.minimapY = minimapMargin;
        this.hudMargin = 15;

        // 创建相机
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);
        this.cameraController = new CameraController(camera);

        // 创建游戏世界
        this.gameWorld = new GameWorld(gameMode);

        // 设置输入处理器
        Gdx.input.setInputProcessor(this);

        // 初始化相机位置
        Player player = gameWorld.getPlayer();
        cameraController.setPositionImmediately(player.x, player.y);
    }

    @Override
    public void render(float delta) {
        // 限制delta防止跳帧
        delta = Math.min(delta, 0.05f);

        // 更新双击计时器
        if (doubleTapTimer > 0) {
            doubleTapTimer -= delta;
        }

        // 更新游戏世界
        gameWorld.update(delta);

        // 同步世界尺寸到相机（生存模式地图缩小）
        cameraController.setWorldSize(gameWorld.getCurrentWorldWidth(), gameWorld.getCurrentWorldHeight());

        // 更新相机
        Player player = gameWorld.getPlayer();
        if (player.alive) {
            cameraController.update(player.x, player.y, player.radius, delta);
        }

        // 检查游戏结束
        if (gameWorld.isGameOver()) {
            gameOverDelay += delta;
            if (gameOverDelay >= GAME_OVER_DELAY) {
                // 保存分数
                saveScore(player.score, (int) player.maxRadius, player.survivalTime);
                // 切换到游戏结束界面
                game.setScreen(new GameOverScreen(game, player.score,
                        (int) player.maxRadius, player.survivalTime, gameMode));
                return;
            }
        }

        // ===== 渲染 =====
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // 设置相机投影
        OrthographicCamera cam = cameraController.getCamera();
        game.shapeRenderer.setProjectionMatrix(cam.combined);

        // 1. 绘制背景网格
        drawGrid(cam);

        // 2. 绘制世界边界
        drawWorldBorder();

        // 3. 绘制食物
        drawFoods();

        // 4. 绘制粒子效果
        drawParticles();

        // 5. 绘制AI球
        drawAIBalls();

        // 6. 绘制玩家分裂球
        drawSplitBalls();

        // 7. 绘制玩家
        drawPlayer();

        // ===== HUD（使用屏幕坐标）=====
        OrthographicCamera hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.shapeRenderer.setProjectionMatrix(hudCamera.combined);
        game.batch.setProjectionMatrix(hudCamera.combined);

        // 8. 绘制HUD
        drawHUD();

        // 9. 绘制小地图
        drawMinimap();

        // 10. 绘制排行榜
        drawLeaderboard();

        // 11. 团队模式显示队伍分数
        if (gameMode == BallBattleGame.MODE_TEAMS) {
            drawTeamScores();
        }

        // 12. 生存模式显示地图缩小警告
        if (gameMode == BallBattleGame.MODE_SURVIVAL) {
            drawSurvivalInfo();
        }

        // 13. 游戏结束遮罩
        if (gameWorld.isGameOver()) {
            drawGameOverOverlay();
        }
    }

    // ========== 绘制方法 ==========

    private void drawGrid(OrthographicCamera cam) {
        float zoom = cameraController.getCurrentZoom();
        float left = cam.position.x - cam.viewportWidth * zoom / 2f;
        float right = cam.position.x + cam.viewportWidth * zoom / 2f;
        float bottom = cam.position.y - cam.viewportHeight * zoom / 2f;
        float top = cam.position.y + cam.viewportHeight * zoom / 2f;

        int startX = (int) (left / GRID_SIZE) * GRID_SIZE;
        int startY = (int) (bottom / GRID_SIZE) * GRID_SIZE;

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(0.15f, 0.15f, 0.2f, 0.5f);

        for (int x = startX; x <= right; x += GRID_SIZE) {
            if (x < 0 || x > BallBattleGame.WORLD_WIDTH) continue;
            game.shapeRenderer.rect(x - 0.5f, Math.max(0, bottom), 1f, Math.min(top, BallBattleGame.WORLD_HEIGHT) - Math.max(0, bottom));
        }
        for (int y = startY; y <= top; y += GRID_SIZE) {
            if (y < 0 || y > BallBattleGame.WORLD_HEIGHT) continue;
            game.shapeRenderer.rect(Math.max(0, left), y - 0.5f, Math.min(right, BallBattleGame.WORLD_WIDTH) - Math.max(0, left), 1f);
        }
        game.shapeRenderer.end();
    }

    private void drawWorldBorder() {
        float ww = gameWorld.getCurrentWorldWidth();
        float wh = gameWorld.getCurrentWorldHeight();

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(0.8f, 0.2f, 0.2f, 0.8f);
        // 上边框
        game.shapeRenderer.rect(0, wh - 5, ww, 5);
        // 下边框
        game.shapeRenderer.rect(0, 0, ww, 5);
        // 左边框
        game.shapeRenderer.rect(0, 0, 5, wh);
        // 右边框
        game.shapeRenderer.rect(ww - 5, 0, 5, wh);
        game.shapeRenderer.end();
    }

    private void drawFoods() {
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Array<Food> foods = gameWorld.getFoods();
        for (Food f : foods) {
            game.shapeRenderer.setColor(f.color);
            game.shapeRenderer.circle(f.x, f.y, f.radius);
        }
        game.shapeRenderer.end();
    }

    private void drawParticles() {
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Array<ParticleEffect> particles = gameWorld.getParticles();
        for (ParticleEffect p : particles) {
            Color c = p.color;
            game.shapeRenderer.setColor(c.r, c.g, c.b, p.getAlpha());
            game.shapeRenderer.circle(p.x, p.y, p.radius);
        }
        game.shapeRenderer.end();
    }

    private void drawAIBalls() {
        Array<AIBall> aiBalls = gameWorld.getAIBalls();
        for (AIBall ai : aiBalls) {
            if (!ai.alive) continue;
            drawBall(ai);
        }
    }

    private void drawSplitBalls() {
        Array<Player> splitBalls = gameWorld.getSplitBalls();
        for (Player sb : splitBalls) {
            if (!sb.alive) continue;
            drawBall(sb);
        }
    }

    private void drawPlayer() {
        Player player = gameWorld.getPlayer();
        if (player.alive) {
            drawBall(player);
        }
    }

    private void drawBall(Player ball) {
        // 绘制球体
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 球体阴影
        game.shapeRenderer.setColor(0, 0, 0, 0.3f);
        game.shapeRenderer.circle(ball.x + 3, ball.y - 3, ball.radius);

        // 球体本体
        game.shapeRenderer.setColor(ball.color);
        game.shapeRenderer.circle(ball.x, ball.y, ball.radius);

        // 球体高光
        game.shapeRenderer.setColor(1, 1, 1, 0.2f);
        game.shapeRenderer.circle(ball.x - ball.radius * 0.25f, ball.y + ball.radius * 0.25f, ball.radius * 0.5f);

        // 球体边框
        game.shapeRenderer.setColor(ball.color.r * 0.7f, ball.color.g * 0.7f, ball.color.b * 0.7f, 1f);
        game.shapeRenderer.end();
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        game.shapeRenderer.circle(ball.x, ball.y, ball.radius);

        // 加速效果
        if (ball.isBoosting()) {
            game.shapeRenderer.setColor(1, 1, 0.5f, 0.5f);
            game.shapeRenderer.circle(ball.x, ball.y, ball.radius + 5);
        }
        game.shapeRenderer.end();

        // 绘制名字
        game.batch.begin();
        game.font.setColor(Color.WHITE);
        glyphLayout.setText(game.font, ball.name);
        game.font.draw(game.batch, ball.name,
                ball.x - glyphLayout.width / 2f, ball.y + glyphLayout.height / 2f);

        // 绘制质量数字
        if (ball.radius > 20) {
            String massText = String.valueOf((int) ball.mass);
            glyphLayout.setText(game.font, massText);
            game.font.setColor(Color.LIGHT_GRAY);
            game.font.draw(game.batch, massText,
                    ball.x - glyphLayout.width / 2f, ball.y - glyphLayout.height * 0.8f);
        }
        game.batch.end();
    }

    private void drawHUD() {
        Player player = gameWorld.getPlayer();
        float screenWidth = Gdx.graphics.getWidth();

        // 左上角 - 分数
        game.batch.begin();
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "分数: " + player.score, hudMargin,
                Gdx.graphics.getHeight() - hudMargin);

        // 质量
        game.font.draw(game.batch, "质量: " + (int) player.mass, hudMargin,
                Gdx.graphics.getHeight() - hudMargin - 25);

        // 存活时间
        int minutes = (int) (player.survivalTime / 60);
        int seconds = (int) (player.survivalTime % 60);
        game.font.draw(game.batch, "时间: " + String.format("%d:%02d", minutes, seconds),
                hudMargin, Gdx.graphics.getHeight() - hudMargin - 50);

        // 模式名称
        String modeName;
        switch (gameMode) {
            case BallBattleGame.MODE_FFA: modeName = "自由模式"; break;
            case BallBattleGame.MODE_TEAMS: modeName = "团队模式"; break;
            case BallBattleGame.MODE_SURVIVAL: modeName = "生存模式"; break;
            default: modeName = "";
        }
        glyphLayout.setText(game.font, modeName);
        float modeWidth = glyphLayout.width;
        game.font.setColor(Color.YELLOW);
        game.font.draw(game.batch, modeName, screenWidth - modeWidth - hudMargin,
                Gdx.graphics.getHeight() - hudMargin);

        game.batch.end();
    }

    private void drawMinimap() {
        float ww = gameWorld.getCurrentWorldWidth();
        float wh = gameWorld.getCurrentWorldHeight();

        // 小地图背景
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(0, 0, 0, 0.5f);
        game.shapeRenderer.rect(minimapX, minimapY, minimapSize, minimapSize);
        game.shapeRenderer.end();

        // 小地图边框
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        game.shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.8f);
        game.shapeRenderer.rect(minimapX, minimapY, minimapSize, minimapSize);
        game.shapeRenderer.end();

        // 绘制小地图上的点
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float scaleX = minimapSize / ww;
        float scaleY = minimapSize / wh;

        // AI点
        Array<AIBall> aiBalls = gameWorld.getAIBalls();
        for (AIBall ai : aiBalls) {
            if (!ai.alive) continue;
            game.shapeRenderer.setColor(ai.color);
            float dotSize = Math.max(2, ai.radius * scaleX * 0.5f);
            game.shapeRenderer.circle(
                    minimapX + ai.x * scaleX,
                    minimapY + ai.y * scaleY,
                    dotSize);
        }

        // 玩家点
        Player player = gameWorld.getPlayer();
        if (player.alive) {
            game.shapeRenderer.setColor(Color.WHITE);
            float dotSize = Math.max(3, player.radius * scaleX * 0.5f);
            game.shapeRenderer.circle(
                    minimapX + player.x * scaleX,
                    minimapY + player.y * scaleY,
                    dotSize);
        }

        game.shapeRenderer.end();
    }

    private void drawLeaderboard() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float lbX = screenWidth - 180 - hudMargin;
        float lbY = screenHeight - 80;
        float lbWidth = 180;
        float lbHeight = 25 * 6 + 30; // 标题 + 6行

        // 背景
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(0, 0, 0, 0.4f);
        game.shapeRenderer.rect(lbX, lbY - lbHeight, lbWidth, lbHeight);
        game.shapeRenderer.end();

        // 文字
        game.batch.begin();
        game.font.setColor(Color.YELLOW);
        game.font.draw(game.batch, "排行榜", lbX + 10, lbY - 5);

        Array<Player> leaderboard = gameWorld.getLeaderboard();
        int count = Math.min(6, leaderboard.size);
        for (int i = 0; i < count; i++) {
            Player p = leaderboard.get(i);
            if (i == 0) {
                game.font.setColor(Color.GOLD);
            } else if (i == 1) {
                game.font.setColor(Color.LIGHT_GRAY);
            } else if (i == 2) {
                game.font.setColor(Color.ORANGE);
            } else {
                game.font.setColor(Color.WHITE);
            }

            String entry = (i + 1) + ". " + p.name + " (" + (int) p.mass + ")";
            game.font.draw(game.batch, entry, lbX + 10, lbY - 30 - i * 25);
        }
        game.batch.end();
    }

    private void drawTeamScores() {
        float screenWidth = Gdx.graphics.getWidth();
        float cx = screenWidth / 2f;
        float y = Gdx.graphics.getHeight() - hudMargin - 10;

        float redScore = gameWorld.getTeamScore(0);
        float blueScore = gameWorld.getTeamScore(1);

        game.batch.begin();
        game.font.setColor(Color.RED);
        String redText = "红队: " + (int) redScore;
        game.font.draw(game.batch, redText, cx - 100, y);

        game.font.setColor(Color.BLUE);
        String blueText = "蓝队: " + (int) blueScore;
        game.font.draw(game.batch, blueText, cx + 20, y);
        game.batch.end();
    }

    private void drawSurvivalInfo() {
        float ww = gameWorld.getCurrentWorldWidth();
        float wh = gameWorld.getCurrentWorldHeight();

        if (ww < BallBattleGame.WORLD_WIDTH) {
            float shrinkPercent = (1f - ww / BallBattleGame.WORLD_WIDTH) * 100;
            game.batch.begin();
            game.font.setColor(Color.RED);
            game.font.draw(game.batch, "地图缩小: " + String.format("%.0f%%", shrinkPercent),
                    hudMargin, Gdx.graphics.getHeight() - hudMargin - 75);
            game.batch.end();
        }
    }

    private void drawGameOverOverlay() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float alpha = Math.min(1f, gameOverDelay / GAME_OVER_DELAY);

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(0, 0, 0, alpha * 0.5f);
        game.shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        game.shapeRenderer.end();

        game.batch.begin();
        game.fontLarge.setColor(1, 1, 1, alpha);
        String goText = "游戏结束";
        glyphLayout.setText(game.fontLarge, goText);
        game.fontLarge.draw(game.batch, goText,
                (screenWidth - glyphLayout.width) / 2f, screenHeight / 2f + 20);
        game.batch.end();
    }

    // ========== 输入处理 ==========

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (gameWorld.isGameOver()) return false;

        float touchY = Gdx.graphics.getHeight() - screenY;
        touchPos.set(screenX, touchY);
        isTouching = true;

        // 双击检测
        if (doubleTapTimer > 0) {
            // 双击 = 分裂
            Player split = gameWorld.playerSplit();
            if (split != null) {
                gameWorld.getSplitBalls().add(split);
            }
            doubleTapTimer = 0;
        } else {
            doubleTapTimer = DOUBLE_TAP_INTERVAL;
        }

        // 长按检测（用于加速）- 在touchDragged中处理
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (gameWorld.isGameOver()) return false;

        float touchY = Gdx.graphics.getHeight() - screenY;
        touchPos.set(screenX, touchY);

        // 计算移动方向
        updatePlayerMovement();

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer == 0) {
            isTouching = false;
            isBoosting = false;
            gameWorld.setPlayerBoosting(false);
            gameWorld.setPlayerMoveDirection(0, false);
        }
        return true;
    }

    private void updatePlayerMovement() {
        if (!isTouching || gameWorld.isGameOver()) return;

        float screenCenterX = Gdx.graphics.getWidth() / 2f;
        float screenCenterY = Gdx.graphics.getHeight() / 2f;

        float dx = touchPos.x - screenCenterX;
        float dy = touchPos.y - screenCenterY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist > 10) { // 死区
            float angle = (float) Math.atan2(dy, dx);
            gameWorld.setPlayerMoveDirection(angle, true);

            // 距离较远时自动加速
            float maxDist = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) / 2f;
            if (dist > maxDist * 0.7f) {
                isBoosting = true;
                gameWorld.setPlayerBoosting(true);
            } else {
                isBoosting = false;
                gameWorld.setPlayerBoosting(false);
            }
        } else {
            gameWorld.setPlayerMoveDirection(0, false);
            isBoosting = false;
            gameWorld.setPlayerBoosting(false);
        }
    }

    // ========== 键盘支持（桌面端） ==========

    @Override
    public boolean keyDown(int keycode) {
        if (gameWorld.isGameOver()) return false;

        if (keycode == com.badlogic.gdx.Input.Keys.SPACE) {
            Player split = gameWorld.playerSplit();
            if (split != null) {
                gameWorld.getSplitBalls().add(split);
            }
        }
        if (keycode == com.badlogic.gdx.Input.Keys.SHIFT_LEFT ||
            keycode == com.badlogic.gdx.Input.Keys.SHIFT_RIGHT) {
            gameWorld.setPlayerBoosting(true);
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == com.badlogic.gdx.Input.Keys.SHIFT_LEFT ||
            keycode == com.badlogic.gdx.Input.Keys.SHIFT_RIGHT) {
            gameWorld.setPlayerBoosting(false);
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) { return false; }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // 桌面端鼠标控制
        if (!Gdx.input.isTouched() && !gameWorld.isGameOver()) {
            float touchY = Gdx.graphics.getHeight() - screenY;
            touchPos.set(screenX, touchY);
            isTouching = true;
            updatePlayerMovement();
        }
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) { return false; }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    // ========== 分数保存 ==========

    private void saveScore(int score, int maxRadius, float survivalTime) {
        com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("ballbattle_scores");

        // 获取现有分数
        int count = prefs.getInteger("score_count", 0);
        if (count >= 10) count = 10; // 最多保存10条

        // 插入新分数（按分数排序）
        int insertIndex = count;
        for (int i = 0; i < count; i++) {
            int existingScore = prefs.getInteger("score_" + i, 0);
            if (score > existingScore) {
                insertIndex = i;
                break;
            }
        }

        // 移动后面的分数
        for (int i = Math.min(count, 9); i > insertIndex; i--) {
            prefs.putInteger("score_" + i, prefs.getInteger("score_" + (i - 1), 0));
            prefs.putInteger("max_radius_" + i, prefs.getInteger("max_radius_" + (i - 1), 0));
            prefs.putFloat("survival_time_" + i, prefs.getFloat("survival_time_" + (i - 1), 0));
            prefs.putInteger("mode_" + i, prefs.getInteger("mode_" + (i - 1), 0));
        }

        // 插入新分数
        if (insertIndex < 10) {
            prefs.putInteger("score_" + insertIndex, score);
            prefs.putInteger("max_radius_" + insertIndex, maxRadius);
            prefs.putFloat("survival_time_" + insertIndex, survivalTime);
            prefs.putInteger("mode_" + insertIndex, gameMode);
            prefs.putInteger("score_count", Math.min(count + 1, 10));
        }

        prefs.flush();
    }

    @Override
    public void resize(int width, int height) {
        cameraController.getCamera().setToOrtho(false, width, height);
    }

    @Override
    public void show() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        Gdx.input.setInputProcessor(null);
    }
}
