package io.github.dragonplatformer.Entity.Actor.Player;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.AnimationEvent;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Actor.Actor;
import io.github.dragonplatformer.Entity.Actor.Enemy.Enemy;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack.Claw;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile.ExplosiveFireball;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile.Fireball;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.ProjectileShootVisual;
import io.github.dragonplatformer.Entity.Effect.Loot.Loot;
import io.github.dragonplatformer.Entity.Effect.Loot.LootType;
import io.github.dragonplatformer.Entity.EffectManager;
import io.github.dragonplatformer.GameContactListener;
import io.github.dragonplatformer.GameScreen;

public class Player extends Actor<PlayerState> {
    private final GameScreen screen;
    public final PlayerInput input = new PlayerInput();
    private final PlayerUpgrades upgrades = new PlayerUpgrades();
    private final int itemPickupFixtureIndex;
    private final int evadeFixtureIndex;
    private int enemyContact = 0;
    private int evadeContact = 0;
    private Enemy enemyContactEntity;
    private ParticleEffect glideEffect;
    private ParticleEffect projectileChargeEffect;

    public Player(float x, float y, World world, GameScreen screen, EffectManager effectManager, AnimationManager animManager) {
        super(x, y, 2, 2, animManager.getPlayerAnimations(), animManager.getPlayerAnimEvents(),
            effectManager, animManager, world);
        this.screen = screen;

        setAsPlayer();
        setHitboxShapeCircle(0.7f);
        setMass(2f);
        setStats(new PlayerStats());
        setAutoMove(false);
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

        FixtureDef evadeDef = new FixtureDef();
        evadeDef.isSensor = true;
        CircleShape evadeShape = new CircleShape();
        evadeShape.setRadius(2);
        evadeDef.shape = evadeShape;
        Fixture evadeFixture = getBody().createFixture(evadeDef);
        evadeFixture.setUserData(this);
        evadeFixture.getFilterData().categoryBits = GameContactListener.FilterBits.PLAYER.getBit();
        evadeFixture.getFilterData().maskBits = GameContactListener.FilterBits.EFFECT.getBit();
        evadeFixtureIndex = getBody().getFixtureList().indexOf(evadeFixture, true);
        evadeShape.dispose();

        upgrades.upgrade(PlayerUpgrades.Upgrade.FIREBALL_LARGE);
        state = PlayerState.IDLE;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        getTargetPos().set(input().getCursor());
        if (stats().isEvading() && evadeContact > 0) input().setEvadeDash(true);
        input().update(delta);
        updatePlayerState();
        updatePlayerMovement(delta);
        if (input().meleeHit) {
            meleeHitEffect();
            input().resetMeleeHit();
        }
        if (input().isGliding()) {
            if (glideEffect == null) {
                glideEffect = effectManager.obtainGlide();
                glideEffect.start();
                for (ParticleEmitter emitter : glideEffect.getEmitters()) {
                    emitter.setContinuous(true);
                }
            }
            Vector2 pos = getPosition();
            float angle = getBody().getLinearVelocity().angleDeg();
            glideEffect.setPosition(pos.x, pos.y);
//            for (ParticleEmitter emitter : glideEffect.getEmitters()) {
//                if (emitter.getName().equals("wind")) emitter.getAngle().setHigh(angle);
//            }
        } else {
            if (glideEffect != null) {
                glideEffect.setDuration(0);
                glideEffect.getEmitters().first().setContinuous(false);
                glideEffect = null;
            }
        }
        if (input().getUseProjectile()) {
            Vector2 pos = getPosition().add(getSpriteDirection(), -0.2f);
            float angle = new Vector2(getTargetPos()).sub(getPosition()).angleDeg();
            if (projectileChargeEffect == null) {
                projectileChargeEffect = effectManager.obtainChargeFirePlayer();
                projectileChargeEffect.setPosition(pos.x, pos.y);
                projectileChargeEffect.start();
            }
            projectileChargeEffect.setPosition(pos.x, pos.y);
            for (ParticleEmitter emitter : projectileChargeEffect.getEmitters()) {
                emitter.getAngle().setHigh(angle);
            }
        } else {
            if (projectileChargeEffect != null) {
                projectileChargeEffect.setDuration(0);
                projectileChargeEffect = null;
            }
        }
//        if (enemyContact > 0) {
//            if (enemyContactEntity.getState() != EnemyState.DEATH && enemyContactEntity.stats().getHitTimer() <= 0) {
//                //damage(1, enemyContactEntity.getBody().getPosition(), 5);
//            }
//        }
    }

    private void updatePlayerState() {
        if (getState().isNonBlocking() || getCurrentAnim().isAnimationFinished(getStateTime())
            || getState() == PlayerState.JUMP) {
            if (input().getUseMelee()) {
                meleeAttack();
                input().resetMelee();
                return;
            } else if (input().getProjectileCharge() > 0 && !input().getUseProjectile()) {
                projectileAttack(input().getProjectileCharge());
                return;
            } else if (input().evade) {
                setState(PlayerState.EVADE);
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
            if (input().upMove) setState(PlayerState.EVADE_UP);
            else if (input().downMove) setState(PlayerState.EVADE_DOWN);
            else setState(PlayerState.EVADE_HORIZONTAL);
        }

        PlayerState nextState = PlayerState.IDLE;
        if (isGrounded()) {
            if (input().getInputDirection() != 0) nextState = PlayerState.RUN;
            if (input().upMove) nextState = PlayerState.JUMP;
        } else {
            if (input().isGliding()) {
                nextState = PlayerState.GLIDE;
                if (input().upMove) nextState = PlayerState.SOAR;
            } else {
                nextState = PlayerState.FLY;
                if (input().upMove) {
                    nextState = PlayerState.JUMP;
                }
            }
            if (input.downMove) nextState = PlayerState.DIVE;
        }

        if (stats().getHealth() <= 0) {
            nextState = PlayerState.DEATH;
        }

        if (getState().isNonBlocking() && nextState != getState()) setState(nextState);
        else if (getState() == PlayerState.JUMP && nextState == PlayerState.DIVE) setState(nextState);
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
        Vector2 pos = new Vector2(getBody().getPosition().x + getSpriteDirection() * 2, getBody().getPosition().y - 0.25f);
        switch (upgrades.getProjectile()) {
            case FIREBALL_LARGE:
                if (charge > 0.8f) {
                    new ExplosiveFireball(1, 5, 5, pos.x, pos.y, 1,
                        getTargetPos(), effectManager, animManager, true, getBody().getWorld());
                    applyWeightedImpulse(getPosition().sub(getTargetPos()).setLength(stats().fireballLargeRecoil));
                    new ProjectileShootVisual(pos.x - getSpriteDirection(), pos.y,
                        effectManager, animManager, getBody().getWorld());
                    stats().resetProjectileCD();
                    input().resetProjectileCharge();
                } else if (charge > 0.4) {
                    new Fireball(
                        1, 3, 1, pos.x, pos.y, 1,
                        getTargetPos(), effectManager, animManager, true, getBody().getWorld()
                    );
                    stats().resetProjectileCD();
                    input().resetProjectileCharge();
                } else if (stats().getProjectileCD() <= 0){
                    new Fireball(
                        1, 3, 1, pos.x, pos.y, 1,
                        getTargetPos(), effectManager, animManager, true, getBody().getWorld()
                    );
                    stats().resetProjectileCD();
                    input().resetProjectileCharge();
                }
                return;
            case FIREBREATH:
                if (input().projectileCharge > 0.25f) {
//                projectile = new Firebreath(0.5f, 0f, 1f, impulse, 0.5f - input.breathCount / 30, pos.x, pos.y,
//                    2f, 1, getSpriteDirection(), animManager, true, getBody().getWorld());
//                impulse.rotateDeg((float) Math.random() * 60 - 30);
//                impulse.scl(0.6f, 0.9f);
                    //stats().resetProjectileCD(0.05f + (getInput().breathCount) / 80);
                    stats().resetProjectileCD(0.05f);
                }
                return;
            case FIREBALL_BASIC:
                stats().resetProjectileCD();
                input().resetProjectileCharge();
                new Fireball(
                    1, 3, 1, pos.x, pos.y, 1,
                    new Vector2(pos).add(getSpriteDirection(), 0), effectManager, animManager, true, getBody().getWorld()
                );
        }
    }

    private void updatePlayerMovement(float delta) {
        Vector2 accel = new Vector2(stats().groundAccX, 0);
        Vector2 maxVel = new Vector2(stats().groundVelX, 0);
        Vector2 vel = getBody().getLinearVelocity();
        damping().set(stats().flyDampingX, stats().flyDampingY);
        if (vel.y < 0) damping().y = stats().fallDamping;
        boolean gravityEnabled = true;

        if (input.getInputDirection() != 0 && getSpriteDirection() != input.getInputDirection()
            && (getState().isNonBlocking() || getState() == PlayerState.JUMP)) {
            setSpriteDirection(input.getInputDirection());
        }

        switch (getState()) {
            case DIVE:
                maxVel.set(stats().diveVelX, stats().diveVelY);
                accel.set(stats().diveAccX, stats().diveAccY);
                gravityEnabled = false;
                stats().chargeSoar(delta);
                stats().chargeGlide(delta);
                if (stats().getGlideCharge() > stats().glideChargeMin) input().setGlide(true);
                break;
            case SOAR:
                maxVel.set(stats().diveVelX, stats().soarVelY);
                accel.set(stats().diveAccX, stats().soarAccY);
                gravityEnabled = false;
                if (vel.y >= 0) stats().chargeSoar(-delta);
                if (stats().getSoarCharge() <= 0) stats().chargeGlide(-delta);
                break;
            case GLIDE:
                maxVel.set(stats().glideVelX, stats().glideVelY);
                accel.set(stats().glideAccX, stats().glideAccY);
                damping().y = stats().glideDampingY;
                if (Math.abs(vel.x) + Math.abs(vel.y) < stats().glideVelX * 0.8f || input().getInputDirection() == 0)
                    stats().chargeGlide(-delta * 2);
                break;
            case EVADE:
                maxVel.x = stats().evadeVelX;
                accel.x = stats().evadeAccX;
                damping().set(stats().evadeDamping, stats().evadeDamping);
                break;
            case EVADE_HORIZONTAL:
                maxVel.x = stats().glideVelX;
                accel.x = stats().glideAccX;
                gravityEnabled = false;
                break;
            case EVADE_DOWN:
            case EVADE_UP:
                gravityEnabled = false;
                break;
            case JUMP:
                maxVel.x = stats().flyVelX;
                accel.x = stats().jumpAccX;
                break;
            case FLY:
                maxVel.x = stats().flyVelX;
                accel.x = stats().flyAccX;
                break;
            case ATTACK_SOAR:
                stats().chargeSoar(-delta / 2);
            case ATTACK_GLIDE:
                stats().chargeGlide(-delta / 2);
            case ATTACK_DIVE:
            case ATTACK_FORWARD1:
            case ATTACK_FORWARD2:
            case ATTACK_UP:
            case ATTACK_DOWN:
            case ATTACK_DIVE_LAND:
            case ATTACK_GROUND1:
            case ATTACK_GROUND2:
                damping().set(10, 0);
                accel.x = 0;
                break;
        }

        if (isGrounded() || (input.isGliding() && stats().getGlideCharge() <= 0)) {
            input().setGlide(false);
            stats().chargeGlide(-delta);
        }

        // Input based movement
        applyClampedForce(new Vector2(accel.x * input().getInputDirection(), accel.y),
            new Vector2(maxVel).scl(-1), maxVel);

        // Ground Friction
        if (vel.x != 0 && input().getInputDirection() == 0 && isGrounded()) {
            if (Math.abs(vel.x) > 0.5f) {
                damping().x = stats().groundDampingX;
            } else {
                getBody().setLinearVelocity(0, vel.y);
            }
        }
        if (!gravityEnabled) applyWeightedForce(0, -getBody().getWorld().getGravity().y);
    }

    @Override
    protected void beginState() {
        Vector2 vel = getBody().getLinearVelocity();
        Vector2 pos = getBody().getPosition();
        input.resetMeleeHit();
        switch(state) {
            case JUMP:
                float jumpForce = stats().jumpImpulseAir;
                if (input().numJumps == stats().getMaxJumps()) jumpForce = stats().jumpImpulseGround;
                applyTimedImpulse(0, jumpForce - vel.y * 0.8f, 0.2f);
                input().numJumps--;
                stats().resetJumpCD();
                ParticleEffect wingFlapEffect = effectManager.obtainFlapWind();
                wingFlapEffect.setPosition(getPosition().x, getPosition().y);
                wingFlapEffect.reset();
                break;
            case GLIDE:
                if (stats().getGlideCharge() > stats().glideChargeMin) {
                    applyWeightedImpulse(input().getInputDirection() * stats().glideImpulseX, 0);
                    applyTimedImpulse(0, stats().glideImpulseY, 0.3f);
                }
                stats().setGlideCharge(stats().glideChargeBase);
                break;
            case DIVE:
                applyWeightedImpulse(0, -stats().diveImpulseY);
                break;
            case SOAR:
                stats().chargeSoar(-0.1f);
                applyTimedImpulse(0, stats().soarImpulseY, 0.3f);
                break;
            case EVADE_HORIZONTAL:
                new ProjectileShootVisual(getPosition().x, getPosition().y, effectManager, animManager, getBody().getWorld());
                int dir = input().getInputDirection();
                if (dir == 0) dir = getSpriteDirection();
                applyTimedImpulse(stats().evadeImpulse * dir, 0, 0.1f);
                input().setGlide(true);
                stats().chargeGlide(0.5f);
                stats().chargeSoar(0.5f);
                break;
            case EVADE_UP:
                new ProjectileShootVisual(getPosition().x, getPosition().y, effectManager, animManager, getBody().getWorld());
                applyTimedImpulse(0, stats().evadeImpulse, 0.1f);
                input().setGlide(true);
                stats().chargeGlide(0.5f);
                stats().chargeSoar(0.5f);
                break;
            case EVADE_DOWN:
                new ProjectileShootVisual(getPosition().x, getPosition().y, effectManager, animManager, getBody().getWorld());
                applyTimedImpulse(0, -stats().evadeImpulse, 0.1f);
                input().setGlide(true);
                stats().chargeGlide(0.5f);
                stats().chargeSoar(0.5f);
                break;
            case ATTACK_DOWN:
                new Claw(
                    1, 10, 2, 2, getHitboxPosition().add(0, -2),
                    effectManager, animManager, getBody()
                ).setSpawnDelay(0.1f);
                break;
            case ATTACK_UP:
                new Claw(
                    1, 10, 2, 2, getHitboxPosition().add(0, 2),
                    effectManager, animManager, getBody()
                ).setSpawnDelay(0.1f);
                break;
            case ATTACK_GLIDE:
                applyTimedImpulse(
                    stats().attackGlideImpulseX * getSpriteDirection(), stats().attackGlideImpulseY, 0.1f
                );
                new Claw(
                    1, 15, 3, 3,
                    getHitboxPosition().add(1f * getSpriteDirection(), 0), effectManager, animManager, getBody()
                ).setSpawnDelay(0.2f);
                break;
            case ATTACK_DIVE:
                applyTimedImpulse(0, -stats().attackVerticalImpulse, 0.1f);
                new Claw(
                    1, 15, 3, 3, getHitboxPosition().add(0, -1),
                    effectManager, animManager, getBody()
                ).setSpawnDelay(0.2f);
                break;
            case ATTACK_DIVE_LAND:
                new Claw(
                    1, 15, 4, 1, getHitboxPosition().add(0, -1),
                    effectManager, animManager, getBody()
                );
                break;
            case ATTACK_SOAR:
                applyTimedImpulse(0, stats().attackVerticalImpulse, 0.1f);
                new Claw(
                    1, 15, 3, 3, getHitboxPosition().add(0, 1),
                    effectManager, animManager, getBody()
                ).setSpawnDelay(0.2f);
                break;
        }
    }

    public void meleeHitEffect() {
        Vector2 vel = getBody().getLinearVelocity();
        switch (getState()) {
            case ATTACK_GROUND1:
            case ATTACK_GROUND2:
            case ATTACK_FORWARD1:
            case ATTACK_FORWARD2:
                applyWeightedImpulse(
                    getSpriteDirection() * -stats().recoilAttackGroundX - vel.x, stats().recoilAttackGroundY
                );
                break;
            case ATTACK_GLIDE:
                stats().chargeGlide(0.3f);
                break;
            case ATTACK_SOAR:
                stats().chargeGlide(0.3f);
                stats().chargeSoar(0.3f);
                break;
            case ATTACK_DIVE:
                break;
        }
    }

    @Override
    protected void endState() {
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
    protected void onAnimEvent(AnimationEvent animEvent) {
        super.onAnimEvent(animEvent);
        switch (animEvent.event) {
            case evadestart:
                stats().setEvading(true);
                break;
            case evadeend:
                stats().setEvading(false);
                break;
            case iframestart:
                stats().setiFramesActive(true);
                break;
            case iframeend:
                stats().setiFramesActive(false);
                break;
            case hitframe:
                switch (getState()) {
                    case ATTACK_GROUND1:
                    case ATTACK_GROUND2:
                    case ATTACK_FORWARD1:
                    case ATTACK_FORWARD2:
                        new Claw(
                            1, 10, 3, 1.5f,
                            getHitboxPosition().add(2f * getSpriteDirection(), 0.2f),
                            effectManager, animManager, getBody()
                        );
                        break;
                }
                break;
            default:
                System.err.printf("Unknown Player AnimationEvent in state [%s]: %s%n",
                    state.name(), animEvent);
        }
    }

    @Override
    public boolean isGrounded() {
        return super.isGrounded() && getState() != PlayerState.JUMP;
    }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        super.beginContact(entityFixture, contactFixture);
        if (getBody().getFixtureList().indexOf(entityFixture, true) == getJumpSensorIndex()) {
            input().numJumps = stats().getMaxJumps();
        } else if (getBody().getFixtureList().indexOf(entityFixture, true) == getHitboxIndex()) {
//            if (contactFixture.getUserData() instanceof Enemy) {
//                enemyContact++;
//                Enemy e = (Enemy) contactFixture.getUserData();
//                enemyContactEntity = e;
//                if (e.getState() != EnemyState.DEATH && e.stats().getHitTimer() <= 0) {
//                    //damage(1, e.getBody().getPosition(), 5);
//                }
//            } else
                if (contactFixture.getUserData() instanceof Loot) {
                Loot loot = (Loot) contactFixture.getUserData();
                if (!loot.isLooted()) {
                    loot(loot);
                    loot.setLooted();
                }
            }
        } else if (getBody().getFixtureList().indexOf(entityFixture, true) == evadeFixtureIndex) {
            evadeContact++;
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
        } else if (getBody().getFixtureList().indexOf(entityFixture, true) == evadeFixtureIndex) {
            evadeContact--;
        }
    }

    @Override
    public boolean damage(float attackDamage, Vector2 attackOrigin, float knockback, Fixture entityFixture) {
        if (getBody().getFixtureList().indexOf(entityFixture, true) == evadeFixtureIndex) {
            if (stats().isEvading()) input().setEvadeDash(true);
            return false;
        }
        if (stats().isIntangible()) return false;
        if (super.damage(attackDamage, attackOrigin, knockback, entityFixture)) {
            stats().setInvulnerable(1.5f);
            return true;
        }
        return false;
    }

    @Override
    public void death() {
        screen.gameOver();
    }

    @Override
    public void debugDraw(Matrix4 projectionMatrix) {
        super.debugDraw(projectionMatrix);
        //Utils.drawLine(getPosition(), getTargetPos(), 2, Color.BLUE, projectionMatrix);
    }

    public PlayerInput input() {
        return input;
    }

    @Override
    public PlayerStats stats() {
        return (PlayerStats) super.stats();
    }

    public int getEvadeContact() {
        return evadeContact;
    }
}
