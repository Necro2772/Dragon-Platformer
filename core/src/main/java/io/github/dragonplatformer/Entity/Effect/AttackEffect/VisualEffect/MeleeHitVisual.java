package io.github.dragonplatformer.Entity.Effect.AttackEffect.VisualEffect;

import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.EffectManager;

public class MeleeHitVisual extends VisualEffect {
    public MeleeHitVisual(float x, float y, float rotation, EffectManager effectManager, AnimationManager animManager, World world) {
        super(x, y, 0.2f, rotation, effectManager.obtainMeleeHit(), effectManager, animManager, world);
        init();
    }
}
