package io.github.dragonplatformer.Entity.Creature.Player;

public class PlayerInput {
    public boolean leftMove;
    public boolean rightMove;
    public boolean downMove;
    public boolean upMove;
    public boolean jump;
    public boolean guard;
    public boolean dodge;
    public boolean projectile;
    public boolean melee;
    public boolean glide;
    public int numJumps;
    public float projectileCharge;
    public float breathCount;
    private int direction;
    public int meleeHit;

    public PlayerInput() {
        leftMove = false;
        rightMove = false;
        downMove = false;
        upMove = false;
        jump = false;
        guard = false;
        dodge = false;
        projectile = false;
        glide = false;

        numJumps = 4;
        projectileCharge = 0;
        breathCount = 0;
    }

    public void update(float delta) {
        if (getProjectile()) projectileCharge += delta;
        if (getProjectile() && breathCount < 10) breathCount += delta; // TODO: redo projectile logic
        else if (breathCount > 0) breathCount -= delta;
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

    public void setDodge(boolean dodge) {
        this.dodge = dodge;
    }

    public void setProjectile(boolean isProjectileInput) {
        this.projectile = isProjectileInput;
    }

    public boolean getProjectile() {
        return projectile;
    }

    public void resetProjectileCharge() {
        this.projectileCharge = 0;
    }

    public float getProjectileCharge() {
        return projectileCharge;
    }

    public void setMelee(boolean isMeleeInput) {
        this.melee = isMeleeInput || this.melee;
    }

    public void resetMelee() {
        this.melee = false;
    }

    public boolean getMelee() {
        return melee;
    }

    public int getMeleeHit() {
        return this.meleeHit;
    }

    public void resetMeleeHit() {
        this.meleeHit = 0;
    }
}
