package io.github.dragonplatformer.Entity.Creature;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Queue;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.AttackEffect.Fireball;

public class Manticore extends Enemy {
    private Vector2 target;
    private float attackTimer;
    private final Queue<EnemyState> attackPhase;
    private float chargeVelocity;
    private float pounceChargeTime;
    private float pounceDuration;
    private float projectileChargeTime;
    public Manticore(float x, float y, World world, AnimationManager animManager) {
        super(x, y, 6, 6, world, animManager, AnimationManager.AnimationKeys.ENEMY_MANTICORE);
        //setHitboxShape(new Vector2(2, 2), new Vector2(1, 0));
        setPlayerSensorShape(new Vector2(30, 20));
        setAggroRange(100);
        init();
        stats().init(100);
        target = new Vector2();
        attackTimer = 0;
        //setStunOnHit(false);
        attackPhase = new Queue<>();
        setLoot((int) (Math.random() * 10) + 40);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        attackTimer -= delta;
        Vector2 pos = getBody().getPosition();
        Vector2 vel = getBody().getLinearVelocity();
        float targetTime = pounceChargeTime - 0.1f;
        switch (getState()) {
            case IDLE:
            case FLYIDLE:
                if (getPlayerSighted()) {
                    if (!attackPhase.isEmpty()) {
                        setState(attackPhase.removeFirst());
                    }
                    else if (attackTimer <= 0) {

                        if (stats().getHealth() > stats().getMaxHealth() * 0.5) {
                            chargeVelocity = 0.8f;
                            pounceChargeTime = 1.2f;
                            pounceDuration = 0.9f;
                            projectileChargeTime = 0.5f;
                            double rand = Math.random();
                            if (rand < 0.4) {
                                attackPhase.addLast(EnemyState.CHARGELUNGE);
                                attackPhase.addLast(EnemyState.CHARGELUNGE);
                                attackPhase.addLast(EnemyState.CHARGELUNGE);
                            } else if (rand < 0.8) {
                                attackPhase.addLast(EnemyState.CHARGESHOOTPROJECTILE);
                                attackPhase.addLast(EnemyState.CHARGESHOOTPROJECTILE);
                                attackPhase.addLast(EnemyState.CHARGESHOOTPROJECTILE);
                            } else {
                                attackPhase.addLast(EnemyState.CHARGELUNGE);
                                attackPhase.addLast(EnemyState.CHARGESHOOTPROJECTILE);
                                attackPhase.addLast(EnemyState.CHARGELUNGE);
                                attackPhase.addLast(EnemyState.CHARGESHOOTPROJECTILE);
                            }
                        } else {
                            chargeVelocity = 0.4f;
                            pounceChargeTime = 0.7f;
                            pounceDuration = 0.7f;
                            attackPhase.addLast(EnemyState.CHARGELUNGE);
                            attackPhase.addLast(EnemyState.SHOOTPROJECTILE);
                            attackPhase.addLast(EnemyState.CHARGELUNGE);
                            attackPhase.addLast(EnemyState.SHOOTPROJECTILE);
                            attackPhase.addLast(EnemyState.CHARGELUNGE);
                            attackPhase.addLast(EnemyState.SHOOTPROJECTILE);

                        }

                        attackPhase.addLast(EnemyState.IDLE);
                        setState(attackPhase.removeFirst());
                    }
                }
                break;
            case CHARGELUNGE:
            case FLYCHARGELUNGE:
                if (getStateTime() > pounceChargeTime) setState(EnemyState.LUNGE);
                if (getStateTime() < targetTime && getStateTime() + delta > targetTime) target = new Vector2(getPlayerPos());
                break;
            case LUNGE:
                if (getStateTime() > pounceDuration || Math.abs(getBody().getLinearVelocity().x) < 0.5f) {
                    setState(EnemyState.IDLE);
                }
                if (anims.get(getState()).isAnimationFinished(getStateTime() + delta)) setState(attackPhase.removeFirst());
                break;
            case CHARGESHOOTPROJECTILE:
            case FLYCHARGESHOOTPROJECTILE:
                if (getStateTime() >= projectileChargeTime) {
                    setState(EnemyState.SHOOTPROJECTILE);
                }
                break;
            case SHOOTPROJECTILE:
            case FLYSHOOTPROJECTILE:
                if (anims.get(getState()).getKeyFrameIndex(getStateTime()) == 0
                    && anims.get(getState()).getKeyFrameIndex(getStateTime() + delta) == 1)
                    shootProjectile((int)(Math.random() * 3 + 3));
                else if (anims.get(getState()).isAnimationFinished(getStateTime() + delta)) setState(attackPhase.removeFirst(), true);
                break;
        }
        if (getState().isFlying()) {
            if (anims.get(getState()).getKeyFrameIndex(getStateTime()) == 1
                && anims.get(getState()).getKeyFrameIndex(getStateTime() + delta) == 0)
                getBody().applyLinearImpulse(0, 70 - vel.y, pos.x, pos.y, true);
        }
        if (isGrounded() && Math.abs(getBody().getLinearVelocity().x) > 3)
            getBody().applyLinearImpulse(new Vector2(getBody().getLinearVelocity()).scl(-1f).x, 0, pos.x, pos.y, true);
    }

    @Override
    public boolean setState(EnemyState state) {
        switch (state) {
            case IDLE:
                if (!isGrounded()) return super.setState(EnemyState.FLYIDLE);
            case CHARGELUNGE:
                if (!isGrounded()) return super.setState(EnemyState.FLYCHARGELUNGE);
            case SHOOTPROJECTILE:
                if (!isGrounded()) return super.setState(EnemyState.FLYSHOOTPROJECTILE);
            default:
                return super.setState(state);
        }
    }

    @Override
    public void beginState() {
        super.beginState();
        switch (getState()) {
            case IDLE:
            case FLYIDLE:
                //chainAttacks = (int)(Math.random() * 3 + 2);
                attackTimer = 3;
                break;
            case LUNGE:
                if (isGrounded()) target.y = Math.max(target.y, getBody().getPosition().y + 2);
                getBody().applyLinearImpulse(
                    new Vector2(target).sub(getBody().getPosition()).nor().scl(1000f * chargeVelocity, 1400 * chargeVelocity).sub(getBody().getLinearVelocity()),
                    getBody().getPosition(), true
                );
            case SHOOTPROJECTILE:
            case FLYSHOOTPROJECTILE:
        }
    }

    @Override
    public void endState() {
        super.endState();
    }

    private void shootProjectile(int numProjectiles) {
        float spread = 120;
        for (int i = 0; i < numProjectiles; i++) {
            Fireball projectile = new Fireball(1, 10, 2, getBody().getPosition().x, getBody().getPosition().y,
                2, 2, -1, animManager, false, getBody().getWorld());
            float rot = spread * (i) / (numProjectiles - 1) - spread / 2;
            Vector2 aim = new Vector2(getPlayerPos()).sub(projectile.getBody().getPosition()).rotateDeg(rot);
            projectile.setRotation(aim.angleDeg());
            if (Math.abs(aim.angleDeg() - 180) > 90) projectile.setDirection(1);
            else projectile.setDirection(-1);
            projectile.getBody().applyLinearImpulse(aim.setLength(30), projectile.getBody().getPosition(), true);
        }
    }
}
