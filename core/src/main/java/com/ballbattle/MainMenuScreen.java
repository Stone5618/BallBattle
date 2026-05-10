package com.ballbattle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class MainMenuScreen implements Screen, InputProcessor {

    private BallBattleGame game;
    private OrthographicCamera camera;
    private GlyphLayout glyphLayout;

    private Array<MenuButton> buttons;
    private boolean initialized = false;

    public MainMenuScreen(BallBattleGame game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.glyphLayout = new GlyphLayout();
        this.buttons = new Array<>();
    }

    private void initLayout() {
        if (initialized) return;
        
        float screenWidth = Math.max(320, Gdx.graphics.getWidth());
        float screenHeight = Math.max(480, Gdx.graphics.getHeight());
        
        camera.setToOrtho(false, screenWidth, screenHeight);
        
        float titleY = screenHeight * 0.78f;
        float buttonStartY = screenHeight * 0.55f;
        float buttonSpacing = screenHeight * 0.1f;
        float buttonWidth = screenWidth * 0.5f;
        float buttonHeight = screenHeight * 0.07f;
        float buttonX = (screenWidth - buttonWidth) / 2f;

        buttons.clear();
        buttons.add(new MenuButton("自由模式", buttonX, buttonStartY, buttonWidth, buttonHeight,
                new Color(0.2f, 0.7f, 0.2f, 1f), BallBattleGame.MODE_FFA));
        buttons.add(new MenuButton("团队模式", buttonX, buttonStartY - buttonSpacing, buttonWidth, buttonHeight,
                new Color(0.2f, 0.4f, 0.8f, 1f), BallBattleGame.MODE_TEAMS));
        buttons.add(new MenuButton("生存模式", buttonX, buttonStartY - buttonSpacing * 2, buttonWidth, buttonHeight,
                new Color(0.8f, 0.3f, 0.2f, 1f), BallBattleGame.MODE_SURVIVAL));
        buttons.add(new MenuButton("排行榜", buttonX, buttonStartY - buttonSpacing * 3, buttonWidth, buttonHeight,
                new Color(0.6f, 0.5f, 0.2f, 1f), -1));

        initialized = true;
    }

    @Override
    public void render(float delta) {
        initLayout();
        
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        // 绘制按钮
        game.shapeRenderer.setProjectionMatrix(camera.combined);
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (MenuButton btn : buttons) {
            game.shapeRenderer.setColor(btn.color);
            game.shapeRenderer.rect(btn.x, btn.y, btn.width, btn.height);
        }
        game.shapeRenderer.end();

        // 绘制文字
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        
        // 标题
        String title = "球球大作战";
        glyphLayout.setText(game.fontLarge, title);
        game.fontLarge.draw(game.batch, title, 
            (camera.viewportWidth - glyphLayout.width) / 2, 
            camera.viewportHeight * 0.85f);
        
        // 按钮文字
        for (MenuButton btn : buttons) {
            glyphLayout.setText(game.font, btn.text);
            game.font.draw(game.batch, btn.text,
                btn.x + (btn.width - glyphLayout.width) / 2,
                btn.y + btn.height * 0.6f + glyphLayout.height / 2);
        }
        
        game.batch.end();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void resize(int width, int height) {
        initialized = false;
        initLayout();
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

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float touchY = (int) camera.viewportHeight - screenY;
        
        for (MenuButton btn : buttons) {
            if (screenX >= btn.x && screenX <= btn.x + btn.width &&
                touchY >= btn.y && touchY <= btn.y + btn.height) {
                
                if (btn.mode == -1) {
                    game.setScreen(new LeaderboardScreen(game));
                } else {
                    game.setScreen(new GameScreen(game, btn.mode));
                }
                return true;
            }
        }
        return false;
    }

    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean keyDown(int keycode) { return false; }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }

    private static class MenuButton {
        String text;
        float x, y, width, height;
        Color color;
        int mode;

        MenuButton(String text, float x, float y, float width, float height, Color color, int mode) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
            this.mode = mode;
        }
    }
}
