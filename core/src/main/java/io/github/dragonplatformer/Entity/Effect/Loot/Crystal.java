package io.github.dragonplatformer.Entity.Effect.Loot;

import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.EffectManager;

public class Crystal extends Loot {
    public Crystal(float x, float y, float width, float height, World world, EffectManager effectManager,
                   AnimationManager animManager) {
        super(x, y, width, height, world, effectManager, animManager,
            AnimationKey.EFFECT_LOOT_CRYSTAL, LootType.CRYSTAL, 1);
    }
}
