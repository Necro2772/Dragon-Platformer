package io.github.dragonplatformer.Entity.AttackEffect;

import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationManager;

public class FireballExplosion extends MeleeAttack {

    public FireballExplosion(Vector2 position, float damage, float knockback, float width, float height, Vector2 offset,
                             int direction, AnimationManager animationManager, boolean isPlayer, World world) {
        super(position.x, position.y, damage, knockback, width, height, direction,
            animationManager.getEffectAnims(AnimationManager.AnimationKeys.EFFECT_EXPLOSION), isPlayer, world);
        init(width, height, offset);
//        Ellipse shape = new Ellipse();
//        shape.set();
//        shape.setRadius(width / 2f);
//        init(shape, offset);
//        shape.dispose();
    }
}
