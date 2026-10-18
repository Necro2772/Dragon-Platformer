package io.github.dragonplatformer.Entity.Effect.AttackEffect.VisualEffect;

import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.EffectManager;

public class ProjectileShootVisual extends VisualEffect {
    public ProjectileShootVisual(float x, float y, EffectManager effectManager, AnimationManager animManager, World world) {
        super(x, y, 0.2f, 0, effectManager.obtainProjectileShoot(), effectManager, animManager, world);
        init();
    }
}
