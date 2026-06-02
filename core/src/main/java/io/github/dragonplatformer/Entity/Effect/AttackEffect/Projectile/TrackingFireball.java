package io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.MovementType;

public class TrackingFireball extends Projectile {

    public TrackingFireball(float damage, float knockback, float health, float x, float y, float diameter,
                    Vector2 targetPos, Vector2 targetVel, AnimationManager animManager, boolean isPlayer, World world) {
        super(damage, knockback, health, 2f, x, y, diameter, diameter,
            AnimationKey.EFFECT_FIREBALL, animManager, isPlayer, world);
        setCollisionAsCircle(diameter / 2, new Vector2());
        setTargetPos(targetPos);
        setTargetVel(targetVel);
        setAutoMove(true);
        setMovementType(MovementType.APPROACH);
        setSpeed(16);
        setAcceleration(80);
        init();
    }
}
