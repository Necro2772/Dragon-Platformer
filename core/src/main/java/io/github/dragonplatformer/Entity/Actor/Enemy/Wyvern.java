package io.github.dragonplatformer.Entity.Actor.Enemy;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.*;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile.Fireball;

public class Wyvern extends Enemy {
    private final float idleWait;
    private final float attackDelay;
    private ParticleEffect fireChargeEffect;

    public Wyvern(float x, float y, World world, EffectManager effectManager, AnimationManager animManager) {
        super(x, y, 1.5f, 1.5f, world, effectManager, animManager, AnimationKey.ENEMY_WYVERN);
        setPlayerSensorShape(new Vector2(15, 20), new Vector2(0, 0));
        init();
        stats().setMaxHealth(3);
        stats().setCrystalLoot(2);
        stats().setAggroRange(50);
        stats().walkSpeed = 2;
        stats().runSpeed = 4;

        setFlying(true);

        idleWait = 3f;
        attackDelay = 0.5f;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (getState() == EnemyState.DEATH) return;

        switch (getState()) {
            case IDLE:
                if (stats().getComboCount() < 5) {
                    setState(EnemyState.ATTACK);
                } else if (getStateTime() > idleWait && stats().isPlayerInRange()) {
                    stats().resetComboCount();
                    setState(EnemyState.ATTACK);
                }
                break;
            case ATTACK:
                fireChargeEffect.setPosition(getPosition().x + 0.7f * getSpriteDirection(), getPosition().y);
                break;
        }
    }

    @Override
    public void beginState() {
        super.beginState();
        switch (getState()) {
            case IDLE:
                break;
            case ATTACK:
                stats().incrementComboCount();
                fireChargeEffect = effectManager.obtainChargeFire();
                fireChargeEffect.start();
                fireChargeEffect.setPosition(getPosition().x + 0.7f * getSpriteDirection(), getPosition().y);
                fireChargeEffect.setDuration(500);
                break;
        }
    }

    @Override
    protected void endState() {
        super.endState();
        switch (getState()) {
            case IDLE:
                break;
            case ATTACK:
                break;
        }
    }

    @Override
    protected void onAnimEvent(AnimationEvent animEvent) {
        super.onAnimEvent(animEvent);
        if (animEvent.event == AnimationEventKey.hitframe) {
            new Fireball(
                1, 5, 1, getBody().getPosition().x,
                getBody().getPosition().y + getWidth() / 2f, 1, new Vector2(stats().getPlayerPos()),
                effectManager, animManager, false, getBody().getWorld()
            );
        }
    }
}
