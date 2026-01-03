package io.github.dragonplatformer.Entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.GameContactListener;

public abstract class Creature extends Entity {
    private int groundContact;
    private CreatureStats stats;

    public Creature(float x, float y, float width, float height, World world) {
        super(x, y, width, height, world, null);
        PolygonShape collisionRec = new PolygonShape();
        collisionRec.setAsBox(width / 2f, height / 2f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = collisionRec;
        fixtureDef.density = 0.5f;
        getBody().createFixture(fixtureDef).setUserData(this);
        collisionRec.dispose();

        Filter sensorFilter = new Filter();
        sensorFilter.categoryBits = GameContactListener.FilterBits.SENSOR.getBit();
        sensorFilter.maskBits = GameContactListener.FilterBits.STATIC.getBit();
        PolygonShape jumpSensorShape = new PolygonShape();
        jumpSensorShape.setAsBox(width / 2f - 0.1f, 0.2f, new Vector2(0, -height / 2f), 0);
        FixtureDef jumpSensorDef = new FixtureDef();
        jumpSensorDef.shape = jumpSensorShape;
        jumpSensorDef.isSensor = true;
        Fixture sensorFixture = getBody().createFixture(jumpSensorDef);
        sensorFixture.setFilterData(sensorFilter);
        sensorFixture.setUserData(this);
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

    public void damage(int attackDamage, Vector2 attackOrigin) {
        getStats().setHealth(getStats().getHealth() - attackDamage);
        if (getStats().getHealth() <= 0) death();
    }

    public abstract void death();

    public CreatureStats getStats() {
        return stats;
    }


    public abstract class CreatureStats {
        private int maxHealth;
        private int health;

        public CreatureStats(int maxHealth) {
            setMaxHealth(maxHealth);
            setHealth(maxHealth);
        }

        public int getMaxHealth() {
            return maxHealth;
        }

        public void setMaxHealth(int maxHealth) {
            this.maxHealth = maxHealth;
        }

        public int getHealth() {
            return health;
        }

        public void setHealth(int health) {
            this.health = Math.min(health, getMaxHealth());
        }
    }
}
