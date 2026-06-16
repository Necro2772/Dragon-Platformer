package io.github.dragonplatformer.Entity.Effect.AttackEffect;

import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.EffectManager;

public abstract class AttackVisual extends AttackEffect {
    public AttackVisual(float x, float y, float width, float height,
                        AnimationKey animKey, EffectManager effectManager, AnimationManager animManager, World world) {
        super(0, 0, x, y, width, height, animKey, effectManager, animManager, world);
    }
}
