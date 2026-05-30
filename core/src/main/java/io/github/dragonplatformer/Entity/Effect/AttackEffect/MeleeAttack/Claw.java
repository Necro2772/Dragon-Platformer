package io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;

public class Claw extends MeleeAttack {
    public Claw(float damage, float knockback, float width, float height, Vector2 offset,
                AnimationManager animManager, Body body) {
        super(damage, knockback, width, height, AnimationKey.EFFECT_CLAWSWIPE,
            animManager, body);
        init(width, height, offset);
    }
}
