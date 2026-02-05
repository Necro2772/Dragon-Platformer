package io.github.dragonplatformer.Entity.Loot;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Entity;
import io.github.dragonplatformer.GameContactListener;

public abstract class Loot extends Entity {
    private final Animation<TextureRegion> animation;
    private boolean looted;
    private float stateTime;
    public LootType type;
    public int value;
    private Vector2 playerPos;

    public Loot(float x, float y, float width, float height, World world,
                AnimationManager animManager, AnimationManager.AnimationKeys animKey,
                LootType type, int value) {
        super(x, y, width, height, world);
        this.animation = animManager.getLootAnim(animKey);
        this.type = type;
        stateTime = 0;
        looted = false;
        this.value = value;
        playerPos = null;

        CircleShape collisionShape = new CircleShape();
        collisionShape.setRadius (width / 2f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = collisionShape;
        fixtureDef.density = 1f;
        Fixture fixture = getBody().createFixture(fixtureDef);
        fixture.setUserData(this);
        fixture.getFilterData().groupIndex = GameContactListener.FilterGroup.LOOT.getBit();
        fixture.getFilterData().categoryBits = GameContactListener.FilterBits.LOOT.getBit();
        fixture.getFilterData().maskBits = (short) (
            GameContactListener.FilterBits.STATIC.getBit()
            + GameContactListener.FilterBits.SENSOR.getBit()
        );
        collisionShape.dispose();

        CircleShape sensorShape = new CircleShape();
        sensorShape.setRadius (1 + width / 2f);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.shape = sensorShape;
        sensorDef.density = 0;
        sensorDef.isSensor = true;
        Fixture sensorFixture = getBody().createFixture(sensorDef);
        sensorFixture.setUserData(this);
        sensorFixture.getFilterData().groupIndex = GameContactListener.FilterGroup.LOOT.getBit();
        sensorFixture.getFilterData().categoryBits = GameContactListener.FilterBits.SENSOR.getBit();
        sensorFixture.getFilterData().maskBits = GameContactListener.FilterBits.PLAYER.getBit();
        sensorShape.dispose();
    }

    @Override
    public void act(float delta) {
        if (playerPos != null) {
            float mag = 15f;
            Vector2 dir = new Vector2(playerPos.x - getBody().getPosition().x,
                playerPos.y - getBody().getPosition().y).nor();
            getBody().applyForceToCenter(dir.scl(mag), true);
        }
    }

    @Override
    public void draw(SpriteBatch batch, float delta) {
        if (looted) getBody().getWorld().destroyBody(getBody());
        TextureRegion frame = animation.getKeyFrame(getStateTime());
        batch.draw(frame,
            this.getBody().getPosition().x - getWidth() / 2f,
            this.getBody().getPosition().y - getHeight() / 2f,
            getWidth() / 2f, getHeight() / 2f, getWidth(), getHeight(), getDirection(), 1, 0);
    }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {

    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {

    }

    public void moveToPlayer(Vector2 playerPos) {
        this.playerPos = playerPos;
    }

    public void stopMove() {
        this.playerPos = null;
    }

    public float getStateTime() {
        return stateTime;
    }

    public void setStateTime(float stateTime) {
        this.stateTime = stateTime;
    }

    public void setLooted() {
        this.looted = true;
    }

    public boolean isLooted() {
        return looted;
    }

    public enum LootType {
        CRYSTAL
    }
}
