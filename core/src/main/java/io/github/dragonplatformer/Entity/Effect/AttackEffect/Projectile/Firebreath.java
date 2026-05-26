package io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;

public class Firebreath extends Projectile {
    private final Vector2 windForce;
    private final float curve;
    public Firebreath(float damage, float knockback, float health, Vector2 aimDirection, float lifetime, float x, float y, float width, float height, int direction, AnimationManager animManager, boolean isPlayer, World world) {
        super(damage, knockback, health, lifetime, x, y, width, height, direction,
            AnimationKey.EFFECT_FIREBALL, animManager, isPlayer, world);
        setReflectOnStatic();
        setPassThroughEnemies();
        init();
        this.windForce = new Vector2(aimDirection).scl(-1f);
        float angle = 120;
        curve = (float) Math.random() * angle - angle / 2;
        setHitCD(0.5f);
        setHitGroup(1);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        getBody().applyForceToCenter(getBody().getLinearVelocity().rotateDeg(curve).scl(2), true);
        getBody().applyForceToCenter(windForce, true);
    }
}
