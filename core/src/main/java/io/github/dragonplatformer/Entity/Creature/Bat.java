package io.github.dragonplatformer.Entity.Creature;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationManager;

public class Bat extends Enemy {
    private float waitTime;
    private final float playerDist;
    private Vector2 attackDirection;

    public Bat(float x, float y, World world, AnimationManager animManager) {
        super(x, y, 1f, 1f, world, animManager, AnimationManager.AnimationKeys.ENEMY_BAT);
        setPlayerSensorShape(new Vector2(15, 20), new Vector2(0, 0));
        init();
        stats().init(1);
        setLoot(2);
        getBody().setGravityScale(0.75f);
        waitTime = (float) Math.random() * 5 + 5;
        setAggroRange(50);
        playerDist = 10 + (float) Math.random() * 5;
        attackDirection = new Vector2();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (getState() == EnemyState.DEATH) return;
        Vector2 maxVel = new Vector2(12, 8);
        Vector2 impulse = new Vector2(0, 0);
        Vector2 pos = getBody().getPosition();

        if (getPlayerSighted()) {
            Vector2 predictedPos = getPredictedPlayerPos(anims.get(getState()).getAnimationDuration() * 2);
            switch (getState()) {
                case IDLE:
                    if (waitTime <= 0) {
                        setState(EnemyState.ATTACKING);
                        waitTime = (float) Math.random() * 5 + 5;
                    } else if (getPlayerSighted()) {
                        impulse.x = 4;
                        waitTime -= delta;
                        float predictedDist = new Vector2(pos).sub(predictedPos).len();
                        float currentDist = new Vector2(pos).sub(getPlayerPos()).len();

                        if (playerDist - predictedDist < 0) maxVel.x = 3;
                        else maxVel.x = (playerDist - predictedDist) / playerDist * 3 + 5;
                        if (pos.x < getPlayerPos().x) maxVel.x *= -1;
                        if (currentDist > playerDist) {
                            maxVel.x *= -1;
                        }
                        if (pos.y - getPlayerPos().y < 5) {
                            impulse.y = 5;
                        } else if (pos.y - getPlayerPos().y > 7) {
                            impulse.y = -3f;
                            maxVel.y *= -1;
                        }
                    }
                    break;
                case ATTACKING:
                    if (getStateTime() > 1.5f) setState(EnemyState.IDLE);
                    float speed = 20;
                    float accel = 5;
                    impulse = new Vector2(attackDirection).scl(accel);
                    maxVel = new Vector2(attackDirection).scl(speed);
                    getBody().applyForceToCenter(0, -getBody().getGravityScale(), true);
                    break;
            }
        } else {
            setState(EnemyState.IDLE);
        }
        getBody().applyForceToCenter(new Vector2(getBody().getLinearVelocity()).scl(-1/2f), true);
        flapMovementUpdate(delta, impulse, maxVel);
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

    @Override
    public void beginState() {
        super.beginState();
        if (getState() == EnemyState.ATTACKING) {
            attackDirection = new Vector2(getPlayerPos());
            attackDirection.sub(getBody().getPosition());
            attackDirection.nor();
        }
    }
}
