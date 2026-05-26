package io.github.dragonplatformer.Entity.Actor.Player;
import io.github.dragonplatformer.Entity.Actor.ActorStats;

public class PlayerStats extends ActorStats {
    private int maxJumps;
    private float projectileMaxCD;
    private float projectileCD;
    private float jumpMaxCD;
    private float jumpCD;
    public float projectileSpeed = 25;
    private int crystals;
    public float soarCharge;
    public float glideCharge;
    private boolean evading;
    private boolean iFramesActive;

    // Movement Stats
    public float groundVelX = 8;
    public float glideVelX = 10;
    public float glideVelY = -3;
    public float flyVelX = 6;
    public float diveVelX = 4;
    public float diveVelY = 30;
    public float soarVelY = 10;
    public float evadeVelX = 0;

    public float groundAccX = 60;
    public float glideAccX = 12f;
    public float glideAccY = 60;
    public float jumpAccX = 48f;
    public float flyAccX = 12f;
    public float diveAccX = 48f;
    public float diveAccY = -36f;
    public float soarAccY = 72f;
    public float evadeAccX = 0f;

    public float glideImpulseX = 4;
    public float glideImpulseY = 0;
    public float jumpImpulseAir = 10;
    public float jumpImpulseGround = 12;
    public float evadeImpulse = 40;
    public float attackGlideImpulseX = 2;
    public float attackGlideImpulseY = 4;

    public float evadeDamping = 30;
    public float evadeDampingSmall = 10;

    // GlideCharge
    public float glideChargeBase = 0.1f;
    public float glideChargeMin = 0.3f;
    public float glideChargeMax = 1f;
    public float soarChargeMax = 1f;

    public PlayerStats() {
        super(20);
        setMaxJumps(4);
        setProjectileMaxCD(0.5f);
        setProjectileCD(0);
        setJumpMaxCD(0.75f);
        setJumpCD(0);
        crystals = 0;
        soarCharge = 0;
        glideCharge = 0;
        setEvading(false);
        setiFramesActive(false);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (getProjectileCD() > 0) setProjectileCD(getProjectileCD() - delta);
        if (getJumpCD() > 0) setJumpCD(getJumpCD() - delta);
    }

    public void resetProjectileCD() {
        setProjectileCD(getProjectileMaxCD());
    }

    public void resetProjectileCD(float cooldown) {
        setProjectileCD(cooldown);
    }

    public int getMaxJumps() {
        return maxJumps;
    }

    public void setMaxJumps(int maxJumps) {
        this.maxJumps = maxJumps;
    }

    public float getProjectileMaxCD() {
        return projectileMaxCD;
    }

    public void setProjectileMaxCD(float projectileMaxCD) {
        this.projectileMaxCD = projectileMaxCD;
    }

    public float getJumpMaxCD() {
        return jumpMaxCD;
    }

    public void setJumpMaxCD(float jumpMaxCD) {
        this.jumpMaxCD = jumpMaxCD;
    }

    public float getProjectileCD() {
        return projectileCD;
    }

    public void setProjectileCD(float projectileCD) {
        this.projectileCD = projectileCD;
    }

    public void addCrystals(int newCrystals) {
        this.crystals += newCrystals;
    }

    public int getCrystals() {
        return this.crystals;
    }

    public float getJumpCD() {
        return jumpCD;
    }

    public void setJumpCD(float jumpCD) {
        this.jumpCD = jumpCD;
    }

    public void resetJumpCD() {
        jumpCD = getJumpMaxCD();
    }

    public void chargeSoar(float charge) {
        this.soarCharge = Math.max(Math.min(soarCharge + charge, soarChargeMax), 0);
    }

    public void chargeGlide(float charge) {
        this.glideCharge = Math.max(Math.min(glideCharge + charge, glideChargeMax), 0);
    }

    public void setEvading(boolean evading) {
        this.evading = evading;
    }

    public boolean isEvading() {
        return evading;
    }

    public boolean isiFramesActive() {
        return iFramesActive;
    }

    public void setiFramesActive(boolean iFramesActive) {
        this.iFramesActive = iFramesActive;
    }

    @Override
    public boolean isIntangible() {
        return super.isIntangible() || isiFramesActive() || isEvading();
    }
}
