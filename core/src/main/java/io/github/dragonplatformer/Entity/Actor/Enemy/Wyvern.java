package io.github.dragonplatformer.Entity.Actor.Enemy;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile.Fireball;

public class Wyvern extends Enemy {
    private final float idleWait;
    private final float attackWait;

    public Wyvern(float x, float y, World world, AnimationManager animManager) {
        super(x, y, 1, 1, world, animManager, AnimationKey.ENEMY_MANTICORE);
        setPlayerSensorShape(new Vector2(15, 20), new Vector2(0, 0));
        init();
        stats().setMaxHealth(1);
        stats().setCrystalLoot(2);
        stats().setAggroRange(50);

        setFlying(true);

        idleWait = 3f;
        attackWait = 0.75f;
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
                if (getStateTime() > idleWait && stats().isPlayerInRange()) {
                    setState(EnemyState.FLYSHOOTPROJECTILE);
                }
                break;
            case FLYSHOOTPROJECTILE:
                if (getStateTime() > attackWait) {
                    if (stats().getComboCount() >= 5) {
                        setState(EnemyState.FLYIDLE);
                    } else {
                        setState(EnemyState.FLYSHOOTPROJECTILE);
                    }
                }
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
            case FLYSHOOTPROJECTILE:
                stats().incrementComboCount();
                new Fireball(1, 5, 1,
                    getBody().getPosition().x, getBody().getPosition().y + getWidth() / 2f,
                    1, new Vector2(stats().getPlayerPos()), animManager,
                    false, getBody().getWorld());
    }
    }
}
