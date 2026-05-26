package io.github.dragonplatformer.Entity.Effect.AttackEffect;

import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;

public class ProjectileShootVisual extends AttackVisual {
    public ProjectileShootVisual(float x, float y, int direction, AnimationManager animManager, World world) {
        super(x, y, 1, 2, direction, AnimationKey.EFFECT_PROJECTILESHOOT, animManager, world);
    }
}
