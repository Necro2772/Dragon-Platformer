package io.github.dragonplatformer.Entity.Actor.Enemy;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.EnemyDeathVisual;
import io.github.dragonplatformer.Entity.Actor.Actor;
import io.github.dragonplatformer.Entity.Actor.Player.Player;
import io.github.dragonplatformer.Entity.Effect.Loot.Crystal;
import io.github.dragonplatformer.GameContactListener;

public abstract class Enemy extends Actor<EnemyState> {
    private final PlayerLOSRay losRay;
    private Vector2 playerSensorSize;
    private Vector2 playerSensorCenter;
    private int playerSensorIndex;
    private final FixtureDef playerSensorDef;

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

        this.setStats(new EnemyStats(1));
        playerSensorIndex = -1;
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
                endState();
                state = getState().nextState();
                stateTime = 0;
                beginState();
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
        stats().update(delta);
        if (stats().isPlayerSighted() && getState().isNonBlocking()) {
            if (getBody().getPosition().x < stats().getPlayerPos().x) setSpriteDirection(1);
            else setSpriteDirection(-1);
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
    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {
        super.endContact(entityFixture, contactFixture);
        if (getBody().getFixtureList().indexOf(entityFixture, true) == playerSensorIndex) {
            if (contactFixture.getUserData() instanceof Player) {
                stats().setPlayerInRange(false);
            }
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
    public boolean damage(float attackDamage, Vector2 attackOrigin, float knockback) {
        if (super.damage(attackDamage, attackOrigin, knockback)) {
            if (stats().isStunOnHit()) stats().setHitTimer(1f);
            return true;
        }
        return false;
    }

    @Override
    public EnemyStats stats() {
        return (EnemyStats) super.stats();
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
