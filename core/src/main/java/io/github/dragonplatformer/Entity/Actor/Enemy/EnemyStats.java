package io.github.dragonplatformer.Entity.Actor.Enemy;

import com.badlogic.gdx.math.Vector2;
import io.github.dragonplatformer.Entity.Actor.ActorStats;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.AttackEffect;

import java.util.ArrayList;
import java.util.List;

public class EnemyStats extends ActorStats {
    private float hitTimer = 0;
    private float attackCD = 0;
    private float attackMaxCD;
    private float projectileSpd;
    private boolean playerSighted;
    private boolean playerInRange;
    private Vector2 playerPos;
    private Vector2 playerVel;
    private float aggroRange;
    private boolean stunOnHit;
    private int crystalLoot;
    private int comboCount;
    private float disperseForce;
    public final List<AttackEffect> activeAttackEffects;
    private float minDst2;
    private float maxDst2;

    // Movement stats
    protected float walkSpeed = 3;
    protected float runSpeed = 5;
    protected float acceleration = 30;
    protected float flyDampingX = 30;
    protected float flyDampingY = 60;

    protected EnemyStats() {
        super();
        activeAttackEffects = new ArrayList<>();
        setAttackMaxCD(0);
        setProjectileSpd(0);
        setPlayerSighted(false);
        setPlayerPos(new Vector2(0, 0));
        setPlayerVel(new Vector2(0, 0));
        setAggroRange(10);
        setStunOnHit(true);
        setCrystalLoot(0);
        resetComboCount();
        setDisperseForce(20);
        setMinDst2(36);
        setMaxDst2(100);
    }

    public void update(float delta) {
        super.update(delta);
        if (getHitTimer() > 0) setHitTimer(getHitTimer() - delta);
        if (getAttackCD() > 0) setAttackCD(getAttackCD() - delta);
    }

    public void resetAttackCD() {
        setAttackCD(getAttackMaxCD());
    }

    public boolean getAttackOnCD() {
        return getAttackCD() <= 0;
    }

    public float getHitTimer() {
        return hitTimer;
    }

    public void setHitTimer(float hitTimer) {
        this.hitTimer = hitTimer;
    }

    public float getProjectileSpd() {
        return projectileSpd;
    }

    public float getAttackCD() {
        return attackCD;
    }

    public void setAttackCD(float attackCD) {
        this.attackCD = attackCD;
    }

    public float getAttackMaxCD() {
        return attackMaxCD;
    }

    public void setAttackMaxCD(float attackMaxCD) {
        this.attackMaxCD = attackMaxCD;
    }

    public void setProjectileSpd(float projectileSpd) {
        this.projectileSpd = projectileSpd;
    }

    public boolean isPlayerSighted() {
        return playerSighted;
    }

    public void setPlayerSighted(boolean playerSighted) {
        this.playerSighted = playerSighted;
    }

    public boolean isPlayerInRange() {
        return playerInRange;
    }

    public void setPlayerInRange(boolean playerInRange) {
        this.playerInRange = playerInRange;
    }

    public Vector2 getPlayerPos() {
        return playerPos;
    }

    public void setPlayerPos(Vector2 playerPos) {
        this.playerPos = playerPos;
    }

    public Vector2 getPlayerVel() {
        return playerVel;
    }

    public void setPlayerVel(Vector2 playerVel) {
        this.playerVel = playerVel;
    }

    public float getAggroRange() {
        return aggroRange;
    }

    public void setAggroRange(float aggroRange) {
        this.aggroRange = aggroRange;
    }

    public boolean isStunOnHit() {
        return stunOnHit;
    }

    public void setStunOnHit(boolean stunOnHit) {
        this.stunOnHit = stunOnHit;
    }

    public int getCrystalLoot() {
        return crystalLoot;
    }

    public void setCrystalLoot(int crystalLoot) {
        this.crystalLoot = crystalLoot;
    }

    public int getComboCount() {
        return comboCount;
    }

    public void resetComboCount() {
        comboCount = 0;
    }

    public void incrementComboCount() {
        this.comboCount++;
    }

    public float getDisperseForce() {
        return disperseForce;
    }

    public void setDisperseForce(float disperseForce) {
        this.disperseForce = disperseForce;
    }

    public float getMinDst2() {
        return minDst2;
    }

    public void setMinDst2(float minDst2) {
        this.minDst2 = minDst2;
    }

    public float getMaxDst2() {
        return maxDst2;
    }

    public void setMaxDst2(float maxDst2) {
        this.maxDst2 = maxDst2;
    }
}
