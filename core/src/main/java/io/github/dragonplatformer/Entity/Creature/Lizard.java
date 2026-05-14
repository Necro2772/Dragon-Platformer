package io.github.dragonplatformer.Entity.Creature;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.AttackEffect.Fireball;
import io.github.dragonplatformer.Entity.AttackEffect.Projectile;

public class Lizard extends Enemy {

    public Lizard(float x, float y, World world, AnimationManager animManager) {
        super(x, y, 1.5f, 1.5f, world, animManager, AnimationManager.AnimationKeys.ENEMY_LIZARD);
        init();
        stats().init(3, 2, 12);
        setLoot(3);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (getPlayerSighted() && stats().getAttackOnCD()) {
            setState(EnemyState.ATTACKING, EnemyState.IDLE);
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
                float posx = getBody().getPosition().x;
                float posy = getBody().getPosition().y;
                Vector2 dir = new Vector2(getPlayerPos().x - posx, getPlayerPos().y - posy).nor();
                dir.scl(stats().getProjectileSpd());
                Projectile fireball = new Fireball(1, 5, 1, getBody().getPosition().x, getBody().getPosition().y,
                    1, 1, getSpriteDirection(), animManager, false, getBody().getWorld());
                fireball.getBody().applyLinearImpulse(dir.x, dir.y, 0, 0, true);
                fireball.setRotation(dir.angleDeg());

        }
    }
}
