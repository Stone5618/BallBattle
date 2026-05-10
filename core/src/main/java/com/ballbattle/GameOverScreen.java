package com.ballbattle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * 游戏结束界面 - 显示本局得分、最大体积、存活时间
 */
public class GameOverScreen implements Screen {

    private BallBattleGame game;
    private OrthographicCamera camera;
    private GlyphLayout glyphLayout;

    private int score;
    private int maxRadius;
    private float survivalTime;
    private int gameMode;

    // 按钮区域
    private float restartBtnX, restartBtnY, restartBtnW, restartBtnH;
    private float menuBtnX, menuBtnY, menuBtnW, menuBtnH;

    // 动画
    private float fadeTimer;
    private static final float FADE_DURATION = 0.5f;

    public GameOverScreen(BallBattleGame game, int score, int maxRadius, float survivalTime, int gameMode) {
        this.game = game;
        this.score = score;
        this.maxRadius = maxRadius;
        this.survivalTime = survivalTime;
        this.gameMode = gameMode;
        this.fadeTimer = 0;
        this.glyphLayout = new GlyphLayout();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // 计算按钮布局
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        float btnWidth = sw * 0.45f;
        float btnHeight = sh * 0.07f;
        float centerX = sw / 2f;

        restartBtnW = btnWidth;
        restartBtnH = btnHeight;
        restartBtnX = centerX - btnWidth / 2f;
        restartBtnY = sh * 0.25f;

        menuBtnW = btnWidth;
        menuBtnH = btnHeight;
        menuBtnX = centerX - btnWidth / 2f;
        menuBtnY = sh * 0.15f;
    }

    @Override
    public void render(float delta) {
        fadeTimer += delta;
        float alpha = Math.min(1f, fadeTimer / FADE_DURATION);

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        float centerX = sw / 2f;

        // 背景装饰
        game.shapeRenderer.setProjectionMatrix(camera.combined);
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(0.1f, 0.1f, 0.2f, 0.5f * alpha);
        game.shapeRenderer.circle(centerX, sh * 0.55f, 200);
        game.shapeRenderer.end();

        // 绘制按钮
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 重新开始按钮
        game.shapeRenderer.setColor(0.2f, 0.7f, 0.2f, alpha);
        game.shapeRenderer.rect(restartBtnX, restartBtnY, restartBtnW, restartBtnH);
        // 高光
        game.shapeRenderer.setColor(1, 1, 1, 0.15f * alpha);
        game.shapeRenderer.rect(restartBtnX, restartBtnY + restartBtnH * 0.6f, restartBtnW, restartBtnH * 0.35f);

        // 返回菜单按钮
        game.shapeRenderer.setColor(0.3f, 0.3f, 0.7f, alpha);
        game.shapeRenderer.rect(menuBtnX, menuBtnY, menuBtnW, menuBtnH);
        game.shapeRenderer.setColor(1, 1, 1, 0.15f * alpha);
        game.shapeRenderer.rect(menuBtnX, menuBtnY + menuBtnH * 0.6f, menuBtnW, menuBtnH * 0.35f);

        game.shapeRenderer.end();

        // 绘制文字
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // 标题
        game.fontLarge.setColor(1, 1, 1, alpha);
        String title = "游戏结束";
        glyphLayout.setText(game.fontLarge, title);
        game.fontLarge.draw(game.batch, title, centerX - glyphLayout.width / 2f, sh * 0.82f);

        // 分割线
        game.shapeRenderer.setProjectionMatrix(camera.combined);
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(1, 1, 1, 0.3f * alpha);
        game.shapeRenderer.rect(centerX - 120, sh * 0.77f, 240, 2);
        game.shapeRenderer.end();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // 模式名称
        String modeName;
        switch (gameMode) {
            case BallBattleGame.MODE_FFA: modeName = "自由模式"; break;
            case BallBattleGame.MODE_TEAMS: modeName = "团队模式"; break;
            case BallBattleGame.MODE_SURVIVAL: modeName = "生存模式"; break;
            default: modeName = "";
        }
        game.font.setColor(Color.YELLOW);
        game.font.setColor(game.font.getColor().r, game.font.getColor().g, game.font.getColor().b, alpha);
        glyphLayout.setText(game.font, modeName);
        game.font.draw(game.batch, modeName, centerX - glyphLayout.width / 2f, sh * 0.72f);

        // 统计信息
        float infoY = sh * 0.62f;
        float infoSpacing = sh * 0.06f;

        game.font.setColor(1, 1, 1, alpha);
        drawStatLine(centerX, infoY, "得分", String.valueOf(score), alpha);
        drawStatLine(centerX, infoY - infoSpacing, "最大体积", String.valueOf(maxRadius), alpha);

        int minutes = (int) (survivalTime / 60);
        int seconds = (int) (survivalTime % 60);
        drawStatLine(centerX, infoY - infoSpacing * 2, "存活时间",
                String.format("%d分%02d秒", minutes, seconds), alpha);

        // 按钮文字
        game.font.setColor(1, 1, 1, alpha);
        String restartText = "重新开始";
        glyphLayout.setText(game.font, restartText);
        game.font.draw(game.batch, restartText,
                restartBtnX + (restartBtnW - glyphLayout.width) / 2f,
                restartBtnY + (restartBtnH + glyphLayout.height) / 2f);

        String menuText = "返回主菜单";
        glyphLayout.setText(game.font, menuText);
        game.font.draw(game.batch, menuText,
                menuBtnX + (menuBtnW - glyphLayout.width) / 2f,
                menuBtnY + (menuBtnH + glyphLayout.height) / 2f);

        game.batch.end();
    }

    private void drawStatLine(float centerX, float y, String label, String value, float alpha) {
        game.font.setColor(0.7f, 0.7f, 0.7f, alpha);
        String labelText = label + ": ";
        glyphLayout.setText(game.font, labelText);
        game.font.draw(game.batch, labelText, centerX - 80, y);

        game.font.setColor(Color.WHITE);
        game.font.setColor(game.font.getColor().r, game.font.getColor().g, game.font.getColor().b, alpha);
        game.font.draw(game.batch, value, centerX - 80 + glyphLayout.width, y);
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                GameOverScreen.this.touchDown(screenX, screenY, pointer, button);
                return true;
            }
        });
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

    // 触摸处理（由 InputAdapter 调用）
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float touchY = Gdx.graphics.getHeight() - screenY;

        // 检查重新开始按钮
        if (screenX >= restartBtnX && screenX <= restartBtnX + restartBtnW &&
            touchY >= restartBtnY && touchY <= restartBtnY + restartBtnH) {
            game.setScreen(new GameScreen(game, gameMode));
            return true;
        }

        // 检查返回菜单按钮
        if (screenX >= menuBtnX && screenX <= menuBtnX + menuBtnW &&
            touchY >= menuBtnY && touchY <= menuBtnY + menuBtnH) {
            game.setScreen(new MainMenuScreen(game));
            return true;
        }

        return false;
    }
}
