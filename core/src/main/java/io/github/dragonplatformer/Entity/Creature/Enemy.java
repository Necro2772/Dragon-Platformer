package io.github.dragonplatformer.Entity.Creature;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.AnimationManager;
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
    private Vector2 spawnPoint;
    private final PlayerLOSRay losRay;
    private float aggroRange;

    public Enemy(float x, float y, float width, float height, Vector2 hitboxSize, World world,
                 AnimationManager animManager, AnimationManager.AnimationKeys animKey,
                 Vector2 playerSensorHalfSize, boolean isFlying) {
        super(x, y, width, height, hitboxSize, world);
        getBody().getFixtureList().get(0).getFilterData().categoryBits = GameContactListener.FilterBits.ENEMY.getBit();
        getBody().getFixtureList().get(0).getFilterData().groupIndex = GameContactListener.FilterGroup.ENEMYATTACK.getBit();

        // index 2: Player Sensor
        Filter playerFilter = new Filter();
        playerFilter.categoryBits = GameContactListener.FilterBits.SENSOR.getBit();
        playerFilter.maskBits = GameContactListener.FilterBits.PLAYER.getBit();
        FixtureDef playerSensorDef = new FixtureDef();
        PolygonShape playerSensorShape = new PolygonShape();
        if (!isFlying) playerSensorShape.setAsBox(playerSensorHalfSize.x, playerSensorHalfSize.y,
            new Vector2(0, -height / 2f + 5), 0);
        else playerSensorShape.setAsBox(playerSensorHalfSize.x, playerSensorHalfSize.y);
        playerSensorDef.shape = playerSensorShape;
        playerSensorDef.isSensor = true;
        getBody().createFixture(playerSensorDef).setFilterData(playerFilter);
        playerSensorShape.dispose();

        this.animManager = animManager;
        this.anims = animManager.getEnemyAnims(animKey);
        setState(EnemyState.IDLE);
        stateTime = 0;
        playerSighted = false;
        spawnPoint = new Vector2(x, y);
        updatePlayerPos(new Vector2(0,0));
        losRay = new PlayerLOSRay();
        aggroRange = playerSensorHalfSize.x * 2;
        stats = new EnemyStats(1);
        this.setStats(stats);
    }

    @Override
    public void act(float delta) {
        if (getStats().getHealth() <= 0) {
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
        if (playerSighted) {
            if (getBody().getPosition().x < getPlayerPos().x) setDirection(1);
            else setDirection(-1);
        }
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
        if (getBody().getFixtureList().indexOf(entityFixture, true) == 2) {
            if (contactFixture.getUserData() instanceof Player) {
                setPlayerInRange(true);
                playerPos = contactFixture.getBody().getPosition();
            }
        }
    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {
        super.endContact(entityFixture, contactFixture);
        if (getBody().getFixtureList().indexOf(entityFixture, true) == 2) {
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
            getWidth() / 2f, getHeight() / 2f, getWidth(), getHeight(), getDirection(), 1, 0);
    }

    @Override
    public void death() {
        int numDrops = (int) (Math.random() * 2) + 1;
        for (int i = 0; i < numDrops; i++) {
            Crystal loot = new Crystal(getBody().getPosition().x, getBody().getPosition().y, 1f, 1f,
                getBody().getWorld(), animManager);
            float impulse = (float) Math.random() * 10 + 3;
            Vector2 dir = new Vector2((float) (Math.random() * 10 - 5), 3).nor();
            loot.getBody().applyLinearImpulse(dir.scl(impulse), new Vector2(0, 0), true);
        }
        getBody().getWorld().destroyBody(getBody());
    }

    @Override
    public void damage(int attackDamage, Vector2 attackOrigin) {
        if (super.damage(attackDamage, attackOrigin, 10)) {
            stats.hitTimer = 1f;
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

    public EnemyStats getStats() {
        return stats;
    }

    protected Vector2 getPlayerPos() {
        return playerPos;
    }

    protected void updatePlayerPos(Vector2 playerPos) {
        this.playerPos = playerPos;
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

    public void setPlayerInRange(boolean playerInRange) {
        this.playerInRange = playerInRange;
    }

    public enum EnemyState {
        IDLE,
        ATTACKING,
        DEATH,
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

    public void setState(EnemyState state, EnemyState bufferedState) {
        if (setState(state)) {
            this.bufferedState = bufferedState;
            stateTime = 0;
        }
    }

    public boolean getPlayerSighted() {
        return playerSighted;
    }

    public void setPlayerSighted(boolean playerSighted) {
        this.playerSighted = playerSighted;
    }

    public static class EnemyStats extends CreatureStats {
        public float hitTimer = 0;
        private EnemyStats(int maxHealth) {
            super(maxHealth);
        }

        public void init(int maxHealth) {
            this.setMaxHealth(maxHealth);
            setHealth(getMaxHealth());
        }

        public void updateCooldowns(float delta) {
            super.updateCooldowns(delta);
            if (hitTimer >= 0) hitTimer -= delta;
        }
    }
}
