package io.github.dragonplatformer.Entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.List;
import java.util.Map;

public abstract class Entity<T extends EntityState> {
    protected final AnimationManager animManager;
    protected final Map<T, Animation<TextureRegion>> anims;
    protected final Map<T, List<AnimationEvent>> animEvents;
    private final Body body;
    private final float width;
    private final float height;
    protected T state;
    protected float stateTime;
    private int sDirection;

    public Entity(float x, float y, float width, float height, Map<T, Animation<TextureRegion>> anims,
                  Map<T, List<AnimationEvent>> animEvents, AnimationManager animManager, World world) {
        this.animManager = animManager;
        this.anims = anims;
        this.animEvents = animEvents;
        this.height = height;
        this.width = width;
        stateTime = 0;
        setSpriteDirection(1);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;
        bodyDef.position.set(x, y);
        this.body = world.createBody(bodyDef);
        getBody().setUserData(this);
    }

    public Entity(float width, float height, Map<T, Animation<TextureRegion>> anims,
                  Map<T, List<AnimationEvent>> animEvents, AnimationManager animManager, Body body) {
        this.animManager = animManager;
        this.anims = anims;
        this.animEvents = animEvents;
        this.height = height;
        this.width = width;
        setSpriteDirection(1);
        this.body = body;
    }

    public void act(float delta) {
        stateTime += delta;
    }

    public void draw(SpriteBatch batch, float delta) {
        try {
            TextureRegion frame = anims.get(getState()).getKeyFrame(getStateTime());
            batch.draw(frame,
                this.getBody().getPosition().x - getWidth() / 2f,
                this.getBody().getPosition().y - getHeight() / 2f,
                getWidth() / 2f, getHeight() / 2f, getWidth(), getHeight(), getSpriteDirection(), 1, 0);
        } catch (NullPointerException e) {
            System.err.printf("Couldn't find animation for state [%s] of object [%s]%n", getState().name(), getClass());
        }
    }

    /**
     * Transitions from current to new state without performing any safety checks.
     * @param state to transition to
     */
    public void setState(T state) {
        endState();
        this.state = state;
        stateTime = 0;
        beginState();
    }

    protected void beginState() { }

    protected void endState() { }

    protected void updateAnimationFlags(float delta) { }

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

    public T getState() {
        return state;
    }

    public float getStateTime() {
        return stateTime;
    }

    public Animation<TextureRegion> getCurrentAnim() {
        return anims.get(getState());
    }

    public void destroy() {
        getBody().getWorld().destroyBody(getBody());
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

    /**
     * Updates movement for a flying creature by sending impulses on frame 1 until velocity is at maxVel.
     * Call from act() every frame while this movement type is active.
     * @param delta time since the last update in seconds.
     * @param impulse amount by which velocity should change. Negative values are ignored and changed to positive.
     * @param maxVel velocity to increase speed towards. Negative values cause impulses to occur in negative directions.
     */
    public void flapMovementUpdate(float delta, Vector2 impulse, Vector2 maxVel) {
        Vector2 vel = getBody().getLinearVelocity();
        if (anims.get(getState()).getKeyFrameIndex(getStateTime()) == 0 &&
            anims.get(getState()).getKeyFrameIndex(getStateTime() + delta) == 1) {
            impulse.x = Math.abs(impulse.x);
            impulse.y = Math.abs(impulse.y);
            if (maxVel.x > 0) {
                if (vel.x < maxVel.x) {
                    applyWeightedImpulse(impulse.x, 0);
                }
            } else if (maxVel.x < 0) {
                if (vel.x > maxVel.x) {
                    applyWeightedImpulse(-impulse.x, 0);
                }
            }
            if (vel.y < 0 && maxVel.y >= 0) {
                applyWeightedImpulse(0, -vel.y * 2);
            }
            if (maxVel.y > 0) {
                if (vel.y < maxVel.y) {
                    applyWeightedImpulse(0, impulse.y);
                }
            } else if (maxVel.y < 0) {
                if (vel.y > maxVel.y) {
                    applyWeightedImpulse(0, -impulse.y);
                }
            }
        }
    }
}
