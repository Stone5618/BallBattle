package com.ballbattle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * 像素数字绘制器 - 用小方块绘制数字（不使用BitmapFont）
 */
public class PixelDrawer {

    // 3x5 像素字体定义 (0-9)
    // 每个数字用3列x5行的boolean数组表示
    private static final boolean[][][] DIGITS = {
            // 0
            {
                    {true, true, true},
                    {true, false, true},
                    {true, false, true},
                    {true, false, true},
                    {true, true, true}
            },
            // 1
            {
                    {false, true, false},
                    {true, true, false},
                    {false, true, false},
                    {false, true, false},
                    {true, true, true}
            },
            // 2
            {
                    {true, true, true},
                    {false, false, true},
                    {true, true, true},
                    {true, false, false},
                    {true, true, true}
            },
            // 3
            {
                    {true, true, true},
                    {false, false, true},
                    {true, true, true},
                    {false, false, true},
                    {true, true, true}
            },
            // 4
            {
                    {true, false, true},
                    {true, false, true},
                    {true, true, true},
                    {false, false, true},
                    {false, false, true}
            },
            // 5
            {
                    {true, true, true},
                    {true, false, false},
                    {true, true, true},
                    {false, false, true},
                    {true, true, true}
            },
            // 6
            {
                    {true, true, true},
                    {true, false, false},
                    {true, true, true},
                    {true, false, true},
                    {true, true, true}
            },
            // 7
            {
                    {true, true, true},
                    {false, false, true},
                    {false, true, false},
                    {false, true, false},
                    {false, true, false}
            },
            // 8
            {
                    {true, true, true},
                    {true, false, true},
                    {true, true, true},
                    {true, false, true},
                    {true, true, true}
            },
            // 9
            {
                    {true, true, true},
                    {true, false, true},
                    {true, true, true},
                    {false, false, true},
                    {true, true, true}
            }
    };

    /**
     * 绘制一个数字
     */
    public static void drawDigit(ShapeRenderer renderer, int digit, float x, float y, float pixelSize, Color color) {
        if (digit < 0 || digit > 9) return;
        if (renderer == null) return;

        boolean[][] d = DIGITS[digit];
        if (d == null) return;

        Color oldColor = renderer.getColor();
        renderer.setColor(color);

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 3; col++) {
                if (row < d.length && col < d[row].length && d[row][col]) {
                    renderer.rect(
                            x + col * pixelSize,
                            y - (row + 1) * pixelSize,
                            pixelSize * 0.9f,
                            pixelSize * 0.9f
                    );
                }
            }
        }

        renderer.setColor(oldColor);
    }

    /**
     * 绘制一个整数
     */
    public static void drawNumber(ShapeRenderer renderer, int number, float x, float y, float pixelSize, Color color) {
        if (renderer == null) return;

        String s = String.valueOf(Math.abs(number));
        int numDigits = s.length();
        float totalWidth = numDigits * 4 * pixelSize - pixelSize; // 3 pixels + 1 spacing per digit
        float startX = x - totalWidth * 0.5f;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int digit = c - '0';
            if (digit >= 0 && digit <= 9) {
                drawDigit(renderer, digit, startX + i * 4 * pixelSize, y, pixelSize, color);
            }
        }

        // 负号
        if (number < 0) {
            Color oldColor = renderer.getColor();
            renderer.setColor(color);
            renderer.rect(startX - 3 * pixelSize, y - 3 * pixelSize, 2 * pixelSize, pixelSize * 0.9f);
            renderer.setColor(oldColor);
        }
    }

    /**
     * 绘制一个整数（左对齐）
     */
    public static void drawNumberLeft(ShapeRenderer renderer, int number, float x, float y, float pixelSize, Color color) {
        if (renderer == null) return;

        String s = String.valueOf(Math.abs(number));
        float startX = x;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int digit = c - '0';
            if (digit >= 0 && digit <= 9) {
                drawDigit(renderer, digit, startX + i * 4 * pixelSize, y, pixelSize, color);
            }
        }
    }

    /**
     * 获取数字的宽度
     */
    public static float getNumberWidth(int number, float pixelSize) {
        String s = String.valueOf(Math.abs(number));
        return s.length() * 4 * pixelSize - pixelSize;
    }
}
