package io.github.dragonplatformer.Entity.Actor.Enemy;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationEvent;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack.Claw;
import io.github.dragonplatformer.Entity.EffectManager;
import io.github.dragonplatformer.GameContactListener;

public class Gargoyle extends Enemy {
    private final float idleWait;
    private final float attackDelay;
    private final Vector2 attackTarget;

    public Gargoyle(float x, float y, World world, EffectManager effectManager, AnimationManager animManager) {
        super(x, y, 3, 3, world, effectManager, animManager, AnimationKey.ENEMY_GARGOYLE);
        setPlayerSensorShape(new Vector2(10, 15), new Vector2(0, 0));
        setDisperseDist(8);
        setNearbyEnemyGroupIndex(GameContactListener.FilterGroup.ENEMYLARGE.getBit());
        init();
        stats().setMaxHealth(8);
        stats().setCrystalLoot(2);
        stats().setAggroRange(30);
        stats().setMinDst2(0);
        stats().setMaxDst2(25);
        stats().setDisperseForce(80);
        stats().walkSpeed = 2;
        stats().runSpeed = 4;

        setFlying(true);

        idleWait = 2f;
        attackDelay = 0.7f;
        attackTarget = new Vector2();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (getState() == EnemyState.DEATH) return;

        switch (getState()) {
            case IDLE:
                if (getStateTime() > idleWait && stats().isPlayerInRange()
                    && getPosition().dst2(stats().getPlayerPos()) < 25) {
                    setState(EnemyState.ATTACK);
                }
                break;
            case ATTACK:
                if (getStateTime() < attackDelay / 2) {
                    attackTarget.set(stats().getPlayerPos());
                }
                break;
        }
    }

    @Override
    public void beginState() {
        super.beginState();
        switch (getState()) {
            case IDLE:
                stats().resetComboCount();
                break;
        }
    }

    @Override
    protected void onAnimEvent(AnimationEvent animEvent) {
        super.onAnimEvent(animEvent);
        switch (animEvent.event) {
            case hitframe:
                stats().incrementComboCount();
                new Claw(
                    1, 1, 4,
                    new Vector2(attackTarget).sub(getPosition()).setLength(3), effectManager, animManager, getBody()
                );
                break;
            default:
                break;
        }
    }
}
