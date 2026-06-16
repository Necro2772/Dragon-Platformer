package io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.Actor.Actor;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.EffectManager;
import io.github.dragonplatformer.Entity.MovementType;

public class VortexFireball extends Projectile {
    public VortexFireball(float damage, float knockback, float health, float x, float y, float diameter,
                          Vector2 targetPos, EffectManager effectManager, AnimationManager animManager, boolean isPlayer, World world) {
        super(damage, knockback, health, 4f, x, y, 1, 1,
            AnimationKey.EFFECT_FIREBALL, effectManager, animManager, isPlayer, world);
        setCollisionAsCircle(diameter / 2 - 0.1f, new Vector2(0.1f, 0));
        setReflectOnStatic();
        setTargetPos(new Vector2(targetPos));
        setAutoMove(true);
        setMovementType(MovementType.LINE);
        setSpeed(2);
        setAcceleration(0);
        setHitTimer(0);
        init();
    }

    @Override
    public boolean hit(Fixture contactFixture) {
        super.hit(contactFixture);
        if (contactFixture.getUserData() instanceof Actor) {
            float vortexAccel = 20;
            Actor<?> actor = (Actor<?>) contactFixture.getUserData();
            actor.applyWeightedForce(getPosition().sub(actor.getPosition()).setLength(vortexAccel));
        }
        return false;
    }
}
