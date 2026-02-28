package io.github.dragonplatformer;

import com.badlogic.gdx.math.Vector2;

public class CameraArea {
    private final float x1;
    private final float x2;
    private final float y1;
    private final float y2;

    public CameraArea(float x, float y, float width, float height) {
        x1 = x;
        x2 = x + width;
        y1 = y;
        y2 = y + height;
    }

    public boolean contains(Vector2 pos) {
        return pos.x > x1 && pos.x < x2 && pos.y > y1 && pos.y < y2;
    }

    public float getWidth() {
        return x2 - x1;
    }

    public float getHeight() {
        return y2 - y1;
    }

    public float getX() {
        return (x1 + x2) / 2;
    }

    public float getY() {
        return (y1 + y2) / 2;
    }
}
