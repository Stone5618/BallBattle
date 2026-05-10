package com.ballbattle;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * 相机控制器 - 跟随玩家并根据大小调整缩放
 */
public class CameraController {

    private com.badlogic.gdx.graphics.OrthographicCamera camera;
    private float targetZoom;
    private float currentZoom;
    private Vector3 targetPosition;
    private Vector3 currentPosition;
    private float lerpSpeed = 5f;

    // 世界尺寸（动态，支持生存模式缩小）
    private float worldWidth;
    private float worldHeight;

    // 缩放范围
    private static final float MIN_ZOOM = 0.05f;
    private static final float MAX_ZOOM = 1.0f;
    private static final float BASE_RADIUS = 30f;

    public CameraController(com.badlogic.gdx.graphics.OrthographicCamera camera) {
        this.camera = camera;
        this.currentZoom = 1.0f;
        this.targetZoom = 1.0f;
        this.targetPosition = new Vector3();
        this.currentPosition = new Vector3();
        this.worldWidth = BallBattleGame.WORLD_WIDTH;
        this.worldHeight = BallBattleGame.WORLD_HEIGHT;
        camera.zoom = currentZoom;
    }

    /**
     * 更新相机位置和缩放
     * @param targetX 玩家X坐标
     * @param targetY 玩家Y坐标
     * @param playerRadius 玩家半径
     * @param delta 时间增量
     */
    public void update(float targetX, float targetY, float playerRadius, float delta) {
        // 根据玩家大小计算目标缩放
        float radiusRatio = playerRadius / BASE_RADIUS;
        targetZoom = 1.0f / (1.0f + (radiusRatio - 1.0f) * 0.15f);
        targetZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, targetZoom));

        // 平滑过渡缩放
        currentZoom += (targetZoom - currentZoom) * lerpSpeed * delta;
        camera.zoom = currentZoom;

        // 设置目标位置
        targetPosition.set(targetX, targetY, 0);

        // 平滑移动
        float lerpFactor = 1.0f - (float) Math.pow(0.01f, delta);
        currentPosition.lerp(targetPosition, lerpFactor);
        camera.position.set(currentPosition);

        // 确保相机不超出世界边界
        clampCameraToWorld();

        camera.update();
    }

    /**
     * 限制相机不超出世界边界
     */
    private void clampCameraToWorld() {
        float viewportWidth = camera.viewportWidth * camera.zoom;
        float viewportHeight = camera.viewportHeight * camera.zoom;

        float halfW = viewportWidth / 2f;
        float halfH = viewportHeight / 2f;

        if (camera.position.x - halfW < 0) {
            camera.position.x = halfW;
        }
        if (camera.position.x + halfW > worldWidth) {
            camera.position.x = worldWidth - halfW;
        }
        if (camera.position.y - halfH < 0) {
            camera.position.y = halfH;
        }
        if (camera.position.y + halfH > worldHeight) {
            camera.position.y = worldHeight - halfH;
        }
    }

    /**
     * 将屏幕坐标转换为世界坐标
     * @param screenX 屏幕X坐标
     * @param screenY 屏幕Y坐标
     * @return 世界坐标
     */
    public Vector2 screenToWorld(float screenX, float screenY) {
        Vector3 worldPos = new Vector3(screenX, screenY, 0);
        camera.unproject(worldPos);
        return new Vector2(worldPos.x, worldPos.y);
    }

    /**
     * 获取当前相机
     * @return OrthographicCamera
     */
    public com.badlogic.gdx.graphics.OrthographicCamera getCamera() {
        return camera;
    }

    /**
     * 获取当前缩放级别
     * @return 缩放值
     */
    public float getCurrentZoom() {
        return currentZoom;
    }

    /**
     * 设置世界尺寸（用于生存模式地图缩小）
     */
    public void setWorldSize(float worldWidth, float worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    /**
     * 立即设置相机位置（不做平滑过渡）
     * @param x X坐标
     * @param y Y坐标
     */
    public void setPositionImmediately(float x, float y) {
        currentPosition.set(x, y, 0);
        targetPosition.set(x, y, 0);
        camera.position.set(x, y, 0);
        camera.update();
    }
}
