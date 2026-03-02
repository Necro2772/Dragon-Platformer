package io.github.dragonplatformer;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class CameraArea {
    private final float x1;
    private final float x2;
    private final float y1;
    private final float y2;

    /**
     * An area which contains the position and size of a viewable zone on the screen
     * @param x horizontal screen position of the lower left corner of the area
     * @param y vertical screen position of the lower left corner of the area
     * @param width width of the area
     * @param height height of the area
     */
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

    public Vector3 getPosition() {
        return new Vector3((x1 + x2) / 2, (y1 + y2) / 2, 0);
    }

    public float getZoom(float viewportWidth, float viewportHeight) {
        return Math.max(getWidth() / viewportWidth, getHeight() / viewportHeight);
    }
}
