package io.github.dragonplatformer.Entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public abstract class Entity {
    private final Body body;
    private final float width;
    private final float height;
    private int sDirection;

    public Entity(float x, float y, float width, float height, World world) {
        this.height = height;
        this.width = width;
        setSpriteDirection(1);
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
        setSpriteDirection(1);
        this.body = body;
    }

    /**
     * Applies a force while factoring in mass of the current object, acting as acceleration change.
     * @param x axis acceleration change
     * @param y axis acceleration change
     */
    public void applyWeightedForce(float x, float y) {
        getBody().applyForceToCenter(
            x * getBody().getMass(),
            y * getBody().getMass(),
            true);
    }

    /**
     * Applies a weighted force on each axis if it would not move current velocity past min or max velocity. X and Y
     * axes are tested separately, and null vectors for min or max velocity will ignore those checks.
     * @param acceleration velocity change to apply
     * @param minVelocity minimum velocity on each axis or null if there is no minimum
     * @param maxVelocity maximum velocity on each axis or null if there is no maximum
     */
    public void applyClampedForce(Vector2 acceleration, Vector2 minVelocity, Vector2 maxVelocity) {
        Vector2 vel = getBody().getLinearVelocity();
        if (minVelocity == null) minVelocity = new Vector2(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        if (maxVelocity == null) maxVelocity = new Vector2(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        if (acceleration.x != 0 &&
            ((vel.x > minVelocity.x && acceleration.x < 0) || (vel.x < maxVelocity.x && acceleration.x >= 0))) {
            applyWeightedForce(acceleration.x, 0);
        }
        if (acceleration.y != 0 &&
            ((vel.y > minVelocity.y && acceleration.y < 0) || (vel.y < maxVelocity.y && acceleration.y >= 0))) {
            applyWeightedForce(0, acceleration.y);
        }
    }

    /**
     * Applies an impulse while factoring in mass of the current object, acting as a velocity change.
     * @param x axis velocity change
     * @param y axis velocity change
     */
    public void applyWeightedImpulse(float x, float y) {
        getBody().applyLinearImpulse(
            x * getBody().getMass(),
            y * getBody().getMass(),
            getBody().getPosition().x, getBody().getPosition().y, true);
    }

    /**
     * Applies an impulse while factoring in mass of the current object, acting as a velocity change.
     * @param vector two dimentional velocity change
     */
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

    public int getSpriteDirection() {
        return sDirection;
    }

    public void setSpriteDirection(int sDirection) {
        this.sDirection = sDirection;
    }

    /**
     * Get the current direction of movement of the sprite.
     * @return 1 if current x velocity is positive, -1 if it is negative, and 0 if it is 0
     */
    public int getMoveDirection() {
        if (getBody().getLinearVelocity().x > 0) return 1;
        else if (getBody().getLinearVelocity().x == 0) return 0;
        else return -1;
    }
}
