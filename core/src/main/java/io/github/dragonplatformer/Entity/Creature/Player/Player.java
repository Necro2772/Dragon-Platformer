package io.github.dragonplatformer.Entity.Creature.Player;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.AttackEffect.*;
import io.github.dragonplatformer.Entity.Creature.Creature;
import io.github.dragonplatformer.Entity.Creature.Enemy;
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
    private float stateTime;
    private int enemyContact;
    private Enemy enemyContactEntity;

    public Player(float x, float y, World world, GameScreen screen, AnimationManager animManager) {
        super(x, y, 2, 2, world);
        this.animManager = animManager;
        this.screen = screen;

        setAsPlayer();
        setHitboxShape(new Vector2(1f, 1f));
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
        input = new PlayerInput();
        stats = new PlayerStats();
        upgrades = new PlayerUpgrades();
        upgrades.upgrade(PlayerUpgrades.Upgrade.FIREBALL_LARGE);
        setStats(stats);
        state = PlayerState.IDLE;

        anims = animManager.getPlayerAnimations();
        enemyContact = 0;
        //getBody().setGravityScale(0.8f);

    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime = stateTime + delta;
        stats().update(delta);
        input().update(delta);
        updatePlayerState();
        updatePlayerMovement(delta);
        if (enemyContact > 0) {
            if (enemyContactEntity.getState() != Enemy.EnemyState.DEATH && enemyContactEntity.stats().getHitTimer() <= 0) {
                damage(1, enemyContactEntity.getBody().getPosition(), 5);
            }
        }
    }

    private void updatePlayerState() {
        if (getCurrentAnim().isAnimationFinished(getStateTime())) {
            if (getState() == PlayerState.DEATH) {
                death();
                setState(PlayerState.IDLE);
                return;
            } else if (getState().nextState() != null) {
                setState(getState().nextState());
            }
        }

        PlayerState nextState = PlayerState.IDLE;
        if (isGrounded()) {
            if (input().getInputDirection() != 0) nextState = PlayerState.RUNNING;
            if (input().upMove) nextState = PlayerState.JUMPING;
        } else {
            if (input().isGliding()) {
                nextState = PlayerState.GLIDING;
                if (input().upMove) nextState = PlayerState.SOAR;
            } else {
                nextState = PlayerState.FLYING;
                if (input().upMove && stats().getJumpCD() <= 0) {
                    nextState = PlayerState.JUMPING;
                    stats().resetJumpCD();
                }
            }
            if (input.downMove) nextState = PlayerState.DIVING;
        }

        if (stats().getHealth() <= 0) {
            nextState = PlayerState.DEATH;
        }

        if (!getState().isBlocking()) {
            if (input().getMelee()) {
                meleeAttack();
                input().resetMelee();
            } else if ((stats().getProjectileCD() <= 0 && input().getProjectile())
                || (input().getProjectileCharge() > 0 && !input().getProjectile())) {
                projectileAttack(input().getProjectileCharge());
                input().resetProjectileCharge();
            } else if (input().dodge) {
                nextState = PlayerState.DASH;
                input().setDodge(false);
            }
        }
        if (!getState().isBlocking() && nextState != getState()) setState(nextState);
    }

    private void meleeAttack() {
        if (getState() == PlayerState.ATTACKFORWARD) {
            setState(PlayerState.ATTACKFORWARD2);
        } else if (getState() == PlayerState.ATTACKFORWARD2) {
            setState(PlayerState.ATTACKFORWARD);
        } else if (input.downMove && !isGrounded()) {
            setState(PlayerState.ATTACKDOWN);
        } else if (input.upMove) {
            setState(PlayerState.ATTACKUP);
        } else {
            setState(PlayerState.ATTACKFORWARD);
        }
    }

    public void projectileAttack(float charge) {
        switch (upgrades.getProjectile()) {
            case FIREBALL_LARGE:
                if (charge > 0.8f) {
                    shootProjectile(PlayerUpgrades.Upgrade.FIREBALL_LARGE);
                } else if (charge > 0.4) {
                    shootProjectile(PlayerUpgrades.Upgrade.FIREBALL_MEDIUM);
                } else if (stats().getProjectileCD() <= 0){
                    shootProjectile(PlayerUpgrades.Upgrade.FIREBALL_BASIC);
                }
                stats().resetProjectileCD();
                return;
            case FIREBREATH:
                if (input().projectileCharge > 0.25f) {
                    shootProjectile(PlayerUpgrades.Upgrade.FIREBREATH);
                    //stats().resetProjectileCD(0.05f + (getInput().breathCount) / 80);
                    stats().resetProjectileCD(0.05f);
                }
                return;
            case FIREBALL_BASIC:
                stats().resetProjectileCD();
                shootProjectile(PlayerUpgrades.Upgrade.FIREBALL_BASIC);
        }
    }

    /**
     * Spawns a projectile object depending on current upgrade.
     * @param type current upgrade for the player projectile
     */
    public void shootProjectile(PlayerUpgrades.Upgrade type) {
        Projectile projectile;
        Vector2 pos = new Vector2(getBody().getPosition().x + getSpriteDirection() * 2, getBody().getPosition().y - 0.25f);
        Vector2 impulse = new Vector2(stats.projectileSpeed * getSpriteDirection(), 0);
        if (input.upMove) impulse.y = stats.projectileSpeed;
        else if (input.downMove) impulse.y = -stats.projectileSpeed;
        switch (type) {
            case FIREBREATH:
                projectile = new Firebreath(0.5f, 0f, 1f, impulse, 0.5f - input.breathCount / 30, pos.x, pos.y,
                    2f, 1, getSpriteDirection(), animManager, true, getBody().getWorld());
                impulse.rotateDeg((float) Math.random() * 60 - 30);
                impulse.scl(0.6f, 0.9f);
                break;
            case FIREBALL_LARGE:
                projectile = new ExplosiveFireball(4, 5, 5, pos.x, pos.y, 3, 1,
                    getSpriteDirection(), animManager, true, getBody().getWorld());
                recoil(new Vector2(getBody().getPosition().add(impulse)), 30);
                new ProjectileShootVisual(pos.x - getSpriteDirection(), pos.y, getSpriteDirection(), animManager, getBody().getWorld())
                    .setRotation(impulse.angleDeg());
                break;
            case FIREBALL_MEDIUM:
                projectile = new Fireball(2, 4, 2, pos.x, pos.y, 2.5f, 1,
                    getSpriteDirection(), animManager, true, getBody().getWorld());
                recoil(new Vector2(getBody().getPosition().add(impulse)), 15);
                new ProjectileShootVisual(pos.x - getSpriteDirection(), pos.y, getSpriteDirection(), animManager, getBody().getWorld())
                    .setRotation(impulse.angleDeg());
                break;
            case FIREBALL_BASIC:
            default:
                projectile = new Fireball(1, 3, 1, pos.x, pos.y, 2, 1,
                    getSpriteDirection(), animManager, true, getBody().getWorld());
        }
        projectile.getBody().applyLinearImpulse(impulse, new Vector2(0, 0), true);
        projectile.setRotation(impulse.angleDeg());
    }

    private void updatePlayerMovement(float delta) {
        Vector2 accel = new Vector2(stats().groundAccX, 0);
        Vector2 maxVel = new Vector2(stats().groundVelX, 0);
        Vector2 vel = getBody().getLinearVelocity();
        Vector2 damping = new Vector2(0, 0);
        boolean gravityEnabled = true;

        if (input.getInputDirection() != 0 && getSpriteDirection() != input.getInputDirection()
            && !getState().isBlocking()) {
            setSpriteDirection(input.getInputDirection());
        }

        switch (state) {
            case ATTACKDIVE:
            case DASHDIVE:
            case DIVING:
                accel.y = stats().diveAccY;
                maxVel.y = stats().diveVelY;
                accel.x = stats().diveAccX;
                maxVel.x = stats().diveVelX;
                gravityEnabled = false;
                stats().chargeSoar(delta);
                stats().chargeGlide(delta);
                if (stats().glideCharge > stats().glideChargeMin) input().setGlide(true);
                break;
            case SOAR:
                accel.x = stats().diveAccX;
                accel.y = stats().soarAccY;
                maxVel.x = stats().diveVelX;
                maxVel.y = stats().soarVelY;
                gravityEnabled = false;
                if (vel.y >= 0) stats().chargeSoar(-delta);
                if (stats().soarCharge <= 0) stats().chargeGlide(-delta);
                break;
            case GLIDING:
                accel.x = stats().glideAccX;
                accel.y = stats().glideAccY;
                maxVel.x = stats().glideVelX;
                maxVel.y = stats().glideVelY;
                gravityEnabled = false;
                if (Math.abs(vel.x) < stats().glideVelX * 0.8f || input().getInputDirection() == 0)
                    stats().chargeGlide(-delta * 2);
                break;
            case DASH:
                maxVel.x = stats().dashVelX;
                accel.x = stats().dashAccX;
                gravityEnabled = false;
                break;
            case JUMPING:
                maxVel.x = stats().flyVelX;
                accel.x = stats().jumpAccX;
                break;
            case FLYING:
                accel.x = stats().flyAccX;
                maxVel.x = stats().flyVelX;
                break;
        }

        if (isGrounded() || (input.isGliding() && stats().glideCharge <= 0)) {
            input().setGlide(false);
            stats().glideCharge = 0;
        }

        // Input based movement
        applyClampedForce(new Vector2(accel.x * input().getInputDirection(), accel.y),
            new Vector2(maxVel).scl(-1), maxVel);

        // Friction / damping
        if (vel.x != 0 && input().getInputDirection() == 0) {
            if (Math.abs(vel.x) > 0.5f) {
                if (isGrounded()) {
                    applyWeightedForce(getMoveDirection() * -accel.x, 0);
                } else {
                    applyWeightedForce(getMoveDirection() * -accel.x / 10, 0);
                }
            } else {
                getBody().setLinearVelocity(0, vel.y);
            }
        } else {
            applyWeightedForce(getMoveDirection() * -5, 0);
        }
        if (!gravityEnabled) applyWeightedForce(0, getBody().getWorld().getGravity().y);
    }

    /**
     * Transitions from current to new state without performing any safety checks.
     * @param state to transition to
     */
    public void setState(PlayerState state) {
        endState();
        this.state = state;
        stateTime = 0;
        if (input.getInputDirection() != 0 && getSpriteDirection() != input.getInputDirection())
            setSpriteDirection(input.getInputDirection());
        beginState();
    }

    private void beginState() {
        Vector2 vel = getBody().getLinearVelocity();
        Vector2 pos = getBody().getPosition();
        input.resetMeleeHit();
        switch(state) {
            case JUMPING:
                float jumpForce = stats().jumpImpulseAir;
                if (input().numJumps == stats().getMaxJumps()) jumpForce = stats().jumpImpulseGround;
                applyWeightedImpulse(0, jumpForce - vel.y * 0.8f);
                input().numJumps--;
                break;
            case GLIDING:
                if (stats().glideCharge > stats().glideChargeMin) {
                    applyWeightedImpulse(new Vector2(
                        input().getInputDirection() * stats().glideImpulseX,
                        stats().glideImpulseY
                    ));
                }
                stats().glideCharge = stats().glideChargeBase;
                break;
            case DASHDIVE:
                new Claw(1, 3, 6, 4.5f, new Vector2(0, -2),
                    getSpriteDirection(), animManager, getBody());
                applyWeightedImpulse(-vel.x * 0.75f, -25 - vel.y);
                break;
            case DASH:
                new ProjectileShootVisual(pos.x, pos.y, getSpriteDirection(), animManager, getBody().getWorld());
                applyWeightedImpulse(getSpriteDirection() * stats().dashImpulse - vel.x, -vel.y * 0.5f);
                break;
            case SOAR:
                stats().chargeSoar(-0.1f);
                break;
            case ATTACKFORWARD:
                new Claw(1, 1, 4, 4, new Vector2(3f * getSpriteDirection(), 0),
                    getSpriteDirection(), animManager, getBody());
                break;
            case ATTACKFORWARD2:
                new Claw(1, 1, 4, 4, new Vector2(3f * getSpriteDirection(), 0),
                    -getSpriteDirection(), animManager, getBody());
                break;
            case ATTACKFORWARD3:
                new Slash(1, 4, 5, 5, new Vector2(4f * getSpriteDirection(), 0),
                    getSpriteDirection(), animManager, getBody());
                break;
            case ATTACKDOWN:
                new Claw(1, 3, 5, 3, new Vector2(0.5f * getSpriteDirection(), -2),
                    getSpriteDirection(), animManager, getBody());
                break;
            case ATTACKUP:
                new Claw(1, 3, 6, 4.5f, new Vector2(0, 2),
                    getSpriteDirection(), animManager, getBody());
                break;
//            case ATTACKJUMP:
//                new Claw(1, 3, 3, 3, new Vector2(2f * getDirection(), 0),
//                    getDirection(), animManager, getBody());
//                break;
//            case ATTACKGLIDE:
//                new Claw(1, 3, 3, 3, new Vector2(2f * getDirection(), -2),
//                    getDirection(), animManager, getBody());
//                break;
        }
    }

    private void endState() {
        Vector2 vel = getBody().getLinearVelocity();
        if (getState() == PlayerState.DASH) {
            getBody().setLinearVelocity(MathUtils.clamp(vel.x, -5, 5), vel.y);
        }
    }

    public void meleeHitEffect() {
        if (input.meleeHit == 0) {
            Vector2 vel = getBody().getLinearVelocity();
            switch (getState()) {
                case ATTACKDOWN:
                    //getBody().applyLinearImpulse(0, 10 - vel.y, pos.x, pos.y, true);
                    break;
                case ATTACKFORWARD:
                case ATTACKFORWARD2:
                    applyWeightedImpulse(-getSpriteDirection() * 0.75f, 0);
                    break;
                case ATTACKFORWARD3:
                    applyWeightedImpulse(-getSpriteDirection() * 2, 0);
                    break;
//                case DASH:
//                    applyWeightedImpulse(-vel.x - getDirection(), -vel.y);
//                case ATTACKGLIDE:
//                    getBody().applyLinearImpulse(getDirection() * 5, 20 - vel.y * 2, pos.x, pos.y, true);
//                    break;
            }
        }
        input.meleeHit++;
    }

    public void recoil(Vector2 attackOrigin, float intensity) {
        getBody().applyLinearImpulse(new Vector2(attackOrigin).sub(getBody().getPosition()).nor().scl(-intensity)
            .sub(new Vector2(getBody().getLinearVelocity()).scl(getBody().getMass(), 0.3f)), getBody().getPosition(), true);
    }

    @Override
    public boolean isGrounded() {
        return super.isGrounded() && getState() != PlayerState.JUMPING;
    }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        super.beginContact(entityFixture, contactFixture);
        if (getBody().getFixtureList().indexOf(entityFixture, true) == getJumpSensorIndex()) {
            input().numJumps = stats().getMaxJumps();
        } else if (getBody().getFixtureList().indexOf(entityFixture, true) == getHitboxIndex()) {
            if (contactFixture.getUserData() instanceof Enemy) {
                enemyContact++;
                Enemy e = (Enemy) contactFixture.getUserData();
                enemyContactEntity = e;
                if (e.getState() != Enemy.EnemyState.DEATH && e.stats().getHitTimer() <= 0) {
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

    public void loot(Loot item) {
        if (item.type == Loot.LootType.CRYSTAL) {
            stats.addCrystals(item.value);
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
        TextureRegion frame = anims.get(getState()).getKeyFrame(getStateTime());
        batch.draw(frame,
            this.getBody().getPosition().x - getWidth() / 2f,
            this.getBody().getPosition().y - getHeight() / 2f,
            getWidth() / 2f, getHeight() / 2f, getWidth(), getHeight(), getSpriteDirection(), 1, 0);
    }

    @Override
    public boolean damage(float attackDamage, Vector2 attackOrigin, float knockback) {
        if (super.damage(attackDamage, attackOrigin, 20)) {
            stats.setInvulnerability(1.5f);
            return true;
        }
        return false;
    }

    @Override
    public void death() {
        screen.gameOver();
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

    public PlayerInput input() {
        return input;
    }

    public PlayerStats stats() {
        return stats;
    }
}
