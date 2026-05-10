package com.ballbattle;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * 主游戏类 - LibGDX游戏入口
 * 管理全局资源和屏幕切换
 */
public class BallBattleGame extends Game {

    public SpriteBatch batch;
    public ShapeRenderer shapeRenderer;
    public BitmapFont font;
    public BitmapFont fontLarge;

    /** 世界尺寸 */
    public static final int WORLD_WIDTH = 5000;
    public static final int WORLD_HEIGHT = 5000;

    /** 游戏模式常量 */
    public static final int MODE_FFA = 0;       // 自由模式
    public static final int MODE_TEAMS = 1;     // 团队模式
    public static final int MODE_SURVIVAL = 2;  // 生存模式

    /** 皮肤颜色（十六进制） */
    public static final String[] SKIN_COLORS = {
        "FF4444", "44FF44", "4444FF", "FFFF44", "FF44FF", "44FFFF",
        "FF8800", "8800FF", "00FF88", "FF0088", "0088FF", "88FF00",
        "FF6666", "66FF66", "6666FF", "FFAA00", "AA00FF", "00FFAA"
    };

    /** AI名字 */
    public static final String[] AI_NAMES = {
        "小明", "大白", "球王", "吃豆人", "滚球兽", "泡泡龙",
        "圆圆", "豆豆", "球球", "滚滚", "弹弹", "蹦蹦",
        "吃货", "萌萌", "嘟嘟", "咕咕", "胖虎", "小夫"
    };

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // 使用默认BitmapFont（不依赖外部字体文件）
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        fontLarge = new BitmapFont();
        fontLarge.setColor(Color.WHITE);
        fontLarge.getData().scale(2f);

        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        fontLarge.dispose();
    }

    @Override
    public void render() {
        super.render();
    }
}
