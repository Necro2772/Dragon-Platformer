package io.github.dragonplatformer.Entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.GameContactListener;
import io.github.dragonplatformer.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Entity<T extends EntityState> {
    protected final AnimationManager animManager;
    protected final EffectManager effectManager;
    protected final Map<T, AnimationWrapper> anims;
    protected final Map<T, List<AnimationEvent>> animEvents;
    private final Body body;
    private final float width;
    private final float height;
    protected T state;
    protected float stateTime = 0;
    private int sDirection = 1;
    private final ArrayList<TimedImpulse> timedImpulses = new ArrayList<>();

    private MovementType movementType = MovementType.IDLE;
    private boolean autoMove = true;
    private boolean isFlying = false;
    private boolean isFloating = false;
    private final Vector2 spawnPos = new Vector2();
    private Vector2 targetPos = new Vector2();
    private Vector2 targetVel = new Vector2();
    private float speed = 5;
    private float startingSpeed = 5;
    private float acceleration = 30;
    private float turnAcceleration;
    private final Vector2 damping = new Vector2(10, 10);
    private final StaticBodyRayCast staticRayCast = new StaticBodyRayCast();
    private float spawnDelay = 0;
    private boolean enabled = true;
    private float hitStunTimer = 0;
    private final Vector2 bufferedVelocity = new Vector2();
    private boolean visible = true;

    public Entity(float x, float y, float width, float height, Map<T, AnimationWrapper> anims,
                  Map<T, List<AnimationEvent>> animEvents, EffectManager effectManager, AnimationManager animManager,
                  World world) {
        this.animManager = animManager;
        this.effectManager = effectManager;
        this.anims = anims;
        this.animEvents = animEvents;
        this.height = height;
        this.width = width;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;
        bodyDef.position.set(x, y);
        this.body = world.createBody(bodyDef);
        getBody().setUserData(this);

        spawnPos.set(x, y);
    }

    public Entity(float width, float height, Map<T, AnimationWrapper> anims,
                  Map<T, List<AnimationEvent>> animEvents, EffectManager effectManager, AnimationManager animManager,
                  Body body) {
        this.animManager = animManager;
        this.effectManager = effectManager;
        this.anims = anims;
        this.animEvents = animEvents;
        this.height = height;
        this.width = width;
        this.body = body;

        spawnPos.set(body.getPosition());
    }

    public void init() {
        if (autoMove) setVelFromSpeed(startingSpeed);
        if (!enabled) getBody().setActive(false);
    }

    public void act(float delta) {
        if (enabled) {
            movementUpdate(delta);
            updateAnimationFlags(delta);
            stateTime += delta;
            for(TimedImpulse timedImpulse : timedImpulses) {
                applyWeightedImpulse(timedImpulse.getImpulse(delta));
                timedImpulse.update(delta);
            }
            timedImpulses.removeIf(TimedImpulse::isExpired);
        } else {
            spawnDelay -= delta;
            if (spawnDelay <= 0) {
                enabled = true;
                getBody().setActive(true);
            }
        }
    }

    private void movementUpdate(float delta) {
        if (isAutoMove()) {
            Vector2 direction = getMovementDirection();
            if (isFlying()) {
                if (anims.get(getState()).getKeyFrameIndex(getStateTime()) == 0 &&
                    anims.get(getState()).getKeyFrameIndex(getStateTime() + delta) == 1) {
                    float jumpBoost = 4;
                    if (getBody().getLinearVelocity().y < 0 && direction.y >= 0) {
                        applyTimedImpulse(0, -getBody().getLinearVelocity().y * 2, 0.2f);
                    }
                    applyClampedTimedImpulse(
                        direction.scl(getAcceleration() * anims.get(getState()).getAnimationDuration()),
                        new Vector2(-getSpeed(), 0), new Vector2(getSpeed(), getSpeed() + jumpBoost), 0.1f
                    );
                }
            } else if (isFloating) {
                applyWeightedForce(
                    new Vector2(direction).scl(
                        Math.min(getAcceleration(), speed - getBody().getLinearVelocity().len())
                    )
                );
                int turnDir = 1;
                if (direction.angleDeg(getBody().getLinearVelocity()) > 180) {
                    turnDir = -1;
                }
                float turnForce = Math.min(180 - Math.abs(direction.angleDeg(getBody().getLinearVelocity()) - 180), 90) / 90;
                applyTurnForce(turnAcceleration * turnForce, turnDir);
            } else {
                applyClampedForce(direction.scl(getAcceleration()), new Vector2(-speed, -speed),
                    new Vector2(speed, speed));
            }
        }

        if (damping().x != 0 || damping().y != 0) {
            applyWeightedForce(
                (damping().x / 100 * -getBody().getLinearVelocity().x * Math.abs(getBody().getLinearVelocity().x)),
                (damping().y / 100 * -getBody().getLinearVelocity().y * Math.abs(getBody().getLinearVelocity().y))
            );
        }
    }

    private Vector2 getMovementDirection() {
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
            case PREDICT_LINE:
                direction.set(getPredictedTargetPos(targetPos.dst(spawnPos()) / speed));
                direction.sub(spawnPos()).nor();
                break;
            case CIRCLE:
            case CAUTION:
                break;
        }
        return direction;
    }

    public void setSpawnDelay(float spawnDelay) {
        this.enabled = false;
        this.spawnDelay = spawnDelay;
    }

    /**
     * Applies an impulse to set current speed based on calculated movement direction. All variables for movement
     * calculations must be set before calling this.
     * @param speed to calculate new velocity from
     */
    public void setVelFromSpeed(float speed) {
        applyWeightedImpulse(getMovementDirection().scl(speed).sub(getBody().getLinearVelocity()));
    }

    public Vector2 getPredictedTargetPos(float time) {
        return new Vector2(targetPos).add(new Vector2(targetVel).scl(time));
    }

    public void draw(SpriteBatch batch, float delta) {
        if (!enabled || !visible) return;
        try {
            TextureRegion frame = anims.get(getState()).getKeyFrame(getStateTime());
            batch.draw(frame,
                getPosition().x - (frame.getRegionWidth() / 2f + anims.get(getState()).getOffset().x) / 32f,
                getPosition().y - (frame.getRegionHeight() / 2f + anims.get(getState()).getOffset().y) / 32f,
                (frame.getRegionWidth() / 2f + anims.get(getState()).getOffset().x) / 32f,
                (frame.getRegionHeight() / 2f + anims.get(getState()).getOffset().y) / 32f,
                frame.getRegionWidth() / 32f, frame.getRegionHeight() / 32f, getSpriteDirection(),
                1, 0);
        } catch (Exception e) {
            if (getState() == null) System.err.printf("Null state on object [%s]%n", getClass());
            else System.err.printf("Couldn't find animation for state [%s] of object [%s]%n",
                getState().name(), getClass());
        }
    }

    public void debugDraw(Matrix4 projectionMatrix) {
        if (isAutoMove() && targetPos.x != 0) {
            Vector2 direction = getMovementDirection();
            Utils.drawLine(
                getPosition(),
                getPosition().add(direction.scl(speed)),
                2, Color.YELLOW, projectionMatrix
            );
        }
    }

    public void setVisible(boolean isVisible) {
        this.visible = isVisible;
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
        if (this.state != null) endState();
        this.state = state;
        stateTime = 0;
        beginState();
    }

    protected void beginState() { }

    protected void endState() { }

    protected void updateAnimationFlags(float delta) {
        if (animEvents.containsKey(state)) {
            for (AnimationEvent animEvent : animEvents.get(state)) {
                if (getStateTime() <= animEvent.time && getStateTime() + delta > animEvent.time) {
                    onAnimEvent(animEvent);
                }
            }
        }
    }

    protected void onAnimEvent(AnimationEvent animEvent) { }

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

    public AnimationWrapper getCurrentAnim() {
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

    public void applyClampedTimedImpulse(Vector2 impulse, Vector2 minVelocity, Vector2 maxVelocity, float duration) {
        Vector2 vel = getBody().getLinearVelocity();
        if (minVelocity == null) minVelocity = new Vector2(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        if (maxVelocity == null) maxVelocity = new Vector2(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        if (impulse.x != 0 &&
            ((vel.x > minVelocity.x && impulse.x < 0) || (vel.x < maxVelocity.x && impulse.x >= 0))) {
            applyTimedImpulse(Math.min(Math.max(impulse.x, minVelocity.x - vel.x), maxVelocity.x - vel.x), 0, duration);
        }
        if (impulse.y != 0 &&
            ((vel.y > minVelocity.y && impulse.y < 0) || (vel.y < maxVelocity.y && impulse.y >= 0))) {
            applyTimedImpulse(0, Math.min(Math.max(impulse.y, minVelocity.y - vel.y), maxVelocity.y - vel.y), duration);
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

    public void applyTurnForce(float turnAcceleration, int direction) {
        applyWeightedForce(new Vector2(getBody().getLinearVelocity())
            .setAngleDeg(getBody().getLinearVelocity().angleDeg() + 90 * direction).setLength(turnAcceleration));
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

    public Vector2 getTargetVel() {
        return targetVel;
    }

    public void setTargetVel(Vector2 targetVel) {
        this.targetVel = targetVel;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
        this.startingSpeed = speed;
    }

    public void setSpeed(float speed, float startingSpeed) {
        this.speed = speed;
        this.startingSpeed = startingSpeed;
    }

    public float getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
        this.turnAcceleration = acceleration;
    }

    public void setAcceleration(float acceleration, float turnAcceleration) {
        this.acceleration = acceleration;
        this.turnAcceleration = turnAcceleration;
    }

    public Vector2 damping() {
        return damping;
    }

    public void setHitStunTimer(float hitStunTime) {
        this.hitStunTimer = hitStunTime;
    }

    public void updateHitStun(float delta) {
        hitStunTimer -= delta;
        bufferedVelocity.add(getBody().getLinearVelocity());
        applyWeightedImpulse(getBody().getLinearVelocity().scl(-1));
        if (!isHitStunned()) {
            applyWeightedImpulse(bufferedVelocity);
            bufferedVelocity.set(0, 0);
        }
    }

    public boolean isHitStunned() {
        return hitStunTimer > 0;
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
