package io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile.Projectile;
import io.github.dragonplatformer.Entity.EffectManager;

public class WindAura extends MeleeAttack {
    ParticleEffect effect;
    final float pushForce = 10;
    public WindAura(float diameter, Vector2 offset, EffectManager effectManager, AnimationManager animManager, Body body) {
        super(0, 0, 1, diameter, diameter, AnimationKey.EFFECT_EMPTY, effectManager, animManager, body);
        init(diameter / 2, offset);
        effect = effectManager.obtainGlide();
        effect.scaleEffect(2);
        effect.setPosition(body.getPosition().x, body.getPosition().y);
        effect.start();
    }

    @Override
    public void act(float delta) {
        effect.setPosition(getPosition().x, getPosition().y);
        super.act(delta);
    }

    @Override
    public boolean hit(Fixture contactFixture) {
        if (contactFixture.getUserData() instanceof Projectile) {
            Projectile p = (Projectile) contactFixture.getUserData();
            float dst = p.getBody().getPosition().dst(getPosition());
            p.applyWeightedImpulse(new Vector2(p.getBody().getPosition()).sub(getPosition()).setLength(pushForce * getWidth() / dst));
        }
        return false;
    }

    @Override
    public void destroy() {
        effect.allowCompletion();
        super.destroy();

    }
}
