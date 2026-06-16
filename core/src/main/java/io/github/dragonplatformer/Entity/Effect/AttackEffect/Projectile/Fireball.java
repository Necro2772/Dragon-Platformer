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
    ParticleEffect fireEffect;

    public Fireball(float damage, float knockback, float health, float x, float y, float diameter,
                    Vector2 targetPos, EffectManager effectManager, AnimationManager animManager, boolean isPlayer, World world) {
        super(damage, knockback, health, 1.3f, x, y, diameter, diameter,
            AnimationKey.EFFECT_FIREBALL, effectManager, animManager, isPlayer, world);
        setCollisionAsCircle(diameter / 2 - 0.1f, new Vector2(0.1f, 0));
        setTargetPos(new Vector2(targetPos));
        setAutoMove(true);
        setMovementType(MovementType.LINE);
        setSpeed(16);
        setAcceleration(100);
        init();
        fireEffect = effectManager.obtainFireball();
        fireEffect.start();
        fireEffect.setPosition(getPosition().x, getPosition().y);
        setVisible(false);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        fireEffect.setPosition(getPosition().x, getPosition().y);
    }

    @Override
    public void destroy() {
        super.destroy();
        fireEffect.setDuration(0);
    }

    @Override
    public boolean hit(Fixture contactFixture) {
        ParticleEffect hitEffect = effectManager.obtainFireballHit();
        hitEffect.setPosition(getPosition().x, getPosition().y);
        hitEffect.reset();
        return super.hit(contactFixture);
    }
}
