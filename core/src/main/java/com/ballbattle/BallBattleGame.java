package com.ballbattle;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.InputAdapter;

/**
 * 超极简版 - 只画一个能跟随手指移动的球
 * 验证 LibGDX 在 Android 上是否能正常运行
 */
public class BallBattleGame extends Game {
    
    private ShapeRenderer shapeRenderer;
    private float ballX = 400;
    private float ballY = 300;
    private float ballRadius = 50;
    
    @Override
    public void create() {
        Gdx.app.log("BallBattle", "Game created successfully!");
        
        shapeRenderer = new ShapeRenderer();
        
        // 设置输入处理器 - 触摸移动球
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                // 转换Y坐标（LibGDX原点在左下角，Android在左上角）
                ballX = screenX;
                ballY = Gdx.graphics.getHeight() - screenY;
                return true;
            }
            
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                ballX = screenX;
                ballY = Gdx.graphics.getHeight() - screenY;
                return true;
            }
        });
    }
    
    @Override
    public void render() {
        // 清屏为深灰色
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // 画一个绿色的球
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.8f, 0.3f, 1f);
        shapeRenderer.circle(ballX, ballY, ballRadius);
        shapeRenderer.end();
    }
    
    @Override
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
