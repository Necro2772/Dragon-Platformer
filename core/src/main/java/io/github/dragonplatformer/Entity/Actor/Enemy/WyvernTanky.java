package io.github.dragonplatformer.Entity.Actor.Enemy;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack.Claw;

public class WyvernTanky extends Enemy {
    private final float idleWait;
    private final float attackWait;
    private final Vector2 attackTarget;

    public WyvernTanky(float x, float y, World world, AnimationManager animManager) {
        super(x, y, 3, 3, world, animManager, AnimationKey.ENEMY_MANTICORE);
        setPlayerSensorShape(new Vector2(10, 15), new Vector2(0, 0));
        init();
        stats().setMaxHealth(5);
        stats().setCrystalLoot(2);
        stats().setAggroRange(30);
        stats().setMinDst2(0);
        stats().setMaxDst2(25);
        stats().setDisperseForce(80);
        setDisperseDist(8);
        stats().walkSpeed = 1;
        stats().runSpeed = 2;

        setFlying(true);

        idleWait = 2f;
        attackWait = 0.7f;
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
                if (getStateTime() > idleWait && stats().isPlayerInRange()
                    && getPosition().dst2(stats().getPlayerPos()) < 25) {
                    setState(EnemyState.FLYCHARGESHOOTPROJECTILE);
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
                new Claw(1, 1, 4,
                    new Vector2(attackTarget).sub(getPosition()).setLength(3), animManager, getBody());
                break;
        }
    }
}
