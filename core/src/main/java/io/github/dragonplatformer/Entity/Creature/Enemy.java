package io.github.dragonplatformer.Entity.Creature;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.AttackEffect.EnemyDeathVisual;
import io.github.dragonplatformer.Entity.Loot.Crystal;
import io.github.dragonplatformer.GameContactListener;

import java.util.Map;

public abstract class Enemy extends Creature {
    protected final Map<EnemyState, Animation<TextureRegion>> anims;
    protected final EnemyStats stats;
    protected final AnimationManager animManager;
    private EnemyState state;
    private EnemyState bufferedState;
    private float stateTime;
    private boolean playerSighted;
    private boolean playerInRange;
    private Vector2 playerPos;
    private Vector2 playerVel;
    private Vector2 spawnPoint;
    private final PlayerLOSRay losRay;
    private float aggroRange;
    private Vector2 playerSensorSize;
    private Vector2 playerSensorCenter;
    private int playerSensorIndex;
    private boolean stunOnHit;
    private int crystalLoot;

    private final FixtureDef playerSensorDef;

    public Enemy(float x, float y, float width, float height, World world,
                 AnimationManager animManager, AnimationManager.AnimationKeys animKey) {
        super(x, y, width, height, world);
        setAsEnemy();

        playerSensorDef = new FixtureDef();
        playerSensorDef.filter.categoryBits = GameContactListener.FilterBits.SENSOR.getBit();
        playerSensorDef.filter.maskBits = GameContactListener.FilterBits.PLAYER.getBit();
        playerSensorDef.isSensor = true;

        this.animManager = animManager;
        this.anims = animManager.getEnemyAnims(animKey);
        setState(EnemyState.IDLE);
        stateTime = 0;
        playerSighted = false;
        spawnPoint = new Vector2(x, y);
        playerPos = new Vector2(0, 0);
        playerVel = new Vector2(0, 0);
        losRay = new PlayerLOSRay();

        playerSensorSize = new Vector2(15, 10);
        playerSensorCenter = new Vector2(0, -playerSensorSize.y/2);

        aggroRange = playerSensorSize.x * 2;
        stats = new EnemyStats(1);
        this.setStats(stats);
        playerSensorIndex = -1;
        stunOnHit = true;
        crystalLoot = 0;
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
        aggroRange = playerSensorSize.x * 2;
    }

    public void setPlayerSensorShape(Vector2 playerSensorSize, Vector2 playerSensorCenter) {
        this.playerSensorSize = playerSensorSize;
        this.playerSensorCenter = playerSensorCenter;
        aggroRange = playerSensorSize.x * 2;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (stats().getHealth() <= 0) {
            this.state = EnemyState.DEATH;
            stateTime = 0;
        }
        if (playerInRange && !playerSighted) {
            losRay.reset();
            getBody().getWorld().rayCast(losRay, getBody().getPosition(), playerPos);
            if (!losRay.result) playerSighted = true;
        }
        if (playerSighted && !playerInRange) {
            if (getBody().getPosition().dst(playerPos) > aggroRange) playerSighted = false;
        }
        stats.updateCooldowns(delta);
        if (playerSighted && getState().getFacePlayer()) {
            if (getBody().getPosition().x < getPlayerPos().x) setDirection(1);
            else setDirection(-1);
        }
    }

    public void setLoot(int numCrystals) {
        this.crystalLoot = numCrystals;
    }

    public void setAggroRange(float aggroRange) {
        this.aggroRange = aggroRange;
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

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        super.beginContact(entityFixture, contactFixture);
        if (getBody().getFixtureList().indexOf(entityFixture, true) == playerSensorIndex) {
            if (contactFixture.getUserData() instanceof Player) {
                setPlayerInRange(true);
                playerPos = contactFixture.getBody().getPosition();
                playerVel = contactFixture.getBody().getLinearVelocity();
            }
        }
    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {
        super.endContact(entityFixture, contactFixture);
        if (getBody().getFixtureList().indexOf(entityFixture, true) == playerSensorIndex) {
            if (contactFixture.getUserData() instanceof Player) {
                setPlayerInRange(false);
            }
        }
    }

    @Override
    public void draw(SpriteBatch batch, float delta) {
        stateTime = stateTime + delta;
        if (anims.get(getState()) == null || anims.get(getState()).isAnimationFinished(getStateTime())) {
            if (getState() == EnemyState.DEATH) {
                death();
                return;
            }
            else if (getBufferedState() != null) {
                endState();
                state = getBufferedState();
                bufferedState = null;
                stateTime = 0;
                beginState();
            }
        }
        TextureRegion frame = anims.get(getState()).getKeyFrame(getStateTime());
        batch.draw(frame,
            this.getBody().getPosition().x - getWidth() / 2f,
            this.getBody().getPosition().y - getHeight() / 2f,
            getCenter().x, getCenter().y, getWidth(), getHeight(), getDirection(), 1, 0);
    }

    @Override
    public void death() {
        for (int i = 0; i < crystalLoot; i++) {
            Crystal loot = new Crystal(getBody().getPosition().x, getBody().getPosition().y, 1f, 1f,
                getBody().getWorld(), animManager);
            float impulse = (float) Math.random() * 10 + 3;
            Vector2 dir = new Vector2((float) (Math.random() * 10 - 5), 3).nor();
            loot.getBody().applyLinearImpulse(dir.scl(impulse), new Vector2(0, 0), true);
        }
        new EnemyDeathVisual(getBody().getPosition().x, getBody().getPosition().y, animManager, getBody().getWorld());
        getBody().getWorld().destroyBody(getBody());
    }

    @Override
    public boolean damage(float attackDamage, Vector2 attackOrigin, float knockback) {
        if (super.damage(attackDamage, attackOrigin, knockback)) {
            if (stunOnHit) stats.setHitTimer(1f);
            return true;
        }
        return false;
    }

    public EnemyStats stats() {
        return stats;
    }

    protected Vector2 getPlayerPos() {
        return playerPos;
    }

    protected Vector2 getPlayerVel() {
        return playerVel;
    }

    protected Vector2 getPredictedPlayerPos(float time) {
        return new Vector2(getPlayerPos()).add(new Vector2(getPlayerVel()).scl(time));
    }

    public Vector2 getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(Vector2 spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    public boolean isPlayerInRange() {
        return playerInRange;
    }

    public void setStunOnHit(boolean stun) {
        this.stunOnHit = stun;
    }

    public void setPlayerInRange(boolean playerInRange) {
        this.playerInRange = playerInRange;
    }

    public enum EnemyState {
        IDLE,
        ATTACKING,
        DEATH,
        CHARGELUNGE,
        LUNGE (false, false),
        CHARGESHOOTPROJECTILE,
        SHOOTPROJECTILE (false, false),
        FLYIDLE (true),
        FLYCHARGELUNGE (true),
        FLYCHARGESHOOTPROJECTILE (true),
        FLYSHOOTPROJECTILE (true, false);

        private final boolean facePlayer;
        private final boolean flying;

        EnemyState() {
            this.facePlayer = true;
            this.flying = false;
        }

        EnemyState(boolean flying) {
            this.flying = flying;
            this.facePlayer = true;
        }

        EnemyState(boolean flying, boolean facePlayer) {
            this.flying = flying;
            this.facePlayer = facePlayer;
        }

        public boolean getFacePlayer() {
            return facePlayer;
        }

        public boolean isFlying() {
            return flying;
        }
    }

    public void beginState() {

    }

    public void endState() {

    }

    public boolean setState(EnemyState state) {
        if (state != this.state && getBufferedState() == null) {
            endState();
            this.state = state;
            stateTime = 0;
            beginState();
            return true;
        }
        return false;
    }

    public boolean setState(EnemyState state, boolean force) {
        if (force) {
            this.state = null;
            bufferedState = null;
        }
        return setState(state);
    }

    public void setState(EnemyState state, EnemyState bufferedState) {
        if (setState(state)) {
            this.bufferedState = bufferedState;
            stateTime = 0;
        }
    }

    public void setState(EnemyState state, EnemyState bufferedState, boolean force) {
        if (setState(state, force)) {
            this.bufferedState = bufferedState;
            stateTime = 0;
        }
    }

    public EnemyState getState() {
        return state;
    }

    public EnemyState getBufferedState() {
        return bufferedState;
    }

    public float getStateTime() {
        return stateTime;
    }

    public boolean getPlayerSighted() {
        return playerSighted;
    }

    public void setPlayerSighted(boolean playerSighted) {
        this.playerSighted = playerSighted;
    }

    public static class EnemyStats extends CreatureStats {
        private float hitTimer = 0;
        private float attackCD = 0;
        private float attackMaxCD;
        private float projectileSpd;
        private EnemyStats(int maxHealth) {
            super(maxHealth);
        }

        public void init(int maxHealth) {
            this.setMaxHealth(maxHealth);
            setHealth(getMaxHealth());
        }

        public void init(int maxHealth, float attackMaxCD, float projectileSpd) {
            this.setMaxHealth(maxHealth);
            setHealth(getMaxHealth());
            this.attackMaxCD = attackMaxCD;
            this.projectileSpd = projectileSpd;
        }

        public void updateCooldowns(float delta) {
            super.updateCooldowns(delta);
            if (getHitTimer() > 0) setHitTimer(getHitTimer() - delta);
            if (attackCD > 0) attackCD -= delta;
        }

        public void resetAttackCD() {
            attackCD = attackMaxCD;
        }

        public boolean getAttackOnCD() {
            return attackCD <= 0;
        }

        public float getHitTimer() {
            return hitTimer;
        }

        public void setHitTimer(float hitTimer) {
            this.hitTimer = hitTimer;
        }

        public float getProjectileSpd() {
            return projectileSpd;
        }
    }
}
