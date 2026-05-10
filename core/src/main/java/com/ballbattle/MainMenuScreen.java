package com.ballbattle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * 主菜单屏幕 - 用ShapeRenderer画按钮（彩色矩形），不用文字
 */
public class MainMenuScreen implements Screen, InputProcessor {

    private BallBattleGame game;
    private ShapeRenderer shapeRenderer;
    private int screenWidth;
    private int screenHeight;
    private boolean initialized;

    // 按钮定义
    private static final int BUTTON_COUNT = 4;
    private float[] buttonX = new float[BUTTON_COUNT];
    private float[] buttonY = new float[BUTTON_COUNT];
    private float buttonWidth;
    private float buttonHeight;
    private float buttonGap;

    // 按钮颜色
    private static final Color[] BUTTON_COLORS = {
            new Color(0.2f, 0.8f, 0.2f, 1f),   // 绿色 = 自由模式
            new Color(0.2f, 0.4f, 0.9f, 1f),   // 蓝色 = 团队模式
            new Color(0.9f, 0.2f, 0.2f, 1f),   // 红色 = 生存模式
            new Color(0.9f, 0.8f, 0.1f, 1f)    // 黄色 = 排行榜
    };

    // 装饰球动画
    private float[] decoBallX;
    private float[] decoBallY;
    private float[] decoBallR;
    private float[] decoBallSpeed;
    private float[] decoBallHue;
    private static final int DECO_BALL_COUNT = 8;

    // 背景颜色
    private static final Color BG_COLOR = new Color(0.04f, 0.04f, 0.08f, 1f);
    private static final Color TITLE_COLOR = new Color(1f, 0.5f, 0.1f, 1f);

    public MainMenuScreen(BallBattleGame game) {
        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
        this.initialized = false;

        // 初始化装饰球
        decoBallX = new float[DECO_BALL_COUNT];
        decoBallY = new float[DECO_BALL_COUNT];
        decoBallR = new float[DECO_BALL_COUNT];
        decoBallSpeed = new float[DECO_BALL_COUNT];
        decoBallHue = new float[DECO_BALL_COUNT];
        for (int i = 0; i < DECO_BALL_COUNT; i++) {
            decoBallX[i] = (float) Math.random();
            decoBallY[i] = (float) Math.random();
            decoBallR[i] = 0.02f + (float) Math.random() * 0.04f;
            decoBallSpeed[i] = 0.02f + (float) Math.random() * 0.03f;
            decoBallHue[i] = (float) Math.random() * 360f;
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

        // 计算按钮布局
        buttonWidth = Math.min(300f, width * 0.7f);
        buttonHeight = Math.min(60f, height * 0.08f);
        buttonGap = buttonHeight * 0.4f;
        float totalHeight = BUTTON_COUNT * buttonHeight + (BUTTON_COUNT - 1) * buttonGap;
        float startY = height * 0.5f + totalHeight * 0.5f - buttonHeight;

        for (int i = 0; i < BUTTON_COUNT; i++) {
            buttonX[i] = (width - buttonWidth) * 0.5f;
            buttonY[i] = startY - i * (buttonHeight + buttonGap);
        }
    }

    @Override
    public void render(float delta) {
        if (!initialized) return;

        delta = Math.min(delta, 0.05f);

        // 清屏
        Gdx.gl.glClearColor(BG_COLOR.r, BG_COLOR.g, BG_COLOR.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 更新装饰球
        for (int i = 0; i < DECO_BALL_COUNT; i++) {
            decoBallX[i] += decoBallSpeed[i] * delta * 0.3f;
            decoBallY[i] += decoBallSpeed[i] * delta * 0.2f;
            if (decoBallX[i] > 1.1f) decoBallX[i] = -0.1f;
            if (decoBallY[i] > 1.1f) decoBallY[i] = -0.1f;
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 画装饰球
        for (int i = 0; i < DECO_BALL_COUNT; i++) {
            float hue = decoBallHue[i];
            float c = 0.8f;
            float x2 = c * (1 - Math.abs(((hue / 60f) % 2f) - 1f));
            float m = 0.9f - c;
            float rr, gg, bb;
            if (hue < 60) { rr = c; gg = x2; bb = 0; }
            else if (hue < 120) { rr = x2; gg = c; bb = 0; }
            else if (hue < 180) { rr = 0; gg = c; bb = x2; }
            else if (hue < 240) { rr = 0; gg = x2; bb = c; }
            else if (hue < 300) { rr = x2; gg = 0; bb = c; }
            else { rr = c; gg = 0; bb = x2; }
            shapeRenderer.setColor(rr + m, gg + m, bb + m, 0.3f);
            float r = Math.max(5f, decoBallR[i] * Math.min(screenWidth, screenHeight));
            shapeRenderer.circle(decoBallX[i] * screenWidth, decoBallY[i] * screenHeight, r);
        }

        // 画标题区域（大圆 + 装饰）
        float titleY = screenHeight * 0.82f;
        shapeRenderer.setColor(TITLE_COLOR);
        shapeRenderer.circle(screenWidth * 0.5f, titleY, 30f);
        shapeRenderer.setColor(1f, 0.8f, 0.2f, 0.6f);
        shapeRenderer.circle(screenWidth * 0.5f, titleY, 35f);

        // 画按钮
        for (int i = 0; i < BUTTON_COUNT; i++) {
            // 按钮阴影
            shapeRenderer.setColor(0f, 0f, 0f, 0.3f);
            shapeRenderer.rect(buttonX[i] + 3f, buttonY[i] - 3f, buttonWidth, buttonHeight);

            // 按钮本体
            shapeRenderer.setColor(BUTTON_COLORS[i]);
            shapeRenderer.rect(buttonX[i], buttonY[i], buttonWidth, buttonHeight);

            // 按钮高光
            shapeRenderer.setColor(1f, 1f, 1f, 0.15f);
            shapeRenderer.rect(buttonX[i], buttonY[i] + buttonHeight * 0.6f, buttonWidth, buttonHeight * 0.4f);

            // 按钮图标（不同形状区分）
            float iconCX = buttonX[i] + 30f;
            float iconCY = buttonY[i] + buttonHeight * 0.5f;
            shapeRenderer.setColor(1f, 1f, 1f, 0.8f);
            switch (i) {
                case 0: // 自由模式 - 圆形
                    shapeRenderer.circle(iconCX, iconCY, 10f);
                    break;
                case 1: // 团队模式 - 两个小圆
                    shapeRenderer.circle(iconCX - 6f, iconCY, 7f);
                    shapeRenderer.circle(iconCX + 6f, iconCY, 7f);
                    break;
                case 2: // 生存模式 - 三角形（用矩形模拟）
                    shapeRenderer.rect(iconCX - 8f, iconCY - 2f, 16f, 4f);
                    shapeRenderer.rect(iconCX - 2f, iconCY - 8f, 4f, 16f);
                    break;
                case 3: // 排行榜 - 方块堆叠
                    shapeRenderer.rect(iconCX - 8f, iconCY - 8f, 7f, 7f);
                    shapeRenderer.rect(iconCX + 1f, iconCY - 8f, 7f, 7f);
                    shapeRenderer.rect(iconCX - 4f, iconCY + 1f, 7f, 7f);
                    break;
            }
        }

        shapeRenderer.end();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pointer != 0 || !initialized) return false;

        // 注意：LibGDX touchDown的screenY是从上往下的，但我们的buttonY也是从上往下的
        // 实际上LibGDX的touch坐标：Y=0在底部，但我们的布局Y是从顶部开始的
        // 需要转换
        float touchYConverted = screenHeight - screenY;

        for (int i = 0; i < BUTTON_COUNT; i++) {
            if (screenX >= buttonX[i] && screenX <= buttonX[i] + buttonWidth
                    && touchYConverted >= buttonY[i] && touchYConverted <= buttonY[i] + buttonHeight) {
                handleButtonClick(i);
                return true;
            }
        }
        return true;
    }

    private void handleButtonClick(int index) {
        switch (index) {
            case 0: // 自由模式
                game.setScreen(new GameScreen(game, 0));
                break;
            case 1: // 团队模式（暂用自由模式代替）
                game.setScreen(new GameScreen(game, 1));
                break;
            case 2: // 生存模式
                game.setScreen(new GameScreen(game, 2));
                break;
            case 3: // 排行榜
                game.setScreen(new LeaderboardScreen(game));
                break;
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
        if (keycode == com.badlogic.gdx.Input.Keys.ENTER) {
            game.setScreen(new GameScreen(game, 0));
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
