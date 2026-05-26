package io.github.dragonplatformer.Entity.Actor.Enemy;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;

public class Bat extends Enemy {
    private float waitTime;
    private final float playerDist;
    private Vector2 attackDirection;

    public Bat(float x, float y, World world, AnimationManager animManager) {
        super(x, y, 1f, 1f, world, animManager, AnimationKey.ENEMY_BAT);
        setPlayerSensorShape(new Vector2(15, 20), new Vector2(0, 0));
        init();
        stats().init(1);
        stats().setCrystalLoot(2);
        getBody().setGravityScale(0.75f);
        waitTime = (float) Math.random() * 5 + 5;
        stats().setAggroRange(50);
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

        if (stats().isPlayerSighted()) {
            Vector2 predictedPos = getPredictedPlayerPos(anims.get(getState()).getAnimationDuration() * 2);
            switch (getState()) {
                case IDLE:
                    if (waitTime <= 0) {
                        setState(EnemyState.ATTACKING);
                        waitTime = (float) Math.random() * 5 + 5;
                    } else if (stats().isPlayerSighted()) {
                        impulse.x = 4;
                        waitTime -= delta;
                        float predictedDist = new Vector2(pos).sub(predictedPos).len();
                        float currentDist = new Vector2(pos).sub(stats().getPlayerPos()).len();

                        if (playerDist - predictedDist < 0) maxVel.x = 3;
                        else maxVel.x = (playerDist - predictedDist) / playerDist * 3 + 5;
                        if (pos.x < stats().getPlayerPos().x) maxVel.x *= -1;
                        if (currentDist > playerDist) {
                            maxVel.x *= -1;
                        }
                        if (pos.y - stats().getPlayerPos().y < 5) {
                            impulse.y = 5;
                        } else if (pos.y - stats().getPlayerPos().y > 7) {
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
        //flapMovementUpdate(delta, impulse, maxVel);
    }

    @Override
    public void beginState() {
        super.beginState();
        if (getState() == EnemyState.ATTACKING) {
            attackDirection = new Vector2(stats().getPlayerPos());
            attackDirection.sub(getBody().getPosition());
            attackDirection.nor();
        }
    }
}
