package io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.Actor.Actor;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.EffectManager;
import io.github.dragonplatformer.Entity.MovementType;

public class IceSpike extends Projectile {
    float slowDuration = 2f;

    public IceSpike(float damage, float knockback, float x, float y, Vector2 targetPos,
                    EffectManager effectManager, AnimationManager animManager, boolean isPlayer, World world) {
        super(damage, knockback, 1, 1.5f, x, y, 0.4f, effectManager.obtainIceSpike(),
            AnimationKey.EFFECT_EMPTY, effectManager, animManager, isPlayer, world);
        setTargetPos(new Vector2(targetPos));
        setAutoMove(true);
        setMovementType(MovementType.LINE);
        setSpeed(12);
        setAcceleration(100);
        init();
    }

    @Override
    public boolean hit(Fixture contactFixture) {
        boolean result = super.hit(contactFixture);
        if (result) {
            ParticleEffect hitEffect = effectManager.obtainIceSpikeHit();
            hitEffect.setPosition(getPosition().x, getPosition().y);
            hitEffect.start();

            if (contactFixture.getUserData() instanceof Actor) {
                Actor<?> actor = (Actor<?>) contactFixture.getUserData();
                actor.stats().slow(slowDuration);
            }
        }
        return result;
    }
}
