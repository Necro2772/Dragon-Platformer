package io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.EffectManager;

public class GlideAttack extends MeleeAttack {
    ParticleEffect glideAttackEffect;

    public GlideAttack(float damage, float knockback, float duration, float width, float height, Vector2 offset, float rotation,
                       ParticleEffect effect, EffectManager effectManager, AnimationManager animManager, Body body) {
        super(damage, knockback, duration, 1, 1, AnimationKey.EFFECT_CLAWSWIPE, effectManager,
            animManager, body);
        init(width, height, offset);
        glideAttackEffect = effect;
        glideAttackEffect.reset();
        glideAttackEffect.start();
        glideAttackEffect.setPosition(getPosition().x, getPosition().y);
        glideAttackEffect.getEmitters().forEach((emmiter) ->
            emmiter.getAngle().setLow(rotation));

    }

    @Override
    public void act(float delta) {
        super.act(delta);
        glideAttackEffect.setPosition(getPosition().x + getBody().getLinearVelocity().x / 5, getPosition().y + getBody().getLinearVelocity().y / 5);
    }

    @Override
    public void destroy() {
        glideAttackEffect.allowCompletion();
        super.destroy();
    }
}
