package io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.EffectManager;
import io.github.dragonplatformer.Entity.MovementType;

public class Fireball extends Projectile {
    public Fireball(float damage, float knockback, float health, float x, float y, float diameter,
                    Vector2 targetPos, EffectManager effectManager, AnimationManager animManager, boolean isPlayer, World world) {
        super(damage, knockback, health, 1.5f, x, y, diameter, diameter, effectManager.obtainFireball(),
            AnimationKey.EFFECT_FIREBALL, effectManager, animManager, isPlayer, world);
        setCollisionAsCircle(diameter / 2 - 0.1f, new Vector2(0.1f, 0));
        setTargetPos(new Vector2(targetPos));
        setAutoMove(true);
        setMovementType(MovementType.LINE);
        setSpeed(16);
        setAcceleration(10);
        init();
    }

    @Override
    public boolean hit(Fixture contactFixture) {
        boolean result = super.hit(contactFixture);
        if (result) {
            ParticleEffect hitEffect = effectManager.obtainFireballHit();
            hitEffect.setPosition(getPosition().x, getPosition().y);
            hitEffect.reset();
        }
        return result;
    }
}
