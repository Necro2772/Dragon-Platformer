package io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack.MeleeAttack;

public class FireballExplosion extends MeleeAttack {

    public FireballExplosion(Vector2 position, float damage, float knockback, float width, float height, Vector2 offset,
                             AnimationManager animationManager, boolean isPlayer, World world) {
        super(position.x, position.y, damage, knockback, width, height,
            AnimationKey.EFFECT_EXPLOSION, animationManager, isPlayer, world);
        init(width, height, offset);
    }
}
