package io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.EffectManager;


public class Slash extends MeleeAttack {
    public Slash(float damage, float knockback, float width, float height, Vector2 offset,
                 EffectManager effectManager, AnimationManager animManager, Body body) {
        super(damage, knockback, width, height, AnimationKey.EFFECT_SLASH, effectManager, animManager, body);
        init(width, height, offset);
    }
}
