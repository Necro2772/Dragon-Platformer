package io.github.dragonplatformer.Entity.Creature;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.AttackEffect.*;
import io.github.dragonplatformer.Entity.Loot.Loot;
import io.github.dragonplatformer.GameContactListener;
import io.github.dragonplatformer.GameScreen;

import java.util.Map;

public class Player extends Creature {
    private final GameScreen screen;
    private final AnimationManager animManager;
    private final Map<PlayerState, Animation<TextureRegion>> anims;
    public final PlayerInput input;
    private final PlayerStats stats;
    private final PlayerUpgrades upgrades;
    private final int itemPickupFixtureIndex;
    private PlayerState state;
    private PlayerState bufferedState;
    private int enemyContact;
    private Enemy enemyContactEntity;
    private float stateTime;

    public Player(float x, float y, World world, GameScreen screen, AnimationManager animManager) {
        super(x, y, 2, 2, world);
        this.animManager = animManager;
        this.screen = screen;

        setAsPlayer();
        setHitboxShape(new Vector2(1f, 1f));
        setDensity(0.225f * 2.25f);
        init();

        FixtureDef itemPickupDef = new FixtureDef();
        itemPickupDef.isSensor = true;
        CircleShape itemPickupShape = new CircleShape();
        itemPickupShape.setRadius(15);
        itemPickupDef.shape = itemPickupShape;
        Fixture itemPickupFixture = getBody().createFixture(itemPickupDef);
        itemPickupFixture.setUserData(this);
        itemPickupFixture.getFilterData().categoryBits = GameContactListener.FilterBits.SENSOR.getBit();
        itemPickupFixture.getFilterData().maskBits = GameContactListener.FilterBits.LOOT.getBit();
        itemPickupFixtureIndex = getBody().getFixtureList().indexOf(itemPickupFixture, true);
        itemPickupShape.dispose();

        stateTime = 0;
        input = new PlayerInput(this);
        stats = new PlayerStats();
        upgrades = new PlayerUpgrades();
        upgrades.upgrade(Upgrade.FIREBALL_LARGE);
        setStats(stats);
        state = PlayerState.IDLE;

        anims = animManager.getPlayerAnimations();
        enemyContact = 0;
        //getBody().setGravityScale(0.8f);

    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (stats().getHealth() <= 0) {
            death();
            return;
        }
        stats().updateCooldowns(delta);
        getInput().updateTimers(delta);
        int direction = input.getInputDirection();
        if (direction != 0 && getDirection() != direction && getState().canRotate) setDirection(direction);
        updatePlayerState();
        updatePlayerMovement(delta);
        if (stats().getProjectileCD() <= 0 && getInput().getProjectile()) projectileStart();
        if (enemyContact > 0){
            if (enemyContactEntity.getState() != Enemy.EnemyState.DEATH && enemyContactEntity.stats.getHitTimer() <= 0) {
                damage(1, enemyContactEntity.getBody().getPosition(), 5);
            }
        }
    }

    private void updatePlayerState() {
        PlayerState nextState = PlayerState.IDLE;
        if (getInput().glide && !isGrounded()) {
            if (getInput().downMove) {
                nextState = PlayerState.DIVING;
            } else if (getInput().upMove && getInput().soarCharge > 0) {
                nextState = PlayerState.DIVESOAR;
            } else {
                nextState = PlayerState.GLIDING;
            }
        } else if (isGrounded()){
            getInput().soarCharge = 0;
            getInput().glideCharge = 0;
        }
        if (getInput().jump && getInput().numJumps > 0 && !getState().isJump) {
            if (input.glide) {
                if (getState() == PlayerState.DIVING) {
                    nextState = PlayerState.DASHDIVE;
                } else if (getInput().getInputDirection() != 0 && !getInput().upMove) {
                    nextState = PlayerState.DASH;
                } else nextState = PlayerState.JUMPING;
            } else nextState = PlayerState.JUMPING;
            getInput().numJumps--;
            input.setJump(false);
        }
        if (nextState == PlayerState.IDLE) {
            if (isGrounded() && getState() != PlayerState.JUMPING) {
                if (getInput().rightMove || getInput().leftMove) nextState = PlayerState.RUNNING;
            } else {
                nextState = PlayerState.FLYING;
            }
        }
        setState(nextState);
    }

    private void updatePlayerMovement(float delta) {
        float maxVelocity = 12;
        //float glideVelocity = 11;
        float diveGlideVelocity = 50;
        Vector2 vel = getBody().getLinearVelocity();
        Vector2 pos = getBody().getPosition();
        float accel = 1.5f;

        switch (state) {
            case DIVING:
                if (vel.y < 0) {
                    getInput().chargeSoar(delta);
                    getInput().chargeGlide(delta);
                }
                if (vel.y > -25) {
                    getBody().applyLinearImpulse(0, -3f, pos.x, pos.y, true);
                } else {
                    getBody().applyForceToCenter(0, 9.8f, true);
                }
                break;
            case DIVESOAR:
                getInput().chargeSoar(-delta);
                getInput().chargeGlide(-delta);
                if (getInput().soarCharge > 0.5f) {
                    if (vel.y < 25) {
                        getBody().applyLinearImpulse(0, 5, pos.x, pos.y, true);
                    }
                } else if (getInput().soarCharge > 0.1f) {
                    if (vel.y < 20) {
                        getBody().applyLinearImpulse(0, 4, pos.x, pos.y, true);
                    }
                }
                break;
            case GLIDING:
                if (getInput().glideCharge > 0.1f) {
                    if (Math.abs(vel.x) < diveGlideVelocity && (getInput().leftMove || getInput().rightMove)) {
                        getBody().applyLinearImpulse(
                            getDirection() * Math.max(getInput().glideCharge, 0.7f) * 4,
                            Math.max(getInput().glideCharge, 0.7f) * 2,
                            pos.x, pos.y, true);
                    }
                }
                getInput().chargeGlide(getInput().glideCharge * delta * -10);
                getInput().chargeSoar(delta * -0.1f);
                if (vel.y < -1 && Math.abs(vel.x) > 0.7f) {
                    getBody().applyLinearImpulse(0, -vel.y / 6, pos.x, pos.y, true);
                }
                break;
            case DASH:
                if (Math.abs(vel.x) > 20) getBody().applyLinearImpulse(getDirection() * -2, 0, pos.x, pos.y, true);
                break;
            case JUMPING:
            case FLYING:
                if (vel.y > 0 && input.downMove) getBody().applyForceToCenter(0, -20, true);
                else if (vel.y > 0) getBody().applyForceToCenter(0, -10, true);
                break;
        }
        if (Math.abs(vel.x) > maxVelocity * 1.5f) getBody().applyLinearImpulse(getDirection() * -2, 0, pos.x, pos.y, true);
        else if (Math.abs(vel.x) > maxVelocity) getBody().applyForceToCenter(getDirection() * -10, 0, true);
        if (vel.y > 18) getBody().applyLinearImpulse(0, -4, pos.x, pos.y, true);

        if (state.isAttack()) { // Slowdown during attacks, otherwise normal left/right movement
            if (isGrounded()) getBody().applyLinearImpulse(-vel.x / 10, 0, pos.x, pos.y, true);
        } else if (getInput().getInputDirection() == 1 && vel.x < maxVelocity) {
            getBody().applyLinearImpulse(accel, 0, pos.x, pos.y, true);
        } else if (getInput().getInputDirection() == -1 && vel.x > -maxVelocity) {
            getBody().applyLinearImpulse(-accel, 0, pos.x, pos.y, true);
        } else if ((getInput().getInputDirection() == 0 && vel.x != 0) || (Math.abs(vel.x) > maxVelocity && isGrounded())) {
            getBody().applyLinearImpulse(-vel.x / 20, 0, pos.x, pos.y, true);
        }
        // Idle ground slowdown
        if (isGrounded() && Math.abs(vel.x) > 1 && getInput().getInputDirection() == 0) {
            getBody().applyLinearImpulse(-vel.x / 6, 0, pos.x, pos.y, true);
        }
        if (getInput().jump) input.setJump(false);
    }

    private void endState() {
        Vector2 pos = getBody().getPosition();
        if (getState() == PlayerState.DIVESOAR) {
            input.chargeSoar(-0.1f);
        }
    }

    private void beginState() {
        Vector2 vel = getBody().getLinearVelocity();
        Vector2 pos = getBody().getPosition();
        switch(state) {
            case JUMPING:
//                if (isGrounded() && (input.upMove || getInput().glide))
//                    getBody().applyLinearImpulse(0, 1.3f * stats().jumpForce - vel.y * 1.8f, pos.x, pos.y, true);
                float jumpForce = 50;
                if (input.downMove)
                    getBody().applyLinearImpulse(0, 0.7f * jumpForce - vel.y * 1.8f, pos.x, pos.y, true);
                else getBody().applyLinearImpulse(0, jumpForce - vel.y * 1.8f, pos.x, pos.y, true);
                break;
            case DASHDIVE:
                getBody().applyLinearImpulse(0, -40 - vel.y, pos.x, pos.y, true);
                break;
            case DASH:
                new ProjectileShootVisual(pos.x, pos.y, getDirection(), animManager, getBody().getWorld());
                getBody().applyLinearImpulse(getDirection() * 80 - vel.x * 1.8f, 10, pos.x, pos.y, true);
                break;
            case ATTACKUP:
                new Claw(1, 3, 4, 4, new Vector2(2f * getDirection(), 2),
                    getDirection(), animManager, getBody());
                break;
            case ATTACKFORWARD:
                new Claw(1, 1, 4, 4, new Vector2(3f * getDirection(), 0),
                    getDirection(), animManager, getBody());
                break;
            case ATTACKFORWARD2:
                new Claw(1, 1, 4, 4, new Vector2(3f * getDirection(), 0),
                    -getDirection(), animManager, getBody());
                break;
            case ATTACKFORWARD3:
                new Slash(1, 4, 5, 5, new Vector2(4f * getDirection(), 0),
                    getDirection(), animManager, getBody());
                break;
            case ATTACKDOWN:
                new Claw(1, 3, 4, 4, new Vector2(2f * getDirection(), -2),
                    getDirection(), animManager, getBody());
                break;
        }
    }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        super.beginContact(entityFixture, contactFixture);
        if (getBody().getFixtureList().indexOf(entityFixture, true) == getJumpSensorIndex()) {
            getInput().numJumps = stats().getMaxJumps();
        } else if (getBody().getFixtureList().indexOf(entityFixture, true) == getHitboxIndex()) {
            if (contactFixture.getUserData() instanceof Enemy) {
                enemyContact++;
                Enemy e = (Enemy) contactFixture.getUserData();
                enemyContactEntity = e;
                if (e.getState() != Enemy.EnemyState.DEATH && e.stats.getHitTimer() <= 0) {
                    damage(1, e.getBody().getPosition(), 5);
                }
            } else if (contactFixture.getUserData() instanceof Loot) {
                Loot loot = (Loot) contactFixture.getUserData();
                if (!loot.isLooted()) {
                    loot(loot);
                    loot.setLooted();
                }
            }
        } else if (getBody().getFixtureList().indexOf(entityFixture, true) == itemPickupFixtureIndex) {
            Loot item = (Loot) contactFixture.getUserData();
            item.moveToPlayer(getBody().getPosition());
        }
    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {
        super.endContact(entityFixture, contactFixture);
        if (getBody().getFixtureList().indexOf(entityFixture, true) == itemPickupFixtureIndex) {
            Loot item = (Loot) contactFixture.getUserData();
            item.stopMove();
        } else if (getBody().getFixtureList().indexOf(entityFixture, true) == getHitboxIndex()) {
            if (contactFixture.getUserData() instanceof Enemy) {
                enemyContact--;
            }
        }
    }

    @Override
    public void draw(SpriteBatch batch, float delta) {
        stateTime = stateTime + delta;
        if (anims.get(getState()).isAnimationFinished(getStateTime())) {
            if (getState() == PlayerState.DEATH) {
                death();
                return;
            } else if (bufferedState != null) {
                PlayerState nextState = bufferedState;
                bufferedState = null;
                setState(nextState);
            }
        }
        TextureRegion frame = anims.get(getState()).getKeyFrame(getStateTime());
        batch.draw(frame,
            this.getBody().getPosition().x - getWidth() / 2f,
            this.getBody().getPosition().y - getHeight() / 2f,
            getWidth() / 2f, getHeight() / 2f, getWidth(), getHeight(), getDirection(), 1, 0);
    }

    @Override
    public boolean damage(float attackDamage, Vector2 attackOrigin, float knockback) {
        if (super.damage(attackDamage, attackOrigin, 20)) {
            stats.setInvulnerability(1.5f);
            return true;
        }
        return false;
    }

    public void meleeHitEffect() {
        input.meleeHit++;
        if (input.meleeHit == 0) {

        }
    }

    public void recoil(Vector2 attackOrigin, float intensity) {
        getBody().applyLinearImpulse(new Vector2(attackOrigin).sub(getBody().getPosition()).nor().scl(-intensity)
            .sub(new Vector2(getBody().getLinearVelocity()).scl(getBody().getMass(), 0.3f)), getBody().getPosition(), true);
    }

    @Override
    public void death() {
        screen.gameOver();
    }

    public void setState(PlayerState state) {
        if (state == this.getState()) return;
        if (bufferedState != null) {
            switch (this.state) {
                case ATTACKFORWARD:
                    if (state == PlayerState.ATTACKFORWARD2) bufferedState = state;
                case ATTACKFORWARD2:
                    if (state == PlayerState.ATTACKFORWARD3) bufferedState = state;
                case ATTACKFORWARD3:
                case ATTACKDOWN:
                case ATTACKUP:
                    if (state == PlayerState.JUMPING) bufferedState = state;
                    break;
                case JUMPING:
                    if (state.isAttack()) bufferedState = state;
            }
            return;
        }

        switch (state) {
            case JUMPING:
                bufferedState = PlayerState.FLYING;
                break;
            case DASHDIVE:
                bufferedState = PlayerState.DIVING;
                break;
            case DASH:
                bufferedState = PlayerState.GLIDING;
                break;
            case ATTACKDOWN:
            case ATTACKFORWARD:
            case ATTACKFORWARD2:
            case ATTACKFORWARD3:
            case ATTACKUP:
                if (isGrounded()) bufferedState = PlayerState.IDLE;
                else bufferedState = PlayerState.FLYING;
        }

        endState();
        this.state = state;
        stateTime = 0;
        beginState();
    }

    public PlayerState getState() {
        return state;
    }

    public float getStateTime() {
        return stateTime;
    }

    public Animation<TextureRegion> getCurrentAnim() {
        return anims.get(getState());
    }

    public PlayerInput getInput() {
        return input;
    }

    public PlayerStats stats() {
        return stats;
    }

    public void meleeAttack() {
        if (getState() == PlayerState.ATTACKFORWARD) {
            setState(PlayerState.ATTACKFORWARD2);
        } else if (getState() == PlayerState.ATTACKFORWARD2) {
            setState(PlayerState.ATTACKFORWARD3);
        } else if (input.downMove && !isGrounded()) {
            setState(PlayerState.ATTACKDOWN);
        } else if (input.upMove) {
            setState(PlayerState.ATTACKUP);
        } else {
            setState(PlayerState.ATTACKFORWARD);
        }
    }

    public void shootProjectile(Upgrade type) {
        Projectile projectile;
        Vector2 pos = new Vector2(getBody().getPosition().x + getDirection() * 2, getBody().getPosition().y - 0.25f);
        Vector2 impulse = new Vector2(stats.projectileSpeed * getDirection(), 0);
        if (input.upMove) impulse.y = stats.projectileSpeed * 0.5f;
        else if (input.downMove) impulse.y = -stats.projectileSpeed * 0.5f;
        switch (type) {
            case FIREBREATH:
                projectile = new Firebreath(0.5f, 0f, 1f, impulse, 0.5f - input.breathCount / 30, pos.x, pos.y,
                    2f, 1, getDirection(), animManager, true, getBody().getWorld());
                impulse.rotateDeg((float) Math.random() * 40 - 20);
                impulse.scl(0.6f, 0.9f);
                break;
            case FIREBALL_LARGE:
                projectile = new Fireball(4, 5, 5, pos.x, pos.y, 3, 1,
                    getDirection(), animManager, true, getBody().getWorld());
                recoil(new Vector2(getBody().getPosition().add(impulse)), 30);
                new ProjectileShootVisual(pos.x - getDirection(), pos.y, getDirection(), animManager, getBody().getWorld())
                    .setRotation(impulse.angleDeg());
                break;
            case FIREBALL_MEDIUM:
                projectile = new Fireball(2, 4, 2, pos.x, pos.y, 2.5f, 1,
                    getDirection(), animManager, true, getBody().getWorld());
                recoil(new Vector2(getBody().getPosition().add(impulse)), 15);
                new ProjectileShootVisual(pos.x - getDirection(), pos.y, getDirection(), animManager, getBody().getWorld())
                    .setRotation(impulse.angleDeg());
                break;
            case FIREBALL_BASIC:
            default:
                projectile = new Fireball(1, 3, 1, pos.x, pos.y, 2, 1,
                    getDirection(), animManager, true, getBody().getWorld());
        }
        projectile.getBody().applyLinearImpulse(impulse, new Vector2(0, 0), true);
        projectile.setRotation(impulse.angleDeg());
    }

    public void projectileStart() { // TODO: test out preventing diving / soaring while using projectiles?
        switch (upgrades.projectile) {
            case FIREBALL_LARGE:
                return;
            case FIREBREATH:
                if (getInput().projectileCharge > 0.25f) {
                    shootProjectile(Upgrade.FIREBREATH);
                    stats().resetProjectileCD(0.05f + (getInput().breathCount) / 80);
                }
                return;
            case FIREBALL_BASIC:
                stats().resetProjectileCD();
                shootProjectile(Upgrade.FIREBALL_BASIC);

        }
    }

    public void projectileRelease(float charge) {
        if (stats().projectileCD > 0) return;
        switch (upgrades.projectile) {
            case FIREBREATH:
            case FIREBALL_LARGE:
                if (charge > 1) {
                    shootProjectile(Upgrade.FIREBALL_LARGE);
                } else if (charge > 0.5) {
                    shootProjectile(Upgrade.FIREBALL_MEDIUM);
                } else {
                    shootProjectile(Upgrade.FIREBALL_BASIC);
                }
                stats().resetProjectileCD();
        }
    }

    public void loot(Loot item) {
        if (item.type == Loot.LootType.CRYSTAL) {
            stats.addCrystals(item.value);
        }
    }

    public enum PlayerState {
        IDLE,
        RUNNING,
        JUMPING (false, true),
        FLYING,
        GLIDING,
        DIVING,
        DIVESOAR,
        DASH (false, true),
        DASHDIVE (false, true),
        ATTACKFORWARD (true),
        ATTACKFORWARD2 (true),
        ATTACKFORWARD3 (true),
        ATTACKUP (true),
        ATTACKDOWN (true),
        DEATH;

        private final boolean canRotate;
        private final boolean isAttack;
        private final boolean isJump;

        PlayerState() {
            this.canRotate = true;
            this.isAttack = false;
            this.isJump = false;
        }

        PlayerState(boolean isAttack) {
            this.canRotate = !isAttack;
            this.isAttack = isAttack;
            this.isJump = false;
        }

        PlayerState(boolean isAttack, boolean isJump) {
            this.canRotate = !isAttack && !isJump;
            this.isAttack = isAttack;
            this.isJump = isJump;
        }

        public boolean isAttack() {
            return isAttack;
        }
    }

    public enum Upgrade {
        FIREBALL_BASIC,
        FIREBALL_MEDIUM,
        FIREBALL_LARGE,
        FIREBREATH
    }

    public static class PlayerUpgrades {
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
    }

    public static class PlayerInput {
        private final Player player;
        public boolean leftMove;
        public boolean rightMove;
        public boolean downMove;
        public boolean upMove;
        public boolean jump;
        public boolean glide;
        public boolean projectile;
        public int numJumps;
        public float soarCharge;
        public float glideCharge;
        public float projectileCharge;
        public float breathCount;
        private int direction;
        public int meleeHit;

        public PlayerInput(Player player) {
            this.player = player;
            leftMove = false;
            rightMove = false;
            downMove = false;
            upMove = false;
            jump = false;
            glide = false;
            projectile = false;

            numJumps = 1;
            soarCharge = 0;
            glideCharge = 0;
            projectileCharge = 0;
            breathCount = 0;
        }

        public void updateTimers(float delta) {
            if (getProjectile()) projectileCharge += delta;
            if (getProjectile() && breathCount < 10) breathCount += delta;
            else if (breathCount > 0) breathCount -= delta;
        }

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

        public void setGlide(boolean glide) {
            this.glide = glide;
        }

        public void setProjectile(boolean isProjectileInput) {
            this.projectile = isProjectileInput;
            if (projectile) {
                projectileCharge = 0;
            } else {
                player.projectileRelease(projectileCharge);
            }
        }

        public boolean getProjectile() {
            return projectile;
        }

        public void chargeSoar(float charge) {
            this.soarCharge += charge;
            if (this.soarCharge < 0) soarCharge = 0;
        }

        public void chargeGlide(float charge) {
            this.glideCharge += charge;
            if (this.glideCharge < 0) glideCharge = 0;
        }

        public int getMeleeHit() {
            return this.meleeHit;
        }

        public void resetMeleeHit() {
            this.meleeHit = 0;
        }
    }

    public static class PlayerStats extends CreatureStats {
        private int maxJumps;
        private float projectileMaxCD = 0.7f;
        private float projectileCD;
        public float projectileSpeed = 25;
        //public float jumpForce = 30;
        private int crystals;

        public PlayerStats() {
            super(20);
            setMaxJumps(4);
            setProjectileMaxCD(0.5f);
            setProjectileCD(0);
            crystals = 0;
        }

        @Override
        public void updateCooldowns(float delta) {
            super.updateCooldowns(delta);
            if (getProjectileCD() > 0) setProjectileCD(getProjectileCD() - delta);
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

    }
}
