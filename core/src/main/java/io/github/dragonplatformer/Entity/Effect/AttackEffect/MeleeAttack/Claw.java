package io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.EffectManager;

public class Claw extends MeleeAttack {
    public Claw(float damage, float knockback, float width, float height, Vector2 offset, EffectManager effectManager,
                AnimationManager animManager, Body body) {
        super(damage, knockback, width * 2, height * 1.2f, AnimationKey.EFFECT_CLAWSWIPE, effectManager,
            animManager, body);
        init(width, height, offset);
    }

    public Claw(float damage, float knockback, float diameter, Vector2 offset, EffectManager effectManager,
                AnimationManager animManager, Body body) {
        super(damage, knockback, diameter * 1.2f, diameter * 1.2f, AnimationKey.EFFECT_CLAWSWIPE, effectManager,
            animManager, body);
        init(diameter / 2, offset);
    }
}
