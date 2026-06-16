package io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.EffectManager;
import io.github.dragonplatformer.Entity.MovementType;

public class Firebomb extends Projectile {
    public Firebomb(float damage, float knockback, float health, float x, float y, float diameter,
                    Vector2 targetPos, EffectManager effectManager, AnimationManager animManager, boolean isPlayer, World world) {
        super(damage, knockback, health, 2f, x, y, diameter, diameter,
            AnimationKey.EFFECT_FIREBALL, effectManager, animManager, isPlayer, world);
        setCollisionAsCircle(diameter / 2 - 0.1f, new Vector2(0.1f, 0));
        setTargetPos(targetPos);
        setAutoMove(true);
        setMovementType(MovementType.APPROACH);
        setSpeed(4);
        setAcceleration(100);
        init();
    }

    @Override
    public boolean hit(Fixture contactFixture) {
        onExpire();
        return super.hit(contactFixture);
    }

    @Override
    public void onExpire() {
        super.onExpire();
        new LingeringFireExplosion(1, 1, 1, getPosition().x, getPosition().y, 4,
            effectManager, animManager, getIsPlayer(), getBody().getWorld());
    }
}
