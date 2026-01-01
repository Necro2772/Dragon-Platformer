package io.github.dragonplatformer.Entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public abstract class Entity {
    private final Body body;
    private final float width;
    private final float height;
    private int direction;

    public Entity(float x, float y, float width, float height, World world) {
        this.height = height;
        this.width = width;
        setDirection(1);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;
        bodyDef.position.set(x, y);
        body = world.createBody(bodyDef);
        getBody().setUserData(this);
    }

    public abstract void act(float delta);

    public abstract void draw(SpriteBatch batch, float delta);

    public abstract void beginContact(Fixture entityFixture, Fixture contactFixture);

    public abstract void endContact(Fixture entityFixture, Fixture contactFixture);

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Body getBody() {
        return body;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }
}
