package io.github.dragonplatformer.Entity.Effect;

import io.github.dragonplatformer.Entity.EntityState;

public enum EffectState implements EntityState {
    IDLE,
    DESTROYED;

    @Override
    public boolean isNonBlocking() {
        return true;
    }

    @Override
    public EntityState nextState() {
        return null;
    }

}
