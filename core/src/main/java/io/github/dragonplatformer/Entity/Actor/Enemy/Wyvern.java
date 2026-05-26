package io.github.dragonplatformer.Entity.Actor.Enemy;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile.Fireball;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile.Projectile;

public class Wyvern extends Enemy {
    private final float idleWait;
    private final float attackWait;

    public Wyvern(float x, float y, World world, AnimationManager animManager) {
        super(x, y, 1, 1, world, animManager, AnimationKey.ENEMY_MANTICORE);
        setPlayerSensorShape(new Vector2(15, 20), new Vector2(0, 0));
        init();
        stats().init(1, 1, 10);
        stats().setCrystalLoot(2);
        stats().setAggroRange(50);
        getBody().setGravityScale(0.75f);

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
        flapMovementUpdate(delta, new Vector2(0, 0), new Vector2(1, 6));
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
                float posx = getBody().getPosition().x;
                float posy = getBody().getPosition().y;
                Vector2 dir = new Vector2(stats().getPlayerPos().x - posx, stats().getPlayerPos().y - posy).nor();
                dir.scl(stats().getProjectileSpd());
                Projectile fireball = new Fireball(1, 5, 1,
                    getBody().getPosition().x, getBody().getPosition().y + getWidth() / 2f,
                    1, 1, getSpriteDirection(), animManager, false, getBody().getWorld());
                fireball.getBody().applyLinearImpulse(dir.x, dir.y, 0, 0, true);
                fireball.setRotation(dir.angleDeg());
    }
    }
}
