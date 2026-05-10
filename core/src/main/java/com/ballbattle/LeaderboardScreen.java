package com.ballbattle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * 排行榜屏幕 - 用Preferences存储历史分数
 */
public class LeaderboardScreen implements Screen, InputProcessor {

    private BallBattleGame game;
    private ShapeRenderer shapeRenderer;
    private int screenWidth;
    private int screenHeight;
    private boolean initialized;

    // 分数数据
    private int[] scores;
    private int scoreCount;

    // 按钮
    private float backBtnX, backBtnY, backBtnW, backBtnH;
    private float clearBtnX, clearBtnY, clearBtnW, clearBtnH;

    // 颜色
    private static final Color BG_COLOR = new Color(0.03f, 0.03f, 0.06f, 1f);
    private static final Color ENTRY_BG = new Color(0f, 0f, 0f, 0.4f);
    private static final Color GOLD_COLOR = new Color(1f, 0.85f, 0f, 1f);
    private static final Color SILVER_COLOR = new Color(0.75f, 0.75f, 0.8f, 1f);
    private static final Color BRONZE_COLOR = new Color(0.8f, 0.5f, 0.2f, 1f);
    private static final Color NORMAL_COLOR = new Color(0.6f, 0.6f, 0.6f, 1f);
    private static final Color SCORE_TEXT_COLOR = new Color(1f, 1f, 1f, 1f);
    private static final Color BACK_COLOR = new Color(0.3f, 0.3f, 0.7f, 1f);
    private static final Color CLEAR_COLOR = new Color(0.6f, 0.2f, 0.2f, 1f);
    private static final Color TITLE_COLOR = new Color(1f, 0.85f, 0f, 1f);

    private static final int MAX_DISPLAY = 10;

    public LeaderboardScreen(BallBattleGame game) {
        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
        this.initialized = false;
        this.scores = new int[MAX_DISPLAY];
        this.scoreCount = 0;
        loadScores();
    }

    private void loadScores() {
        try {
            Preferences prefs = Gdx.app.getPreferences("ballbattle_scores");
            int total = prefs.getInteger("score_count", 0);
            scoreCount = 0;

            // 收集所有分数
            int[] allScores = new int[total];
            for (int i = 0; i < total; i++) {
                allScores[i] = prefs.getInteger("score_" + i, 0);
            }

            // 排序（降序）
            for (int i = 0; i < allScores.length - 1; i++) {
                for (int j = i + 1; j < allScores.length; j++) {
                    if (allScores[j] > allScores[i]) {
                        int temp = allScores[i];
                        allScores[i] = allScores[j];
                        allScores[j] = temp;
                    }
                }
            }

            // 取前MAX_DISPLAY个
            int displayCount = Math.min(total, MAX_DISPLAY);
            for (int i = 0; i < displayCount; i++) {
                scores[i] = allScores[i];
            }
            scoreCount = displayCount;
        } catch (Exception e) {
            Gdx.app.log("LeaderboardScreen", "Failed to load scores: " + e.getMessage());
            scoreCount = 0;
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        this.screenWidth = width;
        this.screenHeight = height;
        this.initialized = true;

        float btnW = Math.min(250f, width * 0.6f);
        float btnH = Math.min(50f, height * 0.06f);
        float centerX = (width - btnW) * 0.5f;

        backBtnW = btnW;
        backBtnH = btnH;
        backBtnX = centerX;
        backBtnY = height * 0.08f;

        clearBtnW = btnW * 0.6f;
        clearBtnH = btnH;
        clearBtnX = centerX;
        clearBtnY = height * 0.08f + btnH + 10f;
    }

    @Override
    public void render(float delta) {
        if (!initialized) return;

        // 清屏
        Gdx.gl.glClearColor(BG_COLOR.r, BG_COLOR.g, BG_COLOR.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 标题（金色大圆）
        float titleY = screenHeight * 0.9f;
        shapeRenderer.setColor(TITLE_COLOR);
        shapeRenderer.circle(screenWidth * 0.5f, titleY, 25f);
        // 奖杯形状（简化）
        shapeRenderer.setColor(1f, 0.9f, 0.3f, 0.8f);
        shapeRenderer.rect(screenWidth * 0.5f - 12f, titleY - 18f, 24f, 15f);
        shapeRenderer.rect(screenWidth * 0.5f - 4f, titleY - 25f, 8f, 8f);

        // 排行榜条目
        float entryStartY = screenHeight * 0.82f;
        float entryHeight = Math.min(45f, screenHeight * 0.05f);
        float entryGap = 5f;
        float entryWidth = Math.min(350f, screenWidth * 0.8f);
        float entryX = (screenWidth - entryWidth) * 0.5f;

        for (int i = 0; i < scoreCount; i++) {
            float entryY = entryStartY - i * (entryHeight + entryGap);

            // 条目背景
            shapeRenderer.setColor(ENTRY_BG);
            shapeRenderer.rect(entryX, entryY, entryWidth, entryHeight);

            // 排名颜色
            Color rankColor;
            if (i == 0) rankColor = GOLD_COLOR;
            else if (i == 1) rankColor = SILVER_COLOR;
            else if (i == 2) rankColor = BRONZE_COLOR;
            else rankColor = NORMAL_COLOR;

            // 排名指示（左侧圆点）
            shapeRenderer.setColor(rankColor);
            float dotRadius = (i < 3) ? 8f : 5f;
            shapeRenderer.circle(entryX + 20f, entryY + entryHeight * 0.5f, dotRadius);

            // 排名数字
            float pixelSize = Math.min(5f, entryHeight * 0.15f);
            PixelDrawer.drawNumberLeft(shapeRenderer, i + 1,
                    entryX + 35f, entryY + entryHeight * 0.65f, pixelSize, rankColor);

            // 分数数字
            PixelDrawer.drawNumberLeft(shapeRenderer, scores[i],
                    entryX + entryWidth * 0.5f, entryY + entryHeight * 0.65f, pixelSize, SCORE_TEXT_COLOR);

            // 分隔线
            if (i < scoreCount - 1) {
                shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 0.5f);
                shapeRenderer.rect(entryX + 10f, entryY - 1f, entryWidth - 20f, 1f);
            }
        }

        // 无数据提示
        if (scoreCount == 0) {
            shapeRenderer.setColor(0.4f, 0.4f, 0.4f, 1f);
            float centerY = screenHeight * 0.5f;
            shapeRenderer.circle(screenWidth * 0.5f, centerY, 15f);
            shapeRenderer.circle(screenWidth * 0.5f - 25f, centerY, 8f);
            shapeRenderer.circle(screenWidth * 0.5f + 25f, centerY, 8f);
        }

        // 返回按钮
        shapeRenderer.setColor(0f, 0f, 0f, 0.3f);
        shapeRenderer.rect(backBtnX + 3f, backBtnY - 3f, backBtnW, backBtnH);
        shapeRenderer.setColor(BACK_COLOR);
        shapeRenderer.rect(backBtnX, backBtnY, backBtnW, backBtnH);
        shapeRenderer.setColor(1f, 1f, 1f, 0.15f);
        shapeRenderer.rect(backBtnX, backBtnY + backBtnH * 0.6f, backBtnW, backBtnH * 0.4f);
        // 返回图标（箭头）
        shapeRenderer.setColor(1f, 1f, 1f, 0.8f);
        float arrowX = backBtnX + 25f;
        float arrowY = backBtnY + backBtnH * 0.5f;
        shapeRenderer.rect(arrowX - 8f, arrowY - 2f, 12f, 4f);
        shapeRenderer.rect(arrowX - 8f, arrowY - 2f, 4f, -6f);
        shapeRenderer.rect(arrowX - 8f, arrowY + 2f, 4f, 6f);

        // 清除按钮
        shapeRenderer.setColor(0f, 0f, 0f, 0.3f);
        shapeRenderer.rect(clearBtnX + 3f, clearBtnY - 3f, clearBtnW, clearBtnH);
        shapeRenderer.setColor(CLEAR_COLOR);
        shapeRenderer.rect(clearBtnX, clearBtnY, clearBtnW, clearBtnH);
        // 清除图标（X）
        shapeRenderer.setColor(1f, 1f, 1f, 0.8f);
        float clearIconX = clearBtnX + 20f;
        float clearIconY = clearBtnY + clearBtnH * 0.5f;
        shapeRenderer.rect(clearIconX - 6f, clearIconY - 1.5f, 12f, 3f);
        shapeRenderer.rect(clearIconX - 1.5f, clearIconY - 6f, 3f, 12f);

        shapeRenderer.end();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pointer != 0 || !initialized) return false;

        float touchYConverted = screenHeight - screenY;

        // 返回按钮
        if (screenX >= backBtnX && screenX <= backBtnX + backBtnW
                && touchYConverted >= backBtnY && touchYConverted <= backBtnY + backBtnH) {
            game.setScreen(new MainMenuScreen(game));
            return true;
        }

        // 清除按钮
        if (screenX >= clearBtnX && screenX <= clearBtnX + clearBtnW
                && touchYConverted >= clearBtnY && touchYConverted <= clearBtnY + clearBtnH) {
            clearScores();
            return true;
        }

        return true;
    }

    private void clearScores() {
        try {
            Preferences prefs = Gdx.app.getPreferences("ballbattle_scores");
            prefs.clear();
            prefs.flush();
            scoreCount = 0;
        } catch (Exception e) {
            Gdx.app.log("LeaderboardScreen", "Failed to clear scores: " + e.getMessage());
        }
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == com.badlogic.gdx.Input.Keys.ESCAPE || keycode == com.badlogic.gdx.Input.Keys.BACK) {
            game.setScreen(new MainMenuScreen(game));
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
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

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
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
