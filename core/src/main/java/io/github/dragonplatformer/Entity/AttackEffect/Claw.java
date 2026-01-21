package io.github.dragonplatformer.Entity.AttackEffect;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import io.github.dragonplatformer.Entity.AnimationManager;

public class Claw extends MeleeAttack {
    public Claw(float damage, float knockback, float width, float height, Vector2 offset, int direction, AnimationManager animManager, Body body) {
        super(damage, knockback, width, height, offset, direction, animManager.getEffectAnims(AnimationManager.AnimationKeys.EFFECT_CLAWSWIPE), body);
    }
}
