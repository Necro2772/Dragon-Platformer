package io.github.dragonplatformer.Entity.Actor.Player;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.AnimationEvent;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Actor.Actor;
import io.github.dragonplatformer.Entity.Actor.Enemy.Enemy;
import io.github.dragonplatformer.Entity.Actor.Enemy.EnemyState;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.*;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack.Claw;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile.ExplosiveFireball;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile.Fireball;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile.Firebreath;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile.Projectile;
import io.github.dragonplatformer.Entity.Effect.Loot.Loot;
import io.github.dragonplatformer.Entity.Effect.Loot.LootType;
import io.github.dragonplatformer.GameContactListener;
import io.github.dragonplatformer.GameScreen;

public class Player extends Actor<PlayerState> {
    private final GameScreen screen;
    public final PlayerInput input;
    private final PlayerUpgrades upgrades;
    private final int itemPickupFixtureIndex;
    private int enemyContact;
    private Enemy enemyContactEntity;

    public Player(float x, float y, World world, GameScreen screen, AnimationManager animManager) {
        super(x, y, 2, 2, animManager.getPlayerAnimations(), animManager.getPlayerAnimEvents(),
            animManager, world);
        this.screen = screen;

        setAsPlayer();
        setHitboxShapeCircle(0.7f);
        setMass(2f);
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

        input = new PlayerInput();
        setStats(new PlayerStats());
        upgrades = new PlayerUpgrades();
        upgrades.upgrade(PlayerUpgrades.Upgrade.FIREBALL_LARGE);
        state = PlayerState.IDLE;

        enemyContact = 0;
        //getBody().setGravityScale(0.8f);

    }

    @Override
    public void act(float delta) {
        super.act(delta);
        input().update(delta);
        updatePlayerState();
        updatePlayerMovement(delta);
        updateAnimationFlags(delta);
        if (input().meleeHit) {
            meleeHitEffect();
            input().resetMeleeHit();
        }
        if (enemyContact > 0) {
            if (enemyContactEntity.getState() != EnemyState.DEATH && enemyContactEntity.stats().getHitTimer() <= 0) {
                damage(1, enemyContactEntity.getBody().getPosition(), 5);
            }
        }
    }

    private void updatePlayerState() {
        if (getState().isNonBlocking() || getCurrentAnim().isAnimationFinished(getStateTime())) {
            if (input().getMelee()) {
                meleeAttack();
                input().resetMelee();
                return;
            } else if ((stats().getProjectileCD() <= 0 && input().getProjectile())
                || (input().getProjectileCharge() > 0 && !input().getProjectile())) {
                projectileAttack(input().getProjectileCharge());
                input().resetProjectileCharge();
                return;
            } else if (input().evade) {
                if (input().upMove) {
                    setState(PlayerState.EVADE_UP);
                } else if (input().downMove) {
                    setState(PlayerState.EVADE_DOWN);
                } else {
                    setState(PlayerState.EVADE);
                }
                input().setEvade(false);
                return;
            }
        }

        if (getCurrentAnim().isAnimationFinished(getStateTime())) {
             if (getState() == PlayerState.DEATH) {
                death();
                setState(PlayerState.IDLE);
                return;
            } else if (getState().nextState() != null) {
                setState(getState().nextState());
            }
        }

        if (input().getEvadeDash()) {
            input().setEvadeDash(false);
            switch (getState()) {
                case EVADE:
                    setState(PlayerState.EVADE_DASH);
                    return;
                case EVADE_DOWN:
                    setState(PlayerState.EVADE_DASH_DOWN);
                    return;
                case EVADE_UP:
                    setState(PlayerState.EVADE_DASH_UP);
                    return;
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
                }
            }
            if (input.downMove) nextState = PlayerState.DIVING;
        }

        if (stats().getHealth() <= 0) {
            nextState = PlayerState.DEATH;
        }

        if (getState().isNonBlocking() && nextState != getState()) setState(nextState);
    }

    private void meleeAttack() {
        if (getState() == PlayerState.ATTACK_FORWARD1) {
            setState(PlayerState.ATTACK_FORWARD2);
        } else if (isGrounded() && getState() == PlayerState.ATTACK_GROUND1) {
            setState(PlayerState.ATTACK_GROUND2);
        } else if (input.downMove && !isGrounded()) {
            if (input().isGliding()) setState(PlayerState.ATTACK_DIVE);
            else setState(PlayerState.ATTACK_DOWN);
        } else if (input.upMove) {
            if (input().isGliding()) setState(PlayerState.ATTACK_SOAR);
            else setState(PlayerState.ATTACK_UP);
        } else {
            if (isGrounded()) setState(PlayerState.ATTACK_GROUND1);
            else if (input().isGliding()) setState(PlayerState.ATTACK_GLIDE);
            else setState(PlayerState.ATTACK_FORWARD1);
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
        Vector2 impulse = new Vector2(stats().projectileSpeed * getSpriteDirection(), 0);
        if (input.upMove) impulse.y = stats().projectileSpeed;
        else if (input.downMove) impulse.y = -stats().projectileSpeed;
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
                //recoil(new Vector2(getBody().getPosition().add(impulse)), 30);
                new ProjectileShootVisual(pos.x - getSpriteDirection(), pos.y, getSpriteDirection(), animManager, getBody().getWorld())
                    .setRotation(impulse.angleDeg());
                break;
            case FIREBALL_MEDIUM:
                projectile = new Fireball(2, 4, 2, pos.x, pos.y, 2.5f, 1,
                    getSpriteDirection(), animManager, true, getBody().getWorld());
                //recoil(new Vector2(getBody().getPosition().add(impulse)), 15);
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
            && getState().isNonBlocking()) {
            setSpriteDirection(input.getInputDirection());
        }

        switch (getState()) {
            case DIVING:
                maxVel.set(stats().diveVelX, stats().diveVelY);
                accel.set(stats().diveAccX, stats().diveAccY);
                gravityEnabled = false;
                stats().chargeSoar(delta);
                stats().chargeGlide(delta);
                if (stats().glideCharge > stats().glideChargeMin) input().setGlide(true);
                break;
            case SOAR:
                maxVel.set(stats().diveVelX, stats().soarVelY);
                accel.set(stats().diveAccX, stats().soarAccY);
                gravityEnabled = false;
                if (vel.y >= 0) stats().chargeSoar(-delta);
                if (stats().soarCharge <= 0) stats().chargeGlide(-delta);
                break;
            case GLIDING:
                maxVel.set(stats().glideVelX, stats().glideVelY);
                accel.set(stats().glideAccX, stats().glideAccY);
                if (Math.abs(vel.x) < stats().glideVelX * 0.8f || input().getInputDirection() == 0)
                    stats().chargeGlide(-delta * 2);
                break;
            case EVADE:
                maxVel.x = stats().evadeVelX;
                accel.x = stats().evadeAccX;
                damping.set(stats().evadeDamping, stats().evadeDampingSmall);
                break;
            case EVADE_DASH:
                maxVel.x = stats().glideVelX;
                accel.x = stats().glideAccX;
                gravityEnabled = false;
                break;
            case EVADE_DOWN:
            case EVADE_UP:
                maxVel.x = stats().evadeVelX;
                accel.x = stats().evadeAccX;
                damping.set(stats().evadeDampingSmall, stats().evadeDamping);
                break;
            case EVADE_DASH_DOWN:
            case EVADE_DASH_UP:
                gravityEnabled = false;
                break;
            case JUMPING:
                maxVel.x = stats().flyVelX;
                accel.x = stats().jumpAccX;
                break;
            case FLYING:
                maxVel.x = stats().flyVelX;
                accel.x = stats().flyAccX;
                break;
            case ATTACK_FORWARD1:
            case ATTACK_FORWARD2:
            case ATTACK_UP:
            case ATTACK_DOWN:
            case ATTACK_GLIDE:
            case ATTACK_GLIDE_HIT:
            case ATTACK_SOAR:
            case ATTACK_DIVE:
            case ATTACK_DIVE_LAND:
            case ATTACK_GROUND1:
            case ATTACK_GROUND2:
                accel.x = 0;
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
        if (damping.x != 0 || damping.y != 0) {
            applyWeightedForce(
                (damping.x / 100 * -vel.x * Math.abs(vel.x)),
                (damping.y / 100 * -vel.y * Math.abs(vel.y))
            );
        }
        if (!gravityEnabled) applyWeightedForce(0, -getBody().getWorld().getGravity().y);
    }

    public void meleeHitEffect() {
        Vector2 vel = getBody().getLinearVelocity();
        switch (getState()) {
            case ATTACK_FORWARD1:
            case ATTACK_FORWARD2:
                applyWeightedImpulse(-getSpriteDirection() * 5f, 0);
                break;
            case ATTACK_GLIDE:
                setState(PlayerState.ATTACK_GLIDE_HIT);
                applyWeightedImpulse(vel.x * -2, 10);
                break;
            case ATTACK_DIVE:
                setState(PlayerState.ATTACK_DIVE_LAND);
                applyWeightedImpulse(0, 5 - vel.y);
                break;
            case ATTACK_SOAR:
                applyWeightedImpulse(0, 5 - vel.y);
                break;
        }
    }

    @Override
    protected void beginState() {
        Vector2 vel = getBody().getLinearVelocity();
        Vector2 pos = getBody().getPosition();
        input.resetMeleeHit();
        switch(state) {
            case JUMPING:
                float jumpForce = stats().jumpImpulseAir;
                if (input().numJumps == stats().getMaxJumps()) jumpForce = stats().jumpImpulseGround;
                applyWeightedImpulse(0, jumpForce - vel.y * 0.8f);
                input().numJumps--;
                stats().resetJumpCD();
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
            case EVADE:
                new ProjectileShootVisual(pos.x, pos.y, getSpriteDirection(), animManager, getBody().getWorld());
                applyWeightedImpulse(getSpriteDirection() * stats().evadeImpulse - vel.x, -vel.y * 0.5f);
                break;
            case EVADE_DOWN:
                new ProjectileShootVisual(pos.x, pos.y, getSpriteDirection(), animManager, getBody().getWorld());
                applyWeightedImpulse(-vel.x * 0.5f, -stats().evadeImpulse - vel.y);
                break;
            case EVADE_UP:
                new ProjectileShootVisual(pos.x, pos.y, getSpriteDirection(), animManager, getBody().getWorld());
                applyWeightedImpulse(-vel.x * 0.5f, stats().evadeImpulse - vel.y);
                break;
            case EVADE_DASH:
                input().setGlide(true);
                stats().chargeGlide(0.5f);
                stats().chargeSoar(0.5f);
                break;
            case EVADE_DASH_DOWN:
                break;
            case EVADE_DASH_UP:
                break;
            case SOAR:
                stats().chargeSoar(-0.1f);
                break;
            case ATTACK_GROUND1:
            case ATTACK_FORWARD1:
                new Claw(1, 1, 4, 4, new Vector2(3f * getSpriteDirection(), 0),
                    getSpriteDirection(), animManager, getBody());
                break;
            case ATTACK_GROUND2:
            case ATTACK_FORWARD2:
                new Claw(1, 1, 4, 4, new Vector2(3f * getSpriteDirection(), 0),
                    -getSpriteDirection(), animManager, getBody());
                break;
            case ATTACK_DOWN:
                new Claw(1, 3, 5, 3, new Vector2(0.5f * getSpriteDirection(), -2),
                    getSpriteDirection(), animManager, getBody());
                break;
            case ATTACK_UP:
                new Claw(1, 3, 6, 4.5f, new Vector2(0, 2),
                    getSpriteDirection(), animManager, getBody());
                break;
            case ATTACK_GLIDE:
                applyWeightedImpulse(stats().attackGlideImpulseX * getMoveDirection(), stats().attackGlideImpulseY);
                new Claw(0, 0, 3, 3, new Vector2(2f * getSpriteDirection(), 0),
                    getSpriteDirection(), animManager, getBody());
                break;
            case ATTACK_DIVE:
                new Claw(0, 0, 2, 3, new Vector2(0, -2),
                    getSpriteDirection(), animManager, getBody());
                break;
            case ATTACK_DIVE_LAND:
                new Claw(1, 4, 6, 3, new Vector2(0, -0.5f),
                    getSpriteDirection(), animManager, getBody());
                break;
            case ATTACK_SOAR:
                new Claw(0, 0, 4, 4.5f, new Vector2(0, 2),
                    getSpriteDirection(), animManager, getBody());
                break;
        }
    }

    @Override
    protected void endState() {
        Vector2 vel = getBody().getLinearVelocity();
        if (getState() == PlayerState.EVADE_DASH) {
            getBody().setLinearVelocity(MathUtils.clamp(vel.x, -5, 5), vel.y);
        }
        if (stats().isEvading()) stats().setEvading(false);
        if (stats().isiFramesActive()) stats().setiFramesActive(false);
    }

    @Override
    public void setState(PlayerState state) {
        super.setState(state);
        if (input.getInputDirection() != 0 && getSpriteDirection() != input.getInputDirection())
            setSpriteDirection(input.getInputDirection());
    }

    @Override
    protected void updateAnimationFlags(float delta) {
        if (animEvents.containsKey(state)) {
            for (AnimationEvent animEvent : animEvents.get(state)) {
                if (getStateTime() - delta < animEvent.time && getStateTime() >= animEvent.time) {
                    switch (animEvent.event) {
                        case "evadestart":
                            stats().setEvading(true);
                            break;
                        case "evadeend":
                            stats().setEvading(false);
                            break;
                        case "invstart":
                            stats().setiFramesActive(true);
                            break;
                        case "invend":
                            stats().setiFramesActive(false);
                            break;
                        default:
                            System.err.printf("Unknown Player AnimationEvent in state [%s]: %s%n",
                                state.name(), animEvent);
                    }
                }
            }
        }
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
                if (e.getState() != EnemyState.DEATH && e.stats().getHitTimer() <= 0) {
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
        if (item.type == LootType.CRYSTAL) {
            stats().addCrystals(item.value);
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
    public boolean damage(float attackDamage, Vector2 attackOrigin, float knockback) {
        if (stats().isEvading()) {
            input().setEvadeDash(true);
            return false;
        }
        if (stats().isIntangible()) return false;
        if (super.damage(attackDamage, attackOrigin, 20)) {
            stats().setInvulnerability(1.5f);
            return true;
        }
        return false;
    }

    @Override
    public void death() {
        screen.gameOver();
    }

    public PlayerInput input() {
        return input;
    }

    @Override
    public PlayerStats stats() {
        return (PlayerStats) super.stats();
    }
}
