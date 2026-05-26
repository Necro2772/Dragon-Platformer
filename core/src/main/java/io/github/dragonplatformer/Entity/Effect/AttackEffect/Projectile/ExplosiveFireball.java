package io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;

public class ExplosiveFireball extends Projectile {
    private final AnimationManager animationManager;
    private final boolean isPlayer;
    private final float damage;
    private final float knockback;

    public ExplosiveFireball(float damage, float knockback, float health, float x, float y, float width, float height, int direction,
                             AnimationManager animationManager, boolean isPlayer, World world) {
        super(0, 0, health, 2, x, y, width, height, direction,
            AnimationKey.EFFECT_FIREBALL, animationManager, isPlayer, world);
        init();
        this.animationManager = animationManager;
        this.isPlayer = isPlayer;
        this.damage = damage;
        this.knockback = knockback;
    }

    @Override
    public void onDestroy() {
        new FireballExplosion(getBody().getPosition(), damage, knockback, 6, 6,
            new Vector2(0, 0), getSpriteDirection(), animationManager, isPlayer, getBody().getWorld());
    }
}
