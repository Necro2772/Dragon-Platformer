package io.github.dragonplatformer.Entity.Actor.Enemy;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack.Claw;
import io.github.dragonplatformer.Entity.EffectManager;

public class WyvernMelee extends Enemy {
    private final float idleWait;
    private float idleWaitCurrent;
    private final float attackWait;
    private final Vector2 attackTarget;

    public WyvernMelee(float x, float y, World world, EffectManager effectManager, AnimationManager animManager) {
        super(x, y, 2, 2, world, effectManager, animManager, AnimationKey.ENEMY_MANTICORE);
        setPlayerSensorShape(new Vector2(15, 20), new Vector2(0, 0));
        setDisperseDist(6);
        init();
        stats().setMaxHealth(3);
        stats().setCrystalLoot(2);
        stats().setAggroRange(50);
        stats().setMinDst2(4);
        stats().setMaxDst2(25);
        stats().setDisperseForce(30);
        stats().walkSpeed = 3;
        stats().runSpeed = 5;

        setFlying(true);

        idleWait = 2f;
        idleWaitCurrent = idleWait;
        attackWait = 0.55f;
        attackTarget = new Vector2();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (getState() == EnemyState.DEATH) return;

        switch (getState()) {
            case IDLE:
                setState(EnemyState.FLYIDLE);
                break;
            case FLYIDLE:
                if (stats().isPlayerInRange() && getPosition().dst2(stats().getPlayerPos()) < 25) {
                    idleWaitCurrent -= delta;
                    if (idleWaitCurrent <= 0) {
                        idleWaitCurrent = idleWait;
                        setState(EnemyState.FLYCHARGESHOOTPROJECTILE);
                    }
                }
                break;
            case FLYCHARGESHOOTPROJECTILE:
                if (getStateTime() > attackWait) {
                    setState(EnemyState.FLYSHOOTPROJECTILE);
                }
                break;
            case FLYSHOOTPROJECTILE:
                break;
        }
    }

    @Override
    public void beginState() {
        super.beginState();
        switch (getState()) {
            case FLYIDLE:
                stats().resetComboCount();
                break;
            case FLYCHARGESHOOTPROJECTILE:
                attackTarget.set(stats().getPlayerPos());
                break;
            case FLYSHOOTPROJECTILE:
                stats().incrementComboCount();
                new Claw(1, 1, 3,
                    new Vector2(attackTarget).sub(getPosition()).setLength(2), effectManager, animManager, getBody());
                break;
        }
    }
}
