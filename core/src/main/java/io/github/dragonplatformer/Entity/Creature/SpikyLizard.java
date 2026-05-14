package io.github.dragonplatformer.Entity.Creature;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.AttackEffect.Slash;

public class SpikyLizard extends Enemy {
    private float attackCD = 0;

    public SpikyLizard(float x, float y, World world, AnimationManager animManager) {
        super(x, y, 4, 4, world, animManager, AnimationManager.AnimationKeys.ENEMY_SPIKYLIZARD);
        //setHitboxShape(new Vector2(2, 2));
        setPlayerSensorShape(new Vector2(16, 8));
        init();
        stats().init(10);
        setLoot(10);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (getPlayerSighted()) {
            switch (getState()) {
                case IDLE:
                    float dst = getPlayerPos().dst(getBody().getPosition());

                    if (dst < 8.5) {
                        if (getStateTime() >= attackCD) setState(EnemyState.ATTACKING, EnemyState.IDLE);
                    }
                    else {
                        moveTowardsPlayer();
                    }
                    break;
                case ATTACKING:
                    Vector2 vel = getBody().getLinearVelocity();
                    if (vel.x * getSpriteDirection() > 0.1f) {
                        getBody().applyLinearImpulse(-vel.x, 0, getBody().getPosition().x, getBody().getPosition().y, true);
                    }
                    if (anims.get(getState()).getKeyFrameIndex(getStateTime()) == 0
                        && anims.get(getState()).getKeyFrameIndex(getStateTime() + delta) == 1) {
                        new Slash(1, 5, 5, 2, new Vector2(5 * getSpriteDirection(), -2), getSpriteDirection(), animManager, getBody());
                    }
                    break;
            }
        }
    }

    @Override
    public void beginState() {
        super.beginState();
        if (getState() == EnemyState.ATTACKING) {
            attackCD = (float) Math.random() * 1 + 1;
        }
    }

    private void moveTowardsPlayer() {
        float maxSpeed = 3f;
        float accel = 2f;
        Vector2 vel = getBody().getLinearVelocity();
        Vector2 pos = getBody().getPosition();
        if (vel.x * getSpriteDirection() < maxSpeed) {
            getBody().applyLinearImpulse(accel * getSpriteDirection(), 0, pos.x, pos.y, true);
        }
    }

}
