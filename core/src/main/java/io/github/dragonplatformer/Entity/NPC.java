package io.github.dragonplatformer.Entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.GameContactListener;

public abstract class NPC extends Entity {
    private int groundContact;

    public NPC(float x, float y, float width, float height, World world) {
        super(x, y, width, height, world);
        PolygonShape collisionRec = new PolygonShape();
        collisionRec.setAsBox(width / 2f, height / 2f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = collisionRec;
        fixtureDef.density = 0.5f;
        getBody().createFixture(fixtureDef);
        collisionRec.dispose();

        Filter sensorFilter = new Filter();
        sensorFilter.categoryBits = GameContactListener.FilterBits.SENSOR.getBit();
        sensorFilter.maskBits = GameContactListener.FilterBits.STATIC.getBit();
        PolygonShape jumpSensorShape = new PolygonShape();
        jumpSensorShape.setAsBox(width / 2f, 0.2f, new Vector2(0, -height / 2f), 0);
        FixtureDef jumpSensorDef = new FixtureDef();
        jumpSensorDef.shape = jumpSensorShape;
        jumpSensorDef.isSensor = true;
        getBody().createFixture(jumpSensorDef).setFilterData(sensorFilter);
        groundContact = 0;
    }

    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        if (getBody().getFixtureList().indexOf(entityFixture, true) == 1) {
            groundContact++;
        }
    }

    public void endContact(Fixture entityFixture, Fixture contactFixture) {
        if (getBody().getFixtureList().indexOf(entityFixture, true) == 1) {
            groundContact--;
        }
    }

    public boolean isGrounded() {
        return groundContact > 0;
    }
}
