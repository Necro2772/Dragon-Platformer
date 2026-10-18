package io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.EffectManager;

public class EmptyMelee extends MeleeAttack {
    boolean reflectProjectiles = false;
    public EmptyMelee(float damage, float knockback, float duration, float width, float height, Vector2 offset, EffectManager effectManager,
                      AnimationManager animManager, Body body) {
        super(damage, knockback, duration, width * 2, height * 1.2f, AnimationKey.EFFECT_EMPTY, effectManager,
            animManager, body);
        init(width, height, offset);
    }

    public EmptyMelee(float damage, float knockback, float duration, float diameter, Vector2 offset, EffectManager effectManager,
                      AnimationManager animManager, Body body) {
        super(damage, knockback, duration, diameter * 1.2f, diameter * 1.2f, AnimationKey.EFFECT_EMPTY, effectManager,
            animManager, body);
        init(diameter / 2, offset);
    }
}
