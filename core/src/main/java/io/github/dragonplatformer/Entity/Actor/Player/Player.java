package io.github.dragonplatformer.Entity.Actor.Player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.AnimationEvent;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Actor.Actor;
import io.github.dragonplatformer.Entity.Actor.Enemy.Enemy;
import io.github.dragonplatformer.Entity.Effect.Loot.Loot;
import io.github.dragonplatformer.Entity.Effect.Loot.LootType;
import io.github.dragonplatformer.Entity.EffectManager;
import io.github.dragonplatformer.Screen.GameContactListener;
import io.github.dragonplatformer.Screen.GameScreen;
import io.github.dragonplatformer.Utils;

public class Player extends Actor<PlayerState> {
    private final GameScreen screen;
    public final PlayerInput input = new PlayerInput();
    private final int itemPickupFixtureIndex;
    private final int evadeFixtureIndex;
    private int enemyContact = 0;
    private int evadeContact = 0;
    private Enemy enemyContactEntity;
    private ParticleEffect glideEffect;
    private final ParticleEffect cursorEffect;

    public Player(float x, float y, PlayerStats stats, World world, GameScreen screen, EffectManager effectManager, AnimationManager animManager) {
        super(x, y, 2, 2, animManager.getPlayerAnimations(), animManager.getPlayerAnimEvents(),
            effectManager, animManager, world);
        this.screen = screen;

        setAsPlayer();
        setHitboxShapeCircle(0.7f);
        setMass(2f);
        setStats(stats);
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

        state = PlayerState.IDLE;
        cursorEffect = effectManager.obtainChargeFirePlayer();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        getTargetPos().set(getPosition()).add(input().getCursor());
        Vector2 cursorPosition = new Vector2(getPosition())
            .add(new Vector2(getTargetPos()).sub(getPosition()).setLength2(9));
        cursorEffect.setPosition(cursorPosition.x, cursorPosition.y);
        if (stats().isEvading() && evadeContact > 0) input().setEvadeDash(true);
        input().update(delta);
        updatePlayerState();
        updatePlayerMovement(delta);
        if (input().meleeHit) {
            stats().getMeleeUpgrade().hitEffect(this, effectManager, animManager);
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
            glideEffect.setPosition(pos.x, pos.y);
        } else {
            if (glideEffect != null) {
                glideEffect.allowCompletion();
                glideEffect = null;
            }
        }
        stats().getMeleeUpgrade().act(delta, this, effectManager, animManager);
        stats().getBreathUpgrade().act(delta, this, effectManager, animManager);
        stats().getEvadeUpgrade().act(delta, this, effectManager, animManager);
    }

    private void updatePlayerState() {
        if (getState().isNonBlocking() || getCurrentAnim().isAnimationFinished(getStateTime())
            || getState() == PlayerState.JUMP) {
            if (input().getUseMelee()) {
                stats().getMeleeUpgrade().startMelee(this);
                input().resetMelee();
                return;
            } else if (input().getProjectileCharge() > 0 && !input().getUseProjectile()) {
                stats().getBreathUpgrade().use(this, effectManager, animManager);
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

//        if (input().getEvadeDash()) {
//            input().setEvadeDash(false);
//            if (input().upMove) setState(PlayerState.EVADE_UP);
//            else if (input().downMove) setState(PlayerState.EVADE_DOWN);
//            else setState(PlayerState.EVADE_HORIZONTAL);
//        }

        PlayerState nextState = PlayerState.IDLE;
        if (isGrounded()) {
            if (input().getInputDirection() != 0) nextState = PlayerState.RUN;
            if (input().upMove) nextState = PlayerState.JUMP;
        } else {
            if (input().isGliding()) {
                if (input().upMove) {
                    if (getState() == PlayerState.GLIDE) {
                        nextState = PlayerState.GLIDE_SOAR_TRANSITION;
                    } else if (getState() == PlayerState.DIVE) {
                        nextState = PlayerState.DIVE_GLIDE_TRANSITION;
                    } else {
                        nextState = PlayerState.SOAR;
                    }
                } else {
                    if (getState() == PlayerState.SOAR) {
                        nextState = PlayerState.SOAR_GLIDE_TRANSITION;
                    } else if (getState() == PlayerState.DIVE) {
                        nextState = PlayerState.DIVE_GLIDE_TRANSITION;
                    } else nextState = PlayerState.GLIDE;
                }
            } else {
                if (getState() == PlayerState.DIVE) nextState = PlayerState.DIVE_GLIDE_TRANSITION;
                else nextState = PlayerState.FLY;
                if (input().upMove) {
                    nextState = PlayerState.JUMP;
                }
            }
            if (input.downMove) {
                if (getState() != PlayerState.DIVE) {
                    nextState = PlayerState.GLIDE_DIVE_TRANSITION;
                } else nextState = PlayerState.DIVE;
            }
        }

        if (input().evade) {
            nextState = PlayerState.EVADE;
        } else if (getState() == PlayerState.EVADE) {
            if (input().upMove) {
                nextState = PlayerState.EVADE_UP;
            } else if (input().downMove) {
                nextState = PlayerState.EVADE_DOWN;
            } else {
                nextState = PlayerState.EVADE_HORIZONTAL;
            }
        }

        if (stats().getHealth() <= 0) {
            nextState = PlayerState.DEATH;
        }

        if (getState().isNonBlocking() && nextState != getState()) setState(nextState);
        else if (getState() == PlayerState.JUMP
            && (nextState == PlayerState.GLIDE_DIVE_TRANSITION || nextState == PlayerState.EVADE)) {
            setState(nextState);
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
            case GLIDE_DIVE_TRANSITION:
            case EVADE_DOWN:
                maxVel.set(stats().diveVelX, stats().diveVelY);
                accel.set(stats().diveAccX, stats().diveAccY);
                gravityEnabled = false;
                stats().chargeSoar(delta);
                stats().chargeGlide(delta);
                if (stats().getGlideCharge() > stats().glideChargeMin) input().setGlide(true);
                break;
            case SOAR:
            case GLIDE_SOAR_TRANSITION:
            case EVADE_UP:
                maxVel.set(stats().diveVelX, stats().soarVelY);
                accel.set(stats().diveAccX, stats().soarAccY);
                gravityEnabled = false;
                if (vel.y >= 0) stats().chargeSoar(-delta);
                if (stats().getSoarCharge() <= 0) stats().chargeGlide(-delta);
                break;
            case GLIDE:
            case DIVE_GLIDE_TRANSITION:
            case SOAR_GLIDE_TRANSITION:
            case EVADE_HORIZONTAL:
                maxVel.set(stats().glideVelX, stats().glideVelY);
                accel.set(stats().glideAccX, stats().glideAccY);
                damping().y = stats().glideDampingY;
                if (Math.abs(vel.x) + Math.abs(vel.y) < stats().glideVelX * 0.8f || input().getInputDirection() == 0) {
                    stats().chargeGlide(-delta * 2);
                }
                break;
            case EVADE:
                maxVel.x = stats().evadeVelX;
                accel.x = stats().evadeAccX;
                damping().set(stats().evadeDamping, stats().evadeDamping);
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
            case ATTACK_GLIDE:
            case ATTACK_DIVE:
                damping().set(0, 0);
                accel.x = 0;
                break;
            case ATTACK_FORWARD1:
            case ATTACK_FORWARD2:
            case ATTACK_GROUND1:
            case ATTACK_GROUND2:
            case ATTACK_UP:
            case ATTACK_DOWN:
                damping().set(0, stats().glideDampingY);
                accel.x = stats().flyAccX;
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
            if (Math.abs(vel.x) >= 1) {
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
            case DIVE_GLIDE_TRANSITION:
            case SOAR_GLIDE_TRANSITION:
                if (stats().getGlideCharge() > stats().glideChargeMin) {
                    applyWeightedImpulse(input().getInputDirection() * stats().glideImpulseX, 0);
                    applyTimedImpulse(0, stats().glideImpulseY, 0.3f);
                }
                stats().setGlideCharge(stats().glideChargeBase);
                break;
            case GLIDE_DIVE_TRANSITION:
                applyWeightedImpulse(0, -stats().diveImpulseY);
                break;
            case GLIDE_SOAR_TRANSITION:
                stats().chargeSoar(-0.1f);
                applyTimedImpulse(0, stats().soarImpulseY, 0.3f);
                break;
            case EVADE:
                stats().getEvadeUpgrade().evadeStart(this, effectManager, animManager);
                break;
            case EVADE_HORIZONTAL:
            case EVADE_UP:
            case EVADE_DOWN:
                //stats().getEvadeUpgrade().use(this, effectManager, animManager);
                break;
            case ATTACK_FORWARD1:
            case ATTACK_FORWARD2:
            case ATTACK_GROUND1:
            case ATTACK_GROUND2:
            case ATTACK_UP:
            case ATTACK_DOWN:
            case ATTACK_GLIDE:
            case ATTACK_DIVE:
            case ATTACK_SOAR:
                stats().getMeleeUpgrade().use(this, effectManager, animManager);
                break;
        }
    }

    @Override
    protected void endState() {
        if (stats().isEvading()) stats().setEvading(false);
        if (stats().isiFramesActive()) stats().setiFramesActive(false);
        if (getState() == PlayerState.EVADE) {
            stats().getEvadeUpgrade().evadeEnd(this);
        }
    }

    @Override
    public void setState(PlayerState state) {
        super.setState(state);
    }

    @Override
    protected void onAnimEvent(AnimationEvent animEvent) {
        super.onAnimEvent(animEvent);
        switch (animEvent.event) {
            case evadestart:
                break;
            case evadeend:
                break;
            case iframestart:
                stats().setiFramesActive(true);
                break;
            case iframeend:
                stats().setiFramesActive(false);
                break;
            case hitframe:
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
    public void land() {
        super.land();
        cancelTimedImpulses((TimedImpulse timedImpulse) -> timedImpulse.impulse.y > 0);
        setState(PlayerState.IDLE);
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
    public void destroy() {
        super.destroy();
        cursorEffect.allowCompletion();
    }

    @Override
    public void debugDraw(Matrix4 projectionMatrix) {
        super.debugDraw(projectionMatrix);
        Utils.drawLine(getPosition(), getTargetPos(), 2, Color.BLUE, projectionMatrix);
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
