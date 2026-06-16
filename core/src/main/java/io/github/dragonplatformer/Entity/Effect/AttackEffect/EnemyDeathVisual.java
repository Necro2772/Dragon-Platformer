package io.github.dragonplatformer.Entity.Effect.AttackEffect;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.EffectManager;

public class EnemyDeathVisual extends AttackVisual {
    ParticleEffect deathEffect;

    public EnemyDeathVisual(float x, float y, EffectManager effectManager, AnimationManager animManager, World world) {
        super(x, y, 2, 2, AnimationKey.EFFECT_ENEMYDEATH, effectManager, animManager, world);
        init();
        setVisible(false);
        deathEffect = effectManager.obtainMeleeHit();
        deathEffect.reset();
        deathEffect.setPosition(getPosition().x, getPosition().y);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }
}
