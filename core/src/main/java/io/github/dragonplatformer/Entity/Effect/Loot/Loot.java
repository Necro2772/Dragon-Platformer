package io.github.dragonplatformer.Entity.Effect.Loot;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.Effect;
import io.github.dragonplatformer.Entity.EffectManager;
import io.github.dragonplatformer.GameContactListener;

public abstract class Loot extends Effect {
    private boolean looted = false;
    public LootType type;
    public int value;
    private Vector2 playerPos = null;

    public Loot(float x, float y, float width, float height, World world, EffectManager effectManager,
                AnimationManager animManager, AnimationKey animKey,
                LootType type, int value) {
        super(x, y, width, height, animKey, effectManager, animManager, world);
        this.type = type;
        this.value = value;

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

        init();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (playerPos != null) {
            float mag = 15f;
            Vector2 dir = new Vector2(playerPos.x - getBody().getPosition().x,
                playerPos.y - getBody().getPosition().y).nor();
            getBody().applyForceToCenter(dir.scl(mag), true);
        }
        if (looted) destroy();
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

    public void setLooted() {
        this.looted = true;
    }

    public boolean isLooted() {
        return looted;
    }

}
