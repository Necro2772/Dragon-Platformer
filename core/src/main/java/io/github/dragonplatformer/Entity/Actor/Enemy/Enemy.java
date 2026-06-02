package io.github.dragonplatformer.Entity.Actor.Enemy;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.EnemyDeathVisual;
import io.github.dragonplatformer.Entity.Actor.Actor;
import io.github.dragonplatformer.Entity.Actor.Player.Player;
import io.github.dragonplatformer.Entity.Effect.Loot.Crystal;
import io.github.dragonplatformer.Entity.MovementType;
import io.github.dragonplatformer.GameContactListener;

import java.util.ArrayList;

public abstract class Enemy extends Actor<EnemyState> {
    private final PlayerLOSRay losRay;
    private Vector2 playerSensorSize;
    private Vector2 playerSensorCenter;
    private int playerSensorIndex;
    private final FixtureDef playerSensorDef;
    private final ArrayList<Fixture> nearbyEnemies;
    private float disperseDist;
    private int nearbyEnemySensorIndex;
    private final FixtureDef nearbyEnemySensorDef;

    public Enemy(float x, float y, float width, float height, World world,
                 AnimationManager animManager, AnimationKey animKey) {
        super(x, y, width, height, animManager.getEnemyAnims(animKey), animManager.getEnemyAnimEvents(animKey),
            animManager, world);
        setAsEnemy();

        playerSensorDef = new FixtureDef();
        playerSensorDef.filter.categoryBits = GameContactListener.FilterBits.SENSOR.getBit();
        playerSensorDef.filter.maskBits = GameContactListener.FilterBits.PLAYER.getBit();
        playerSensorDef.isSensor = true;

        setState(EnemyState.IDLE);
        losRay = new PlayerLOSRay();

        playerSensorSize = new Vector2(15, 10);
        playerSensorCenter = new Vector2(0, -playerSensorSize.y/2);

        this.setStats(new EnemyStats());
        playerSensorIndex = -1;
        nearbyEnemySensorIndex = -1;

        nearbyEnemies = new ArrayList<>();
        nearbyEnemySensorDef = new FixtureDef();
        nearbyEnemySensorDef.filter.categoryBits = GameContactListener.FilterBits.SENSOR.getBit();
        nearbyEnemySensorDef.filter.maskBits = GameContactListener.FilterBits.NONE.getBit();
        nearbyEnemySensorDef.filter.groupIndex = GameContactListener.FilterGroup.ENEMYDEFAULT.getBit();
        nearbyEnemySensorDef.isSensor = true;
        setDisperseDist(5);
    }

    @Override
    public void init() {
        super.init();
        PolygonShape playerSensorShape = new PolygonShape();
        playerSensorShape.setAsBox(playerSensorSize.x, playerSensorSize.y, playerSensorCenter, 0);
        playerSensorDef.shape = playerSensorShape;
        Fixture playerSensorFixture = getBody().createFixture(playerSensorDef);
        playerSensorIndex = getBody().getFixtureList().indexOf(playerSensorFixture, true);
        playerSensorShape.dispose();

        CircleShape nearbyEnemyShape = new CircleShape();
        nearbyEnemyShape.setRadius(disperseDist);
        nearbyEnemySensorDef.shape = nearbyEnemyShape;
        Fixture nearbyEnemyFixture = getBody().createFixture(nearbyEnemySensorDef);
        nearbyEnemySensorIndex = getBody().getFixtureList().indexOf(nearbyEnemyFixture, true);
        nearbyEnemyShape.dispose();
    }

    public void setPlayerSensorShape(Vector2 playerSensorSize) {
        this.playerSensorSize = playerSensorSize;
        playerSensorCenter.y = playerSensorSize.y/4;
        stats().setAggroRange(playerSensorSize.x * 2);
    }

    public void setPlayerSensorShape(Vector2 playerSensorSize, Vector2 playerSensorCenter) {
        this.playerSensorSize = playerSensorSize;
        this.playerSensorCenter = playerSensorCenter;
        stats().setAggroRange(playerSensorSize.x * 2);
    }

    public void setDisperseDist(float disperseDist) {
        this.disperseDist = disperseDist;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (stats().getHealth() <= 0) {
            setState(EnemyState.DEATH);
        }
        if (anims.get(getState()) == null || anims.get(getState()).isAnimationFinished(getStateTime())) {
            if (getState() == EnemyState.DEATH) {
                death();
                return;
            }
            else if (getState().nextState() != null) {
                setState(getState().nextState());
            }
        }
        if (stats().isPlayerInRange() && !stats().isPlayerSighted()) {
            losRay.reset();
            getBody().getWorld().rayCast(losRay, getBody().getPosition(), stats().getPlayerPos());
            if (!losRay.result) stats().setPlayerSighted(true);
        }
        if (stats().isPlayerSighted() && !stats().isPlayerInRange()) {
            if (getBody().getPosition().dst(stats().getPlayerPos()) > stats().getAggroRange()) {
                stats().setPlayerSighted(false);
            }
        }
        if (stats().isPlayerSighted() && getState().isNonBlocking()) {
            if (getBody().getPosition().x < stats().getPlayerPos().x) setSpriteDirection(1);
            else setSpriteDirection(-1);
        }
        if (!nearbyEnemies.isEmpty()) {
            float disperseTotal = 0;
            float disperseDist2 = (float) Math.pow(disperseDist, 2);
            for (Fixture enemy : nearbyEnemies) {
                disperseTotal += stats().getDisperseForce() *
                    (disperseDist2 - getPosition().dst2(enemy.getBody().getPosition())) / disperseDist2;
            }
            applyWeightedForce(getNearbyEnemyDirection().scl(-disperseTotal));
        }
        updateMovementType();
    }

    private void updateMovementType() {
        damping().set(stats().flyDampingX, stats().flyDampingY);
        if (stats().isPlayerSighted()) {
            getTargetPos().set(stats().getPlayerPos());
            float dst2 = stats().getPlayerPos().dst2(getPosition());
            if (dst2 < stats().getMinDst2()) {
                setMovementType(MovementType.FLEE);
                setSpeed(stats().runSpeed);
                setAcceleration(stats().acceleration);
            } else if (dst2 < stats().getMaxDst2()) {
                setMovementType(MovementType.CAUTION);
                setSpeed(stats().walkSpeed);
            } else {
                setMovementType(MovementType.APPROACH);
                setSpeed(stats().runSpeed);
            }
        } else {
            setMovementType(MovementType.IDLE);
            setSpeed(stats().walkSpeed);
        }
    }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        super.beginContact(entityFixture, contactFixture);
        if (getBody().getFixtureList().indexOf(entityFixture, true) == playerSensorIndex) {
            if (contactFixture.getUserData() instanceof Player) {
                stats().setPlayerInRange(true);
                stats().setPlayerPos(contactFixture.getBody().getPosition());
                stats().setPlayerVel(contactFixture.getBody().getLinearVelocity());
            }
        }
        if (getBody().getFixtureList().indexOf(entityFixture, true) == nearbyEnemySensorIndex) {
            if (nearbyEnemies.size() <= 5) nearbyEnemies.add(contactFixture);
        }
    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {
        super.endContact(entityFixture, contactFixture);
        if (getBody().getFixtureList().indexOf(entityFixture, true) == playerSensorIndex) {
            if (contactFixture.getUserData() instanceof Player) {
                stats().setPlayerInRange(false);
            }
        }
        if (getBody().getFixtureList().indexOf(entityFixture, true) == nearbyEnemySensorIndex) {
            nearbyEnemies.remove(contactFixture);
        }
    }

    @Override
    public void death() {
        for (int i = 0; i < stats().getCrystalLoot(); i++) {
            Crystal loot = new Crystal(getBody().getPosition().x, getBody().getPosition().y, 1f, 1f,
                getBody().getWorld(), animManager);
            float impulse = (float) Math.random() * 10 + 3;
            Vector2 dir = new Vector2((float) (Math.random() * 10 - 5), 3).nor();
            loot.getBody().applyLinearImpulse(dir.scl(impulse), new Vector2(0, 0), true);
        }
        new EnemyDeathVisual(getBody().getPosition().x, getBody().getPosition().y, animManager, getBody().getWorld());
        destroy();
    }

    @Override
    public boolean damage(float attackDamage, Vector2 attackOrigin, float knockback, Fixture entityFixture) {
        if (super.damage(attackDamage, attackOrigin, knockback, entityFixture)) {
            if (stats().isStunOnHit()) stats().setHitTimer(1f);
            return true;
        }
        return false;
    }

    @Override
    public EnemyStats stats() {
        return (EnemyStats) super.stats();
    }

    private Vector2 getNearbyEnemyDirection() {
        if (nearbyEnemies.isEmpty()) return new Vector2();
        Vector2 average = new Vector2(nearbyEnemies.get(0).getBody().getPosition());
        for (int i = 1; i < nearbyEnemies.size(); i++) {
            average.add(nearbyEnemies.get(i).getBody().getPosition());
        }
        average.scl(1f / nearbyEnemies.size());
        return average.sub(getPosition()).nor();
    }

    protected Vector2 getPredictedPlayerPos(float time) {
        return new Vector2(stats().getPlayerPos()).add(new Vector2(stats().getPlayerVel()).scl(time));
    }

    private static class PlayerLOSRay implements RayCastCallback{
        public boolean result;
        public PlayerLOSRay() {
            result = false;
        }

        public void reset() {
            result = false;
        }

        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            short bit = GameContactListener.FilterBits.STATIC.getBit();
            if ((fixture.getFilterData().categoryBits & bit) == bit) {
                result = true;
                return 0;
            }
            return -1;
        }
    }
}
