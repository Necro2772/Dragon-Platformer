package io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.EffectManager;

public class FireballExplosion extends MeleeAttack {
    ParticleEffect explosionEffect;

    public FireballExplosion(Vector2 position, float damage, float knockback, float width, float height, Vector2 offset,
                             EffectManager effectManager, AnimationManager animationManager, boolean isPlayer, World world) {
        super(position.x, position.y, damage, knockback, width, height,
            AnimationKey.EFFECT_EXPLOSION, effectManager, animationManager, isPlayer, world);
        init(width, height, offset);
        setVisible(false);
        explosionEffect = effectManager.obtainExplosion();
        explosionEffect.reset();
        explosionEffect.setPosition(getPosition().x, getPosition().y);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        explosionEffect.setPosition(getPosition().x, getPosition().y);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
