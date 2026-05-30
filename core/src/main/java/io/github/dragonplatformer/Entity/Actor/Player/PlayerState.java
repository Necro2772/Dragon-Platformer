package io.github.dragonplatformer.Entity.Actor.Player;

import io.github.dragonplatformer.Entity.EntityState;

public enum PlayerState implements EntityState {
    IDLE,
    RUNNING,
    FLYING,
    JUMP(FLYING),
    FLAPPING(FLYING),
    GLIDE,
    DIVE,
    SOAR,
    EVADE(FLYING),
    EVADE_HORIZONTAL(GLIDE),
    EVADE_UP(SOAR),
    EVADE_DOWN(DIVE),
    ATTACK_FORWARD1(FLYING),
    ATTACK_FORWARD2(FLYING),
    ATTACK_UP(FLYING),
    ATTACK_DOWN(FLYING),
    ATTACK_GLIDE(GLIDE),
    ATTACK_GLIDE_HIT(GLIDE),
    ATTACK_SOAR(SOAR),
    ATTACK_DIVE(FLYING),
    ATTACK_DIVE_LAND(IDLE),
    ATTACK_GROUND1(IDLE),
    ATTACK_GROUND2(IDLE),
    DEATH(IDLE);

    private final PlayerState autoTransition;
    private final boolean isBlocking;

    PlayerState() {
        this.autoTransition = null;
        this.isBlocking = false;
    }

    PlayerState(PlayerState autoTransition) {
        this.autoTransition = autoTransition;
        this.isBlocking = autoTransition != null;
    }

    PlayerState(PlayerState autoTransition, boolean isBlocking) {
        this.autoTransition = autoTransition;
        this.isBlocking = isBlocking;
    }

    public boolean isNonBlocking() {
        return !isBlocking;
    }

    public PlayerState nextState() {
        return autoTransition;
    }

}
