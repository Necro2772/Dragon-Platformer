package io.github.dragonplatformer.Entity.Actor.Enemy;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile.Fireball;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile.Projectile;

public class Lizard extends Enemy {

    public Lizard(float x, float y, World world, AnimationManager animManager) {
        super(x, y, 1.5f, 1.5f, world, animManager, AnimationKey.ENEMY_LIZARD);
        init();
        stats().setMaxHealth(3);
        stats().setAttackCD(2);
        stats().setProjectileSpd(12);
        stats().setCrystalLoot(3);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (stats().isPlayerSighted() && stats().getAttackOnCD()) {
            setState(EnemyState.ATTACKING);
            stats().resetAttackCD();
        }
        Vector2 vel = getBody().getLinearVelocity();
        if (Math.abs(vel.x) > 0.1f) getBody().applyLinearImpulse(-vel.x, 0,
            getBody().getPosition().x, getBody().getPosition().y, true);
    }

    @Override
    public boolean damage(float attackDamage, Vector2 attackOrigin, float knockback) {
        if (super.damage(attackDamage, attackOrigin, knockback)) {
            stats().resetAttackCD();
            return true;
        }
        return false;
    }

    @Override
    public void beginState() {
        super.beginState();

        switch (getState()) {
            case ATTACKING:
                new Fireball(1, 5, 1, getBody().getPosition().x, getBody().getPosition().y,
                    1, new Vector2(stats().getPlayerPos()), animManager, false, getBody().getWorld());

        }
    }
}
