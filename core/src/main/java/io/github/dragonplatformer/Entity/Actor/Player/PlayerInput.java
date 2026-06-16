package io.github.dragonplatformer.Entity.Actor.Player;

import com.badlogic.gdx.math.Vector2;

public class PlayerInput {
    public boolean leftMove = false;
    public boolean rightMove = false;
    public boolean downMove = false;
    public boolean upMove = false;
    public boolean jump = false;
    public boolean glide = false;
    public boolean guard = false;
    public boolean evadeDash = false;
    public boolean evade = false;
    public boolean useProjectile = false;
    public boolean useMelee = false;
    public boolean meleeHit = false;
    public int meleeHitCount = 0;
    public int numJumps = 4;
    public float projectileCharge = 0;
    public float breathCount = 0;
    private int direction = 1;
    private Vector2 cursor = new Vector2();

    public PlayerInput() {}

    public void update(float delta) {
        if (getUseProjectile()) projectileCharge += delta;
//        if (getProjectile() && breathCount < 10) breathCount += delta; // TODO: redo projectile logic
//        else if (breathCount > 0) breathCount -= delta;
    }

    public boolean isGliding() {
        return glide;
    }

    public void setGlide(boolean glide) {
        this.glide = glide;
    }

    /**
     * Gets the currently input horizontal direction of movement, regardless of where the player is facing.
     *
     * @return 0 if leftMove and rightMove are false, else 1 if the player is moving right or -1 if the
     * player is moving left
     */
    public int getInputDirection() {
        if (!leftMove && !rightMove) return 0;
        return direction;
    }

    public void setLeftMove(boolean leftMove) {
        this.leftMove = leftMove;
        if (leftMove) direction = -1;
        else if (rightMove) direction = 1;
    }

    public void setRightMove(boolean rightMove) {
        this.rightMove = rightMove;
        if (rightMove) direction = 1;
        else if (leftMove) direction = -1;
    }

    public void setDownMove(boolean downMove) {
        if (upMove && downMove) upMove = false;
        this.downMove = downMove;
    }

    public void setUpMove(boolean upMove) {
        if (downMove && upMove) downMove = false;
        this.upMove = upMove;
    }

    public void setJump(boolean jump) {
        this.jump = jump;
    }

    public void setGuard(boolean guard) {
        this.guard = guard;
    }

    public void setEvade(boolean evade) {
        this.evade = evade;
    }

    public boolean getEvadeDash() {
        return evadeDash;
    }

    public void setEvadeDash(boolean evadeDash) {
        this.evadeDash = evadeDash;
    }

    public void setUseProjectile(boolean isProjectileInput) {
        this.useProjectile = isProjectileInput;
    }

    public boolean getUseProjectile() {
        return useProjectile;
    }

    public void resetProjectileCharge() {
        this.projectileCharge = 0;
    }

    public float getProjectileCharge() {
        return projectileCharge;
    }

    public void setUseMelee(boolean isMeleeInput) {
        this.useMelee = isMeleeInput || this.useMelee;
    }

    public void resetMelee() {
        this.useMelee = false;
    }

    public boolean getUseMelee() {
        return useMelee;
    }

    public boolean getMeleeHit() {
        return meleeHit;
    }

    public void incrementMeleeHit() {
        meleeHitCount += 1;
        if (meleeHitCount == 1) meleeHit = true;
    }

    public void resetMeleeHit() {
        meleeHitCount = 0;
        meleeHit = false;
    }

    public Vector2 getCursor() {
        return cursor;
    }

    public void setCursor(Vector2 cursor) {
        this.cursor = cursor;
    }
}
