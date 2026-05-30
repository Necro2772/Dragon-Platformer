package io.github.dragonplatformer.Entity.Effect.AttackEffect;

import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;

public class EnemyDeathVisual extends AttackVisual {
    public EnemyDeathVisual(float x, float y, AnimationManager animManager, World world) {
        super(x, y, 2, 2, AnimationKey.EFFECT_ENEMYDEATH, animManager, world);
        init();
    }
}
