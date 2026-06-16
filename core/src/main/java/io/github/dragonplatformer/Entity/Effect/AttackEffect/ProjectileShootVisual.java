package io.github.dragonplatformer.Entity.Effect.AttackEffect;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.EffectManager;

public class ProjectileShootVisual extends AttackVisual {
    public ProjectileShootVisual(float x, float y, EffectManager effectManager, AnimationManager animManager, World world) {
        super(x, y, 1, 2, AnimationKey.EFFECT_PROJECTILESHOOT, effectManager, animManager, world);
        init();
        ParticleEffect projectileShootEffect = effectManager.obtainProjectileShoot();
        projectileShootEffect.setPosition(getPosition().x, getPosition().y);
        projectileShootEffect.reset();
    }
}
