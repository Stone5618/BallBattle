package com.ballbattle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.Preferences;

/**
 * 排行榜界面 - 显示本地存储的历史最高分
 */
public class LeaderboardScreen implements Screen {

    private BallBattleGame game;
    private OrthographicCamera camera;
    private GlyphLayout glyphLayout;
    private Preferences prefs;

    // 返回按钮
    private float backBtnX, backBtnY, backBtnW, backBtnH;

    // 清除按钮
    private float clearBtnX, clearBtnY, clearBtnW, clearBtnH;

    // 动画
    private float fadeTimer;

    public LeaderboardScreen(BallBattleGame game) {
        this.game = game;
        this.prefs = Gdx.app.getPreferences("ballbattle_scores");
        this.fadeTimer = 0;
        this.glyphLayout = new GlyphLayout();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // 计算按钮布局
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        float btnWidth = sw * 0.4f;
        float btnHeight = sh * 0.06f;
        float centerX = sw / 2f;

        backBtnW = btnWidth;
        backBtnH = btnHeight;
        backBtnX = centerX - btnWidth / 2f;
        backBtnY = sh * 0.08f;

        clearBtnW = btnWidth * 0.6f;
        clearBtnH = btnHeight * 0.8f;
        clearBtnX = centerX - clearBtnW / 2f;
        clearBtnY = sh * 0.17f;
    }

    @Override
    public void render(float delta) {
        fadeTimer += delta;
        float alpha = Math.min(1f, fadeTimer / 0.3f);

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        float centerX = sw / 2f;

        // 绘制背景面板
        game.shapeRenderer.setProjectionMatrix(camera.combined);
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(0.12f, 0.12f, 0.18f, 0.9f * alpha);
        float panelX = sw * 0.1f;
        float panelY = sh * 0.25f;
        float panelW = sw * 0.8f;
        float panelH = sh * 0.65f;
        game.shapeRenderer.rect(panelX, panelY, panelW, panelH);
        game.shapeRenderer.end();

        // 绘制返回按钮
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(0.3f, 0.3f, 0.7f, alpha);
        game.shapeRenderer.rect(backBtnX, backBtnY, backBtnW, backBtnH);
        game.shapeRenderer.setColor(1, 1, 1, 0.15f * alpha);
        game.shapeRenderer.rect(backBtnX, backBtnY + backBtnH * 0.6f, backBtnW, backBtnH * 0.35f);

        // 清除按钮
        game.shapeRenderer.setColor(0.6f, 0.2f, 0.2f, alpha);
        game.shapeRenderer.rect(clearBtnX, clearBtnY, clearBtnW, clearBtnH);
        game.shapeRenderer.end();

        // 绘制文字
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // 标题
        game.fontLarge.setColor(1, 1, 1, alpha);
        String title = "排行榜";
        glyphLayout.setText(game.fontLarge, title);
        game.fontLarge.draw(game.batch, title, centerX - glyphLayout.width / 2f, sh * 0.87f);

        // 表头
        float headerY = panelY + panelH - 40;
        game.font.setColor(Color.YELLOW);
        game.font.setColor(game.font.getColor().r, game.font.getColor().g, game.font.getColor().b, alpha);
        game.font.draw(game.batch, "排名", panelX + 20, headerY);
        game.font.draw(game.batch, "模式", panelX + 80, headerY);
        game.font.draw(game.batch, "得分", panelX + 180, headerY);
        game.font.draw(game.batch, "最大体积", panelX + 280, headerY);
        game.font.draw(game.batch, "存活时间", panelX + 400, headerY);

        // 分割线
        game.shapeRenderer.setProjectionMatrix(camera.combined);
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(1, 1, 1, 0.3f * alpha);
        game.shapeRenderer.rect(panelX + 10, headerY - 8, panelW - 20, 2);
        game.shapeRenderer.end();

        // 分数列表
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        int count = prefs.getInteger("score_count", 0);
        int displayCount = Math.min(count, 10);

        if (displayCount == 0) {
            game.font.setColor(Color.GRAY);
            game.font.setColor(game.font.getColor().r, game.font.getColor().g, game.font.getColor().b, alpha);
            String noData = "暂无记录";
            glyphLayout.setText(game.font, noData);
            game.font.draw(game.batch, noData, centerX - glyphLayout.width / 2f, panelY + panelH / 2f);
        }

        for (int i = 0; i < displayCount; i++) {
            int s = prefs.getInteger("score_" + i, 0);
            int mr = prefs.getInteger("max_radius_" + i, 0);
            float st = prefs.getFloat("survival_time_" + i, 0);
            int mode = prefs.getInteger("mode_" + i, 0);

            float rowY = headerY - 35 - i * 35;

            // 排名颜色
            if (i == 0) {
                game.font.setColor(Color.GOLD);
            } else if (i == 1) {
                game.font.setColor(Color.LIGHT_GRAY);
            } else if (i == 2) {
                game.font.setColor(Color.ORANGE);
            } else {
                game.font.setColor(Color.WHITE);
            }
            game.font.setColor(game.font.getColor().r, game.font.getColor().g, game.font.getColor().b, alpha);

            // 排名
            String rank;
            switch (i) {
                case 0: rank = "1st"; break;
                case 1: rank = "2nd"; break;
                case 2: rank = "3rd"; break;
                default: rank = (i + 1) + "th"; break;
            }
            game.font.draw(game.batch, rank, panelX + 20, rowY);

            // 模式
            String modeStr;
            switch (mode) {
                case BallBattleGame.MODE_FFA: modeStr = "自由"; break;
                case BallBattleGame.MODE_TEAMS: modeStr = "团队"; break;
                case BallBattleGame.MODE_SURVIVAL: modeStr = "生存"; break;
                default: modeStr = "未知"; break;
            }
            game.font.draw(game.batch, modeStr, panelX + 80, rowY);

            // 得分
            game.font.draw(game.batch, String.valueOf(s), panelX + 180, rowY);

            // 最大体积
            game.font.draw(game.batch, String.valueOf(mr), panelX + 280, rowY);

            // 存活时间
            int mins = (int) (st / 60);
            int secs = (int) (st % 60);
            game.font.draw(game.batch, String.format("%d:%02d", mins, secs), panelX + 400, rowY);
        }

        // 按钮文字
        game.font.setColor(1, 1, 1, alpha);
        String backText = "返回主菜单";
        glyphLayout.setText(game.font, backText);
        game.font.draw(game.batch, backText,
                backBtnX + (backBtnW - glyphLayout.width) / 2f,
                backBtnY + (backBtnH + glyphLayout.height) / 2f);

        String clearText = "清除记录";
        glyphLayout.setText(game.font, clearText);
        game.font.draw(game.batch, clearText,
                clearBtnX + (clearBtnW - glyphLayout.width) / 2f,
                clearBtnY + (clearBtnH + glyphLayout.height) / 2f);

        game.batch.end();
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
                LeaderboardScreen.this.touchDown(screenX, screenY, pointer, button);
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

        // 返回按钮
        if (screenX >= backBtnX && screenX <= backBtnX + backBtnW &&
            touchY >= backBtnY && touchY <= backBtnY + backBtnH) {
            game.setScreen(new MainMenuScreen(game));
            return true;
        }

        // 清除按钮
        if (screenX >= clearBtnX && screenX <= clearBtnX + clearBtnW &&
            touchY >= clearBtnY && touchY <= clearBtnY + clearBtnH) {
            prefs.clear();
            prefs.flush();
            return true;
        }

        return false;
    }
}
