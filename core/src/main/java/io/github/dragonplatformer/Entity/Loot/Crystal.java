package io.github.dragonplatformer.Entity.Loot;

import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationManager;

public class Crystal extends Loot {
    public Crystal(float x, float y, float width, float height, World world, AnimationManager animManager) {
        super(x, y, width, height, world, animManager,
            AnimationManager.AnimationKeys.LOOT_CRYSTAL, LootType.CRYSTAL, 1);
    }
}
