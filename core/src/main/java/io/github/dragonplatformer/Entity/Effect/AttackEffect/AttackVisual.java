package io.github.dragonplatformer.Entity.Effect.AttackEffect;

import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;

public abstract class AttackVisual extends AttackEffect {
    public AttackVisual(float x, float y, float width, float height,
                        AnimationKey animKey, AnimationManager animManager, World world) {
        super(0, 0, x, y, width, height, animKey, animManager, world);
    }
}
