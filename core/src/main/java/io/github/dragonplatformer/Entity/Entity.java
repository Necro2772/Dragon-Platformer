package io.github.dragonplatformer.Entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
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
        this.body = world.createBody(bodyDef);
        getBody().setUserData(this);
    }

    public Entity(float width, float height, Body body) {
        this.height = height;
        this.width = width;
        setDirection(1);
        this.body = body;
    }

    public void applyWeightedImpulse(float x, float y) {
        getBody().applyLinearImpulse(
            x * getBody().getMass(),
            y * getBody().getMass(),
            getBody().getPosition().x, getBody().getPosition().y, true);
    }

    public void applyWeightedImpulse(Vector2 vector) {
        applyWeightedImpulse(vector.x, vector.y);
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
