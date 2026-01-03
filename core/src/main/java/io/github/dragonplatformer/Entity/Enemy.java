package io.github.dragonplatformer.Entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.GameContactListener;

import java.util.Map;

public abstract class Enemy extends Creature {
    protected final Map<EnemyState, Animation<TextureRegion>> anims;
    protected EnemyStats stats;
    private EnemyState state;
    private EnemyState bufferedState;
    private float stateTime;
    private boolean playerSighted;
    private Vector2 playerPos;

    public Enemy(float x, float y, float width, float height, World world, Map<EnemyState, Animation<TextureRegion>> anims) {
        super(x, y, width, height, world);
        getBody().getFixtureList().get(0).getFilterData().categoryBits = GameContactListener.FilterBits.ENEMY.getBit();

        // index 2: Player Sensor
        Filter playerFilter = new Filter();
        playerFilter.categoryBits = GameContactListener.FilterBits.SENSOR.getBit();
        playerFilter.maskBits = GameContactListener.FilterBits.PLAYER.getBit();
        PolygonShape playerSensorShape = new PolygonShape();
        playerSensorShape.setAsBox(15, 10, new Vector2(0, -height / 2f + 5), 0);
        FixtureDef playerSensorDef = new FixtureDef();
        playerSensorDef.shape = playerSensorShape;
        playerSensorDef.isSensor = true;
        getBody().createFixture(playerSensorDef).setFilterData(playerFilter);

        this.anims = anims;
        setState(EnemyState.IDLE);
        stateTime = 0;
        playerSighted = false;
        updatePlayerPos(new Vector2(0,0));
    }

    public abstract void act(float delta);

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        super.beginContact(entityFixture, contactFixture);
        if (getBody().getFixtureList().indexOf(entityFixture, true) == 2) {
            if (contactFixture.getUserData() instanceof Player) {
                setPlayerSighted(true);
                playerPos = contactFixture.getBody().getPosition();
            }
        }
    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {
        super.endContact(entityFixture, contactFixture);
        if (getBody().getFixtureList().indexOf(entityFixture, true) == 2) {
            if (contactFixture.getUserData() instanceof Player) {
                setPlayerSighted(false);
            }
        }
    }

    @Override
    public void draw(SpriteBatch batch, float delta) {
        stateTime = stateTime + delta;
        if (anims.get(getState()) == null) {
            if (getState() == EnemyState.DEATH) {
                getBody().getWorld().destroyBody(getBody());
                return;
            }
        }
        if (anims.get(getState()).isAnimationFinished(getStateTime())) {
            if (getState() == EnemyState.DEATH) {
                getBody().getWorld().destroyBody(getBody());
                return;
            }
            else if (getBufferedState() != null) {
                state = getBufferedState();
                bufferedState = null;
                stateTime = 0;
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
        setState(EnemyState.DEATH);
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

    public enum EnemyState {
        IDLE,
        ATTACKING,
        DEATH,
    }

    public void setState(EnemyState state) {
        if (state != this.state && getBufferedState() == null) {
            this.state = state;
            stateTime = 0;
        }
    }

    public void setState(EnemyState state, EnemyState bufferedState) {
        setState(state);
        this.bufferedState = bufferedState;
        stateTime = 0;
    }

    public boolean getPlayerSighted() {
        return playerSighted;
    }

    public void setPlayerSighted(boolean playerSighted) {
        this.playerSighted = playerSighted;
    }

    public abstract class EnemyStats extends CreatureStats {

        public EnemyStats(int maxHealth) {
            super(maxHealth);
        }
    }
}
