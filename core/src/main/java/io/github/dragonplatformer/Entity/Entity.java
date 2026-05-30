package io.github.dragonplatformer.Entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.GameContactListener;

import java.util.ArrayList;
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
    private final ArrayList<TimedImpulse> timedImpulses;

    private MovementType movementType;
    private boolean autoMove;
    private boolean isFlying;
    private boolean isFloating;
    private final Vector2 spawnPos;
    private Vector2 targetPos;
    private float speed;
    private float acceleration;
    private final Vector2 damping;
    private final StaticBodyRayCast staticRayCast;

    public Entity(float x, float y, float width, float height, Map<T, Animation<TextureRegion>> anims,
                  Map<T, List<AnimationEvent>> animEvents, AnimationManager animManager, World world) {
        this.animManager = animManager;
        this.anims = anims;
        this.animEvents = animEvents;
        this.height = height;
        this.width = width;
        stateTime = 0;
        timedImpulses = new ArrayList<>();
        setSpriteDirection(1);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;
        bodyDef.position.set(x, y);
        this.body = world.createBody(bodyDef);
        getBody().setUserData(this);

        setMovementType(MovementType.IDLE);
        setAutoMove(true);
        setFlying(false);
        setFloating(false);
        spawnPos = new Vector2(x, y);
        targetPos = new Vector2();
        setSpeed(5);
        setAcceleration(30);
        damping = new Vector2(10, 10);
        staticRayCast = new StaticBodyRayCast();
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
        timedImpulses = new ArrayList<>();

        setMovementType(MovementType.IDLE);
        setAutoMove(true);
        setFlying(false);
        setFloating(false);
        spawnPos = new Vector2(body.getPosition());
        targetPos = new Vector2();
        setSpeed(0);
        setAcceleration(30);
        damping = new Vector2(10, 10);
        staticRayCast = new StaticBodyRayCast();
    }

    public void act(float delta) {
        movementUpdate(delta);
        stateTime += delta;
        for(TimedImpulse timedImpulse : timedImpulses) {
            applyWeightedImpulse(timedImpulse.getImpulse(delta));
            timedImpulse.update(delta);
        }
        timedImpulses.removeIf(TimedImpulse::isExpired);
    }

    private void movementUpdate(float delta) {
        if (isAutoMove()) {
            Vector2 direction = new Vector2();
            switch (getMovementType()) {
                case IDLE:
                    direction.set(spawnPos()).sub(getPosition()).nor();
                    break;
                case FLEE:
                    direction.set(getPosition()).sub(getTargetPos()).nor();
                    float distToWall = 5;
                    staticRayCast.reset();
                    getBody().getWorld().rayCast(staticRayCast, getPosition(), new Vector2(direction).scl(distToWall));
                    if (staticRayCast.result) {
                        int[] closestAngles = new int[3];
                        float diamondAngle;
                        if (direction.y >= 0)
                                diamondAngle = (direction.x >= 0 ? direction.y/(direction.x+direction.y)
                                    : 1-direction.x/(-direction.x+direction.y));
                            else
                                diamondAngle = (direction.x < 0 ? 2-direction.y/(-direction.x-direction.y)
                                    : 3+direction.x/(direction.x-direction.y));
                        closestAngles[0] = Math.round(diamondAngle) % 4;
                        if (diamondAngle < closestAngles[0]) {
                            closestAngles[1] = (closestAngles[0] + 3) % 4;
                            closestAngles[2] = (closestAngles[0] + 1) % 4;
                        } else {
                            closestAngles[1] = (closestAngles[0] + 1) % 4;
                            closestAngles[2] = (closestAngles[0] + 3) % 4;
                        }
                        for (int angle : closestAngles) {
                            staticRayCast.reset();
                            Vector2 rayCastTarget = getPosition();
                            switch (angle) {
                                case 0:
                                    rayCastTarget.add(distToWall, 0);
                                    break;
                                case 1:
                                    rayCastTarget.add(0, distToWall);
                                    break;
                                case 2:
                                    rayCastTarget.add(-distToWall, 0);
                                    break;
                                case 3:
                                    rayCastTarget.add(0, -distToWall);
                                    break;
                            }
                            getBody().getWorld().rayCast(staticRayCast, getPosition(), rayCastTarget);
                            if (!staticRayCast.result) {
                                direction.set(rayCastTarget).sub(getPosition()).scl(1/distToWall);
                                break;
                            }
                        }
                    }
                    break;
                case APPROACH:
                    direction.set(getTargetPos()).sub(getPosition()).nor();
                    break;
                case LINE:
                    direction.set(getTargetPos()).sub(spawnPos()).nor();
                    break;
                case CIRCLE:
                case CAUTION:
                    break;
            }

            if (isFlying()) {
                if (anims.get(getState()).getKeyFrameIndex(getStateTime()) == 0 &&
                    anims.get(getState()).getKeyFrameIndex(getStateTime() + delta) == 1) {
                    float jumpBoost = 4;
                    if (getBody().getLinearVelocity().y < 0 && direction.y >= 0) {
                        applyWeightedImpulse(0, -getBody().getLinearVelocity().y * 2);
                    }

                    applyClampedImpulse(
                        direction.scl(getAcceleration() * anims.get(getState()).getAnimationDuration()),
                        new Vector2(-getSpeed(), 0), new Vector2(getSpeed(), getSpeed() + jumpBoost)
                    );
                }
            } else {
                applyClampedForce(new Vector2(direction).scl(getAcceleration()), new Vector2(-getSpeed(), -getSpeed()),
                    new Vector2(getSpeed(), getSpeed()));
            }
        }

        if (getDamping().x != 0 || getDamping().y != 0) {
            applyWeightedForce(
                (getDamping().x / 100 * -getBody().getLinearVelocity().x * Math.abs(getBody().getLinearVelocity().x)),
                (getDamping().y / 100 * -getBody().getLinearVelocity().y * Math.abs(getBody().getLinearVelocity().y))
            );
        }
    }

    public void draw(SpriteBatch batch, float delta) {
        try {
            TextureRegion frame = anims.get(getState()).getKeyFrame(getStateTime());
            batch.draw(frame,
                getPosition().x - getWidth() / 2f,
                getPosition().y - getHeight() / 2f,
                getWidth() / 2f, getHeight() / 2f, getWidth(), getHeight(), getSpriteDirection(),
                1, 0);
        } catch (NullPointerException e) {
            if (getState() == null) System.err.printf("Null state on object [%s]%n", getClass());
            else System.err.printf("Couldn't find animation for state [%s] of object [%s]%n",
                getState().name(), getClass());
        }
    }

    private static class StaticBodyRayCast implements RayCastCallback {
        public boolean result;
        public Vector2 collisionPoint;

        public StaticBodyRayCast() {
            result = false;
            collisionPoint = new Vector2();
        }

        public void reset() {
            result = false;
        }

        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            short bit = GameContactListener.FilterBits.STATIC.getBit();
            if ((fixture.getFilterData().categoryBits & bit) != 0) {
                result = true;
                collisionPoint.set(point);
                return 0;
            }
            return -1;
        }
    }

    /**
     * Returns the center position of this entity in a new vector.
     * @return position vector
     */
    public Vector2 getPosition() {
        return new Vector2(this.getBody().getPosition());
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
     * Applies a force while factoring in mass of the current object.
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
     * Applies a force while factoring in mass of the current object.
     * @param force acceleration change
     */
    public void applyWeightedForce(Vector2 force) {
        applyWeightedForce(force.x, force.y);
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

    public void applyClampedImpulse(Vector2 impulse, Vector2 minVelocity, Vector2 maxVelocity) {
        Vector2 vel = getBody().getLinearVelocity();
        if (minVelocity == null) minVelocity = new Vector2(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        if (maxVelocity == null) maxVelocity = new Vector2(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        if (impulse.x != 0 &&
            ((vel.x > minVelocity.x && impulse.x < 0) || (vel.x < maxVelocity.x && impulse.x >= 0))) {
            applyWeightedImpulse(Math.min(Math.max(impulse.x, minVelocity.x - vel.x), maxVelocity.x - vel.x), 0);
        }
        if (impulse.y != 0 &&
            ((vel.y > minVelocity.y && impulse.y < 0) || (vel.y < maxVelocity.y && impulse.y >= 0))) {
            applyWeightedImpulse(0, Math.min(Math.max(impulse.y, minVelocity.y - vel.y), maxVelocity.y - vel.y));
        }
    }

    /**
     * Applies a weighted impulse over a duration.
     * @param x velocity change
     * @param y velocity change
     * @param duration time over which to apply impulse
     */
    public void applyTimedImpulse(float x, float y, float duration) {
        applyTimedImpulse(new Vector2(x, y), duration);
    }

    /**
     * Applies a weighted impulse over a duration.
     * @param impulse velocity change
     * @param duration time over which to apply impulse
     */
    public void applyTimedImpulse(Vector2 impulse, float duration) {
        timedImpulses.add(new TimedImpulse(impulse, duration));
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public boolean isAutoMove() {
        return autoMove;
    }

    public void setAutoMove(boolean autoMove) {
        this.autoMove = autoMove;
    }

    public boolean isFlying() {
        return isFlying;
    }

    public void setFlying(boolean flying) {
        isFlying = flying;
    }

    public boolean isFloating() {
        return isFloating;
    }

    public void setFloating(boolean floating) {
        isFloating = floating;
        if (floating) getBody().setGravityScale(0);
    }

    public Vector2 spawnPos() {
        return spawnPos;
    }

    public Vector2 getTargetPos() {
        return targetPos;
    }

    public void setTargetPos(Vector2 target) {
        this.targetPos = target;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }

    public Vector2 getDamping() {
        return damping;
    }

    private static class TimedImpulse {
        public Vector2 impulse;
        public float duration;

        public TimedImpulse(Vector2 impulse, float duration) {
            this.impulse = impulse;
            this.duration = duration;
        }

        public Vector2 getImpulse(float delta) {
            return new Vector2(impulse).scl(Math.min(delta / duration, 1));
        }

        public void update(float delta) {
            impulse.scl((duration - delta) / duration);
            duration -= delta;
        }

        public boolean isExpired() {
            return this.duration <= 0;
        }
    }
}
