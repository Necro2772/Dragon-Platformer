package io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile;

import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;

public class Fireball extends Projectile {
    public Fireball(float damage, float knockback, float health, float x, float y, float width, float height, int direction,
                    AnimationManager animManager, boolean isPlayer, World world) {
        super(damage, knockback, health, 1.3f, x, y, width, height, direction,
            AnimationKey.EFFECT_FIREBALL, animManager, isPlayer, world);
        init();
    }
}
