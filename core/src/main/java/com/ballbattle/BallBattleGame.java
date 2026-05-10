package com.ballbattle;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

/**
 * 球球大作战 - 主游戏入口
 */
public class BallBattleGame extends Game {

    @Override
    public void create() {
        Gdx.app.log("BallBattle", "create() started");
        // 直接进入主菜单
        setScreen(new MainMenuScreen(this));
        Gdx.app.log("BallBattle", "create() success - MainMenuScreen set");
    }

    @Override
    public void render() {
        // 清屏（各Screen会自行绘制）
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 渲染当前Screen
        if (screen != null) {
            screen.render(Gdx.graphics.getDeltaTime());
        }
    }

    @Override
    public void dispose() {
        if (screen != null) {
            screen.dispose();
        }
    }
}
