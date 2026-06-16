package io.github.dragonplatformer.Entity.Actor.Enemy;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack.Claw;
import io.github.dragonplatformer.Entity.EffectManager;
import io.github.dragonplatformer.Entity.MovementType;

public class WyvernSwoop extends Enemy {
    private final float idleWait = 5f + (float) Math.random() * 3;
    private final float attackExpire = 4f;

    public WyvernSwoop(float x, float y, World world, EffectManager effectManager, AnimationManager animManager) {
        super(x, y, 2, 2, world, effectManager, animManager, AnimationKey.ENEMY_MANTICORE);
        setPlayerSensorShape(new Vector2(15, 20), new Vector2(0, 0));
        setDisperseDist(2);
        init();
        stats().setMaxHealth(3);
        stats().setCrystalLoot(2);
        stats().setAggroRange(50);
        stats().setMinDst2(4);
        stats().setMaxDst2(225);
        stats().setDisperseForce(30);
        stats().walkSpeed = 6;
        stats().runSpeed = 12;
        stats().acceleration = 30;
        stats().flyDampingX = 10;
        stats().flyDampingY = 10;
        getBody().setGravityScale(0.4f);
        setFlying(true);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (getState() == EnemyState.DEATH) return;

        switch (getState()) {
            case IDLE:
                if (getStateTime() > idleWait) {
                    setState(EnemyState.FLYIDLE);
                }
                break;
            case FLYIDLE:
                if (stats().getPlayerPos().dst2(getPosition()) < stats().getMaxDst2()) {
                    setState(EnemyState.FLYSHOOTPROJECTILE);
                } else if (getStateTime() > attackExpire) {
                    setState(EnemyState.IDLE);
                }
                break;
            case FLYSHOOTPROJECTILE:
                setState(EnemyState.IDLE);
                break;
        }
    }

    @Override
    public void beginState() {
        super.beginState();
        switch (getState()) {
            case IDLE:
                setFlying(true);
                stats().setMinDst2(4);
                stats().setMaxDst2(225);
                break;
            case FLYIDLE:
                setFlying(false);
                setMovementType(MovementType.PREDICT_LINE);
                stats().setMinDst2(4);
                stats().setMaxDst2(9);
                break;
            case FLYSHOOTPROJECTILE:
                stats().setMinDst2(100);
                stats().setMaxDst2(225);
                new Claw(1, 40, 2,
                    new Vector2(stats().getPlayerPos()).sub(getPosition()).setLength(1f), effectManager, animManager, getBody());
                break;
        }
    }
}
