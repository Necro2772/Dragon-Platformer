package io.github.dragonplatformer.Entity.Effect.AttackEffect.VisualEffect;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.AttackEffect;
import io.github.dragonplatformer.Entity.Effect.EffectState;
import io.github.dragonplatformer.Entity.EffectManager;

public abstract class VisualEffect extends AttackEffect {
    ParticleEffect effect;
    private float duration;

    public VisualEffect(float x, float y, float duration, float rotation, ParticleEffect effect, EffectManager effectManager,
                        AnimationManager animationManager, World world) {
        super(0, 0, x, y, 1, 1, AnimationKey.EFFECT_EMPTY,
            effectManager, animationManager, world);
        this.effect = effect;
        this.duration = duration;
        effect.reset();
        effect.setPosition(getPosition().x, getPosition().y);
        effect.getEmitters().forEach((emmiter) -> emmiter.getAngle().setLow(rotation));
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (effect != null) effect.setPosition(getPosition().x, getPosition().y);
        duration -= delta;
        if (duration <= 0) setState(EffectState.DESTROYED);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (effect != null) effect.allowCompletion();
    }
}
