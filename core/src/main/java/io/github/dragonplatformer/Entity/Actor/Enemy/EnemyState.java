package io.github.dragonplatformer.Entity.Actor.Enemy;

import io.github.dragonplatformer.Entity.EntityState;

public enum EnemyState implements EntityState {
    IDLE,
    ATTACK(IDLE),
    DEATH,
    CHARGELUNGE (IDLE),
    LUNGE (IDLE),
    CHARGESHOOTPROJECTILE (IDLE),
    SHOOTPROJECTILE (IDLE),
    FLYIDLE,
    FLYCHARGELUNGE (FLYIDLE),
    FLYCHARGESHOOTPROJECTILE (FLYIDLE),
    FLYSHOOTPROJECTILE (FLYIDLE);

    private final EnemyState autoTransition;
    private final boolean isBlocking;
    //private final boolean flying;

    EnemyState() {
        this.autoTransition = null;
        this.isBlocking = false;
    }

    EnemyState(EnemyState autoTransition) {
        this.autoTransition = autoTransition;
        this.isBlocking = autoTransition != null;
    }

    EnemyState(EnemyState autoTransition, boolean isBlocking) {
        this.autoTransition = autoTransition;
        this.isBlocking = isBlocking;
    }

    public boolean isNonBlocking() {
        return !isBlocking;
    }

    public EnemyState nextState() {
        return autoTransition;
    }
}
