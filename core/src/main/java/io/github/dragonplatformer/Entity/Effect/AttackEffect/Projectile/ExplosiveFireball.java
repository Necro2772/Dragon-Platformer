package io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack.FireballExplosion;
import io.github.dragonplatformer.Entity.EffectManager;
import io.github.dragonplatformer.Entity.MovementType;

public class ExplosiveFireball extends Projectile {
    float explosionDamage;
    float explosionKnockback;

    public ExplosiveFireball(float damage, float knockback, float health, float x, float y, float diameter,
                             Vector2 targetPos, EffectManager effectManager, AnimationManager animationManager, boolean isPlayer, World world) {
        super(0, 0, health, 2, x, y, diameter, diameter, effectManager.obtainFireballMedium(),
            AnimationKey.EFFECT_FIREBALL, effectManager, animationManager, isPlayer, world);
        setCollisionAsCircle(diameter / 2 - 0.2f, new Vector2());
        this.explosionDamage = damage;
        this.explosionKnockback = knockback;
        setTargetPos(new Vector2(targetPos));
        setAutoMove(true);
        setMovementType(MovementType.LINE);
        setSpeed(18);
        setAcceleration(100);
        init();
    }

    @Override
    public boolean hit(Fixture contactFixture) {
        new FireballExplosion(getBody().getPosition(), explosionDamage, explosionKnockback,
            new Vector2(0, 0), effectManager, animManager, getIsPlayer(), getBody().getWorld());
        return super.hit(contactFixture);
    }
}
