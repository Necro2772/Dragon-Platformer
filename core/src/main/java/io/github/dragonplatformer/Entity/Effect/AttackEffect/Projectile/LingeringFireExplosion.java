package io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;

public class LingeringFireExplosion extends Projectile {

    public LingeringFireExplosion(float damage, float knockback, float health, float x, float y, float diameter,
                                  AnimationManager animManager, boolean isPlayer, World world) {
        super(damage, knockback, health, 6, x, y, diameter, diameter, AnimationKey.EFFECT_FIREBALL,
            animManager, isPlayer, world);
        setCollisionAsCircle(diameter / 2, new Vector2());
        setAutoMove(false);
        setPassThroughStatic();
        init();
    }
}
