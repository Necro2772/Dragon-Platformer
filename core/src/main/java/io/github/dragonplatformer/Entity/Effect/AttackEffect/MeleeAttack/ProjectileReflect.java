package io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile.Projectile;
import io.github.dragonplatformer.Entity.EffectManager;

public class ProjectileReflect extends MeleeAttack {
    ParticleEffect effect;
    public ProjectileReflect(float duration, float diameter, Vector2 offset, EffectManager effectManager, AnimationManager animManager, Body body) {
        super(0, 0, duration, diameter, diameter, AnimationKey.EFFECT_EMPTY, effectManager, animManager, body);
        init(diameter / 2, offset);
        effect = effectManager.obtainIceExplosion();
        effect.setPosition(getPosition().x, getPosition().y);
        effect.start();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (effect != null) effect.setPosition(getPosition().x, getPosition().y);
    }

    @Override
    public boolean hit(Fixture contactFixture) {
        if (contactFixture.getUserData() instanceof Projectile) {
            Projectile projectile = (Projectile) contactFixture.getUserData();
            projectile.reflect();
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {
        effect.allowCompletion();
        super.destroy();
    }
}
