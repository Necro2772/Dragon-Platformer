package io.github.dragonplatformer.Entity.Creature;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.Entity;
import io.github.dragonplatformer.GameContactListener;

public abstract class Creature extends Entity {
    private int groundContact;
    protected CreatureStats stats;

    public Creature(float x, float y, float width, float height, Vector2 hitboxSize, World world) {
        super(x, y, width, height, world, null);
        PolygonShape collisionRec = new PolygonShape();
        collisionRec.setAsBox(hitboxSize.x, hitboxSize.y, new Vector2(0, (hitboxSize.y - height/2)), 0);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = collisionRec;
        fixtureDef.density = 0.5f;
        getBody().createFixture(fixtureDef).setUserData(this);
        collisionRec.dispose();

        Filter sensorFilter = new Filter();
        sensorFilter.categoryBits = GameContactListener.FilterBits.SENSOR.getBit();
        sensorFilter.maskBits = GameContactListener.FilterBits.STATIC.getBit();
        PolygonShape jumpSensorShape = new PolygonShape();
        jumpSensorShape.setAsBox(hitboxSize.x / 2f - 0.1f, 0.2f, new Vector2(0, -height / 2f), 0);
        FixtureDef jumpSensorDef = new FixtureDef();
        jumpSensorDef.shape = jumpSensorShape;
        jumpSensorDef.isSensor = true;
        Fixture sensorFixture = getBody().createFixture(jumpSensorDef);
        sensorFixture.setFilterData(sensorFilter);
        sensorFixture.setUserData(this);
        groundContact = 0;
    }

    public void setStats(CreatureStats stats) {
        this.stats = stats;
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

    public boolean damage(int attackDamage, Vector2 attackOrigin, float knockback) {
        if (!stats.getInvulnerable()) {
            getStats().setHealth(getStats().getHealth() - attackDamage);
            getBody().applyLinearImpulse(getBody().getLinearVelocity().scl(-1), getBody().getPosition(), true);
            if (attackOrigin.x - getBody().getPosition().x < 0) {
                getBody().applyLinearImpulse(new Vector2(knockback, knockback), getBody().getPosition(), true);
            } else {
                getBody().applyLinearImpulse(new Vector2(-knockback, knockback), getBody().getPosition(), true);
            }
            return true;
        }
        return false;
    }

    public abstract void damage(int attackDamage, Vector2 attackOrigin);

    public abstract void death();

    public CreatureStats getStats() {
        return stats;
    }


    public abstract static class CreatureStats {
        private int maxHealth;
        private int health;
        private float invulnerability;

        public CreatureStats(int maxHealth) {
            setMaxHealth(maxHealth);
            setHealth(maxHealth);
            invulnerability = 0;
        }

        public void updateCooldowns(float delta) {
            if (getInvulnerable()) setInvulnerability(invulnerability - delta);
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

        public void setInvulnerability(float time) {
            this.invulnerability = time;
        }

        public boolean getInvulnerable() {
            return this.invulnerability > 0;
        }
    }
}
