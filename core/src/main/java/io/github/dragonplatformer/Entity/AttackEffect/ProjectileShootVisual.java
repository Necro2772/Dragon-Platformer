package io.github.dragonplatformer.Entity.AttackEffect;

import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationManager;

public class ProjectileShootVisual extends AttackVisual {
    public ProjectileShootVisual(float x, float y, int direction, AnimationManager animManager, World world) {
        super(x, y, 1, 2, direction,
            animManager.getEffectAnims(AnimationManager.AnimationKeys.EFFECT_PROJECTILESHOOT), world);
    }
}
