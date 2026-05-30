package io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.MovementType;

public class Fireball extends Projectile {
    public Fireball(float damage, float knockback, float health, float x, float y, float diameter,
                    Vector2 targetPos, AnimationManager animManager, boolean isPlayer, World world) {
        super(damage, knockback, health, 1.3f, x, y, diameter, diameter,
            AnimationKey.EFFECT_FIREBALL, animManager, isPlayer, world);
        setCollisionAsCircle(diameter / 2 - 0.1f, new Vector2(0.1f, 0));
        setTargetPos(targetPos);
        setAutoMove(true);
        setMovementType(MovementType.LINE);
        init();
    }
}
