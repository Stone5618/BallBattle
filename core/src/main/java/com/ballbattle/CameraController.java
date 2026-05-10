package com.ballbattle;

import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * 相机控制器 - 跟随玩家并根据大小调整缩放
 */
public class CameraController {
    private OrthographicCamera camera;
    private float worldWidth;
    private float worldHeight;
    private float zoomTarget;
    private float currentZoom;

    public CameraController() {
        this.camera = new OrthographicCamera();
        this.worldWidth = Player.WORLD_WIDTH;
        this.worldHeight = Player.WORLD_HEIGHT;
        this.currentZoom = 1f;
        this.zoomTarget = 1f;
    }

    public void update(Player player, float delta) {
        if (player == null || !player.alive) return;

        // 目标位置：玩家中心
        float targetX = player.getCenterX();
        float targetY = player.getCenterY();

        // 平滑跟随
        float lerpFactor = 1f - (float) Math.pow(0.01f, delta);
        camera.position.x += (targetX - camera.position.x) * lerpFactor;
        camera.position.y += (targetY - camera.position.y) * lerpFactor;

        // 根据玩家大小调整缩放
        float totalRadius = player.getTotalRadius();
        float ratio = Math.max(0.01f, totalRadius / Player.INITIAL_RADIUS);
        // 玩家越大，缩放越小（看到更多区域）
        zoomTarget = Math.max(0.15f, 1f / (1f + (ratio - 1f) * 0.3f));

        // 平滑缩放
        currentZoom += (zoomTarget - currentZoom) * lerpFactor * 0.5f;
        camera.zoom = currentZoom;

        // 限制相机不超出世界边界
        float viewWidth = camera.viewportWidth * currentZoom;
        float viewHeight = camera.viewportHeight * currentZoom;

        if (worldWidth > 0 && worldHeight > 0 && viewWidth > 0 && viewHeight > 0) {
            float halfViewW = viewWidth * 0.5f;
            float halfViewH = viewHeight * 0.5f;

            // 如果视图比世界小，限制相机位置
            if (viewWidth < worldWidth) {
                camera.position.x = Math.max(halfViewW, Math.min(worldWidth - halfViewW, camera.position.x));
            } else {
                camera.position.x = worldWidth * 0.5f;
            }
            if (viewHeight < worldHeight) {
                camera.position.y = Math.max(halfViewH, Math.min(worldHeight - halfViewH, camera.position.y));
            } else {
                camera.position.y = worldHeight * 0.5f;
            }
        }

        camera.update();
    }

    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        camera.setToOrtho(false, width, height);
        camera.update();
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public void setWorldSize(float w, float h) {
        this.worldWidth = Math.max(1f, w);
        this.worldHeight = Math.max(1f, h);
    }
}
