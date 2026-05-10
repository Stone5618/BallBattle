package com.ballbattle;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

public class BallBattleGame extends Game {
    private static final String TAG = "BallBattleGame";

    public SpriteBatch batch;
    public ShapeRenderer shapeRenderer;
    public BitmapFont font;
    public BitmapFont fontLarge;

    public static final int WORLD_WIDTH = 2000;
    public static final int WORLD_HEIGHT = 2000;

    public static final int MODE_FFA = 0;
    public static final int MODE_TEAMS = 1;
    public static final int MODE_SURVIVAL = 2;

    public static final String[] SKIN_COLORS = {
        "FF4444", "44FF44", "4444FF", "FFFF44", "FF44FF", "44FFFF",
        "FF8800", "8800FF", "00FF88", "FF0088", "0088FF", "88FF00"
    };

    public static final String[] AI_NAMES = {
        "小明", "大白", "球王", "豆豆", "球球", "滚滚"
    };

    @Override
    public void create() {
        try {
            Gdx.app.log(TAG, "create() started");
            
            batch = new SpriteBatch();
            shapeRenderer = new ShapeRenderer();

            font = createSimpleFont(16, Color.WHITE);
            fontLarge = createSimpleFont(24, Color.WHITE);

            Gdx.app.log(TAG, "Setting MainMenuScreen...");
            setScreen(new MainMenuScreen(this));
            
            Gdx.app.log(TAG, "create() completed");
        } catch (Exception e) {
            Gdx.app.error(TAG, "Error in create()", e);
            throw new RuntimeException("Failed to initialize game", e);
        }
    }

    private BitmapFont createSimpleFont(int size, Color color) {
        Pixmap pixmap = null;
        try {
            int cellWidth = size;
            int cellHeight = size + 4;
            int cols = 16;
            int rows = 6;
            
            pixmap = new Pixmap(cellWidth * cols, cellHeight * rows, Pixmap.Format.RGBA8888);
            pixmap.setColor(0, 0, 0, 0);
            pixmap.fill();
            
            pixmap.setColor(color);
            
            for (int i = 0; i < 96; i++) {
                int x = (i % cols) * cellWidth;
                int y = (i / cols) * cellHeight;
                pixmap.drawRectangle(x + 1, y + 1, cellWidth - 2, cellHeight - 2);
                pixmap.fillRectangle(x + 3, y + 3, cellWidth - 6, cellHeight - 6);
            }
            
            Texture texture = new Texture(pixmap);
            TextureRegion region = new TextureRegion(texture);
            Array<TextureRegion> regions = new Array<TextureRegion>();
            regions.add(region);
            
            BitmapFont font = new BitmapFont(
                new BitmapFont.BitmapFontData(), 
                regions, 
                false
            );
            font.setColor(color);
            return font;
        } finally {
            if (pixmap != null) {
                pixmap.dispose();
            }
        }
    }

    @Override
    public void dispose() {
        Gdx.app.log(TAG, "dispose()");
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
        if (fontLarge != null) fontLarge.dispose();
    }

    @Override
    public void render() {
        super.render();
    }
}
