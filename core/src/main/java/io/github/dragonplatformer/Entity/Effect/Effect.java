package io.github.dragonplatformer.Entity.Effect;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.EffectManager;
import io.github.dragonplatformer.Entity.Entity;

public abstract class Effect extends Entity<EffectState> {
    private boolean isDisjointFixture = false;
    public Effect(float width, float height, AnimationKey animKey, EffectManager effectManager,
                  AnimationManager animManager, Body body) {
        super(width, height, animManager.getEffectAnims(animKey), animManager.getEffectAnimEvents(animKey),
            effectManager, animManager, body);
    }

    public Effect(float x, float y, float width, float height, AnimationKey animKey, EffectManager effectManager,
                  AnimationManager animManager, World world) {
        super(x, y, width, height, animManager.getEffectAnims(animKey), animManager.getEffectAnimEvents(animKey),
            effectManager, animManager, world);
    }

    @Override
    public void init() {
        super.init();
        setState(EffectState.IDLE);
        if (!isDisjointFixture()) {
            setFloating(true);
            damping().set(0, 0);
        }
        setVisible(false);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (getState() == EffectState.DESTROYED && anims.get(getState()).isAnimationFinished(getStateTime())) {
            destroy();
        } else if (anims.get(getState()).getPlayMode() == Animation.PlayMode.NORMAL
            && anims.get(getState()).isAnimationFinished(getStateTime())) {
            setState(EffectState.DESTROYED);
        }
    }

    @Override
    protected void beginState() {
        super.beginState();
        if (getState() == EffectState.DESTROYED) {
            if (!anims.containsKey(getState())) destroy();
        }
    }

    public boolean isDisjointFixture() {
        return isDisjointFixture;
    }

    public void setDisjointFixture(boolean disjointFixture) {
        isDisjointFixture = disjointFixture;
        setAutoMove(false);
    }
}
