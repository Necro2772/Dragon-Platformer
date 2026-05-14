package io.github.dragonplatformer.Entity.Creature.Player;

public enum PlayerState {
    IDLE,
    RUNNING,
    FLYING,
    JUMPING(FLYING),
    FLAPPING(FLYING),
    GLIDING,
    DIVING,
    SOAR,
    DASH(GLIDING),
    DASHDIVE(DIVING),
    ATTACKFORWARD(FLYING),
    ATTACKFORWARD2(FLYING),
    ATTACKFORWARD3(FLYING),
    ATTACKUP(FLYING),
    ATTACKDOWN(FLYING),
    ATTACKDIVE(FLYING),
    ATTACKDASH(FLYING),
    ATTACKGLIDE(FLYING),
    ATTACKJUMP(FLYING),
    DEATH(IDLE);

    //        private final boolean canRotate;
//        private final boolean isAttack;
//        private final boolean isDash;
//        private final boolean attackCancelable;
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

    public boolean isBlocking() {
        return isBlocking;
    }

    public PlayerState nextState() {
        return autoTransition;
    }

//        public boolean isAttack() {
//            return isAttack;
//        }
//
//        public boolean isAttackCancelable() {
//            return attackCancelable;
//        }
}
