package io.github.dragonplatformer.Entity.Actor.Player;

public class PlayerUpgrades {
    private Upgrade projectile;

    public PlayerUpgrades() {
        projectile = Upgrade.FIREBALL_BASIC;
    }

    public Upgrade getProjectile() {
        return projectile;
    }

    public void upgrade(Upgrade upgrade) {
        projectile = upgrade;
    }

    public enum Upgrade {
        FIREBALL_BASIC,
        FIREBALL_MEDIUM,
        FIREBALL_LARGE,
        FIREBREATH
    }
}
