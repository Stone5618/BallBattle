package com.ballbattle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * 主菜单界面 - 显示游戏标题和模式选择按钮
 */
public class MainMenuScreen implements Screen, InputProcessor {

    private BallBattleGame game;
    private OrthographicCamera camera;
    private GlyphLayout glyphLayout;

    // 按钮定义
    private Array<MenuButton> buttons;
    private float titleY;
    private float subtitleY;
    private float buttonStartY;
    private float buttonSpacing;

    // 装饰球动画
    private Array<DecorBall> decorBalls;
    private float decorTimer;

    public MainMenuScreen(BallBattleGame game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.glyphLayout = new GlyphLayout();
        this.buttons = new Array<>();
        this.decorBalls = new Array<>();
        this.decorTimer = 0;

        // 计算布局
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        titleY = screenHeight * 0.78f;
        subtitleY = screenHeight * 0.70f;
        buttonStartY = screenHeight * 0.55f;
        buttonSpacing = screenHeight * 0.1f;

        float buttonWidth = screenWidth * 0.5f;
        float buttonHeight = screenHeight * 0.07f;
        float buttonX = (screenWidth - buttonWidth) / 2f;

        // 创建按钮
        buttons.add(new MenuButton("自由模式", buttonX, buttonStartY, buttonWidth, buttonHeight,
                new Color(0.2f, 0.7f, 0.2f, 1f), BallBattleGame.MODE_FFA));
        buttons.add(new MenuButton("团队模式", buttonX, buttonStartY - buttonSpacing, buttonWidth, buttonHeight,
                new Color(0.2f, 0.4f, 0.8f, 1f), BallBattleGame.MODE_TEAMS));
        buttons.add(new MenuButton("生存模式", buttonX, buttonStartY - buttonSpacing * 2, buttonWidth, buttonHeight,
                new Color(0.8f, 0.3f, 0.2f, 1f), BallBattleGame.MODE_SURVIVAL));
        buttons.add(new MenuButton("排行榜", buttonX, buttonStartY - buttonSpacing * 3, buttonWidth, buttonHeight,
                new Color(0.6f, 0.5f, 0.2f, 1f), -1));

        // 创建装饰球
        for (int i = 0; i < 20; i++) {
            decorBalls.add(new DecorBall());
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        decorTimer += delta;

        // 更新装饰球
        for (DecorBall db : decorBalls) {
            db.update(delta);
        }

        // 绘制装饰球
        game.shapeRenderer.setProjectionMatrix(camera.combined);
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (DecorBall db : decorBalls) {
            game.shapeRenderer.setColor(db.color);
            game.shapeRenderer.circle(db.x, db.y, db.radius);
        }
        game.shapeRenderer.end();

        // 绘制按钮
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (MenuButton btn : buttons) {
            // 按钮阴影
            game.shapeRenderer.setColor(0, 0, 0, 0.3f);
            game.shapeRenderer.rect(btn.x + 3, btn.y - 3, btn.width, btn.height);

            // 按钮本体
            game.shapeRenderer.setColor(btn.color);
            game.shapeRenderer.rect(btn.x, btn.y, btn.width, btn.height);

            // 按钮高光
            game.shapeRenderer.setColor(1, 1, 1, 0.15f);
            game.shapeRenderer.rect(btn.x, btn.y + btn.height * 0.6f, btn.width, btn.height * 0.35f);
        }
        game.shapeRenderer.end();

        // 绘制文字
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // 标题
        game.fontLarge.setColor(Color.WHITE);
        glyphLayout.setText(game.fontLarge, "球球大作战");
        game.fontLarge.draw(game.batch, "球球大作战",
                (Gdx.graphics.getWidth() - glyphLayout.width) / 2f, titleY);

        // 副标题
        game.font.setColor(Color.LIGHT_GRAY);
        glyphLayout.setText(game.font, "Ball Battle");
        game.font.draw(game.batch, "Ball Battle",
                (Gdx.graphics.getWidth() - glyphLayout.width) / 2f, subtitleY);

        // 按钮文字
        for (MenuButton btn : buttons) {
            game.font.setColor(Color.WHITE);
            glyphLayout.setText(game.font, btn.text);
            game.font.draw(game.batch, btn.text,
                    btn.x + (btn.width - glyphLayout.width) / 2f,
                    btn.y + (btn.height + glyphLayout.height) / 2f);
        }

        // 版本信息
        game.font.setColor(Color.GRAY);
        game.font.draw(game.batch, "v1.0",
                Gdx.graphics.getWidth() - 60, 30);

        game.batch.end();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // 转换为左下角坐标系
        float touchY = Gdx.graphics.getHeight() - screenY;

        for (MenuButton btn : buttons) {
            if (btn.contains(screenX, touchY)) {
                if (btn.gameMode == -1) {
                    // 排行榜
                    game.setScreen(new LeaderboardScreen(game));
                } else {
                    // 开始游戏
                    game.setScreen(new GameScreen(game, btn.gameMode));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public boolean keyDown(int keycode) { return false; }

    @Override
    public boolean keyUp(int keycode) { return false; }

    @Override
    public boolean keyTyped(char character) { return false; }

    @Override
    public boolean mouseMoved(int screenX, int screenY) { return false; }

    @Override
    public boolean scrolled(float amountX, float amountY) { return false; }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
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
    public void dispose() {}

    // ========== 内部类 ==========

    /**
     * 菜单按钮
     */
    private static class MenuButton {
        String text;
        float x, y, width, height;
        Color color;
        int gameMode;

        MenuButton(String text, float x, float y, float width, float height, Color color, int gameMode) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
            this.gameMode = gameMode;
        }

        boolean contains(float px, float py) {
            return px >= x && px <= x + width && py >= y && py <= y + height;
        }
    }

    /**
     * 装饰球
     */
    private static class DecorBall {
        float x, y, radius;
        float vx, vy;
        Color color;
        float alpha;

        DecorBall() {
            float screenWidth = Gdx.graphics.getWidth();
            float screenHeight = Gdx.graphics.getHeight();
            x = MathUtils.random(screenWidth);
            y = MathUtils.random(screenHeight);
            radius = MathUtils.random(10, 40);
            float angle = MathUtils.random(MathUtils.PI2);
            float speed = MathUtils.random(20, 60);
            vx = MathUtils.cos(angle) * speed;
            vy = MathUtils.sin(angle) * speed;
            color = Player.colorFromHex(BallBattleGame.SKIN_COLORS[MathUtils.random(BallBattleGame.SKIN_COLORS.length - 1)]);
            alpha = MathUtils.random(0.1f, 0.3f);
            color.a = alpha;
        }

        void update(float delta) {
            x += vx * delta;
            y += vy * delta;
            float sw = Gdx.graphics.getWidth();
            float sh = Gdx.graphics.getHeight();
            if (x < -radius) x = sw + radius;
            if (x > sw + radius) x = -radius;
            if (y < -radius) y = sh + radius;
            if (y > sh + radius) y = -radius;
        }
    }
}
