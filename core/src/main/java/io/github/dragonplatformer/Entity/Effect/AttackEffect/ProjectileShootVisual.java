package io.github.dragonplatformer.Entity.Effect.AttackEffect;

import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;

public class ProjectileShootVisual extends AttackVisual {
    public ProjectileShootVisual(float x, float y, AnimationManager animManager, World world) {
        super(x, y, 1, 2, AnimationKey.EFFECT_PROJECTILESHOOT, animManager, world);
        init();
    }
}
