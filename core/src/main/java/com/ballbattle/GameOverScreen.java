package com.ballbattle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * 游戏结束屏幕 - 显示最终分数，提供重新开始和返回按钮
 */
public class GameOverScreen implements Screen, InputProcessor {

    private BallBattleGame game;
    private ShapeRenderer shapeRenderer;
    private int screenWidth;
    private int screenHeight;
    private boolean initialized;
    private int finalScore;
    private float animTimer;

    // 按钮
    private float restartBtnX, restartBtnY, restartBtnW, restartBtnH;
    private float menuBtnX, menuBtnY, menuBtnW, menuBtnH;

    // 颜色
    private static final Color BG_COLOR = new Color(0.05f, 0.02f, 0.02f, 1f);
    private static final Color SCORE_BG = new Color(0f, 0f, 0f, 0.5f);
    private static final Color SCORE_COLOR = new Color(1f, 1f, 1f, 1f);
    private static final Color RESTART_COLOR = new Color(0.2f, 0.7f, 0.2f, 1f);
    private static final Color MENU_COLOR = new Color(0.5f, 0.5f, 0.5f, 1f);
    private static final Color TITLE_COLOR = new Color(1f, 0.3f, 0.3f, 1f);

    public GameOverScreen(BallBattleGame game, int finalScore) {
        this.game = game;
        this.finalScore = finalScore;
        this.shapeRenderer = new ShapeRenderer();
        this.initialized = false;
        this.animTimer = 0;
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
        float btnH = Math.min(55f, height * 0.07f);
        float centerX = (width - btnW) * 0.5f;

        restartBtnW = btnW;
        restartBtnH = btnH;
        restartBtnX = centerX;
        restartBtnY = height * 0.35f;

        menuBtnW = btnW;
        menuBtnH = btnH;
        menuBtnX = centerX;
        menuBtnY = height * 0.35f - btnH - 20f;
    }

    @Override
    public void render(float delta) {
        if (!initialized) return;
        delta = Math.min(delta, 0.05f);
        animTimer += delta;

        // 清屏
        Gdx.gl.glClearColor(BG_COLOR.r, BG_COLOR.g, BG_COLOR.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 标题区域（红色大圆 = 游戏结束标志）
        float titleY = screenHeight * 0.7f;
        float pulse = 1f + 0.05f * (float) Math.sin(animTimer * 3f);
        shapeRenderer.setColor(TITLE_COLOR);
        shapeRenderer.circle(screenWidth * 0.5f, titleY, 40f * pulse);
        // 内部X标记
        shapeRenderer.setColor(1f, 1f, 1f, 0.8f);
        float xSize = 15f * pulse;
        shapeRenderer.rect(screenWidth * 0.5f - xSize, titleY - 2f, xSize * 2, 4f);
        shapeRenderer.rect(screenWidth * 0.5f - 2f, titleY - xSize, 4f, xSize * 2);

        // 分数背景
        float scoreBoxW = Math.min(300f, screenWidth * 0.7f);
        float scoreBoxH = 80f;
        float scoreBoxX = (screenWidth - scoreBoxW) * 0.5f;
        float scoreBoxY = screenHeight * 0.5f;
        shapeRenderer.setColor(SCORE_BG);
        shapeRenderer.rect(scoreBoxX, scoreBoxY, scoreBoxW, scoreBoxH);

        // 分数标签（黄色方块）
        shapeRenderer.setColor(1f, 1f, 0f, 1f);
        shapeRenderer.rect(scoreBoxX + 15f, scoreBoxY + scoreBoxH - 25f, 10f, 10f);

        // 分数数字
        float pixelSize = Math.min(8f, scoreBoxW * 0.04f);
        PixelDrawer.drawNumber(shapeRenderer, finalScore,
                scoreBoxX + scoreBoxW * 0.5f, scoreBoxY + scoreBoxH * 0.35f,
                pixelSize, SCORE_COLOR);

        // 重新开始按钮
        shapeRenderer.setColor(0f, 0f, 0f, 0.3f);
        shapeRenderer.rect(restartBtnX + 3f, restartBtnY - 3f, restartBtnW, restartBtnH);
        shapeRenderer.setColor(RESTART_COLOR);
        shapeRenderer.rect(restartBtnX, restartBtnY, restartBtnW, restartBtnH);
        shapeRenderer.setColor(1f, 1f, 1f, 0.15f);
        shapeRenderer.rect(restartBtnX, restartBtnY + restartBtnH * 0.6f, restartBtnW, restartBtnH * 0.4f);
        // 图标：圆形箭头（简化为圆+线）
        shapeRenderer.setColor(1f, 1f, 1f, 0.8f);
        float iconCX = restartBtnX + 30f;
        float iconCY = restartBtnY + restartBtnH * 0.5f;
        shapeRenderer.circle(iconCX, iconCY, 10f);

        // 返回菜单按钮
        shapeRenderer.setColor(0f, 0f, 0f, 0.3f);
        shapeRenderer.rect(menuBtnX + 3f, menuBtnY - 3f, menuBtnW, menuBtnH);
        shapeRenderer.setColor(MENU_COLOR);
        shapeRenderer.rect(menuBtnX, menuBtnY, menuBtnW, menuBtnH);
        shapeRenderer.setColor(1f, 1f, 1f, 0.15f);
        shapeRenderer.rect(menuBtnX, menuBtnY + menuBtnH * 0.6f, menuBtnW, menuBtnH * 0.4f);
        // 图标：小房子（简化为矩形+三角）
        shapeRenderer.setColor(1f, 1f, 1f, 0.8f);
        float hIconCX = menuBtnX + 30f;
        float hIconCY = menuBtnY + menuBtnH * 0.5f;
        shapeRenderer.rect(hIconCX - 8f, hIconCY - 8f, 16f, 12f);
        shapeRenderer.rect(hIconCX - 4f, hIconCY - 4f, 4f, 8f);

        shapeRenderer.end();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pointer != 0 || !initialized) return false;

        float touchYConverted = screenHeight - screenY;

        // 重新开始按钮
        if (screenX >= restartBtnX && screenX <= restartBtnX + restartBtnW
                && touchYConverted >= restartBtnY && touchYConverted <= restartBtnY + restartBtnH) {
            game.setScreen(new GameScreen(game, 0));
            return true;
        }

        // 返回菜单按钮
        if (screenX >= menuBtnX && screenX <= menuBtnX + menuBtnW
                && touchYConverted >= menuBtnY && touchYConverted <= menuBtnY + menuBtnH) {
            game.setScreen(new MainMenuScreen(game));
            return true;
        }

        return true;
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
        if (keycode == com.badlogic.gdx.Input.Keys.ENTER) {
            game.setScreen(new GameScreen(game, 0));
            return true;
        }
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
