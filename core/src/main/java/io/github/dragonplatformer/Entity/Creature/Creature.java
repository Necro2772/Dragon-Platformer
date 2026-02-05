package io.github.dragonplatformer.Entity.Creature;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.Entity;
import io.github.dragonplatformer.GameContactListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Creature extends Entity {
    private final FixtureDef bodyFixtureDef;
    private final FixtureDef hitboxDef;
    private int jumpSensorIndex;
    private int hitboxIndex;
    private int ragdollIndex;
    private Vector2 hitboxSize;
    private Vector2 hitboxCenter;
    private int groundContact;
    private final ArrayList<Fixture> overlapFixtures;
    private float hitEffectTimer;
    protected CreatureStats stats;

    public Creature(float x, float y, float width, float height, World world) {
        super(x, y, width, height, world);
        bodyFixtureDef = new FixtureDef();
        bodyFixtureDef.density = 0.5f;
        bodyFixtureDef.friction = 0;
        bodyFixtureDef.filter.maskBits = GameContactListener.FilterBits.STATIC.getBit();
        hitboxSize = new Vector2(width/2, height/2);
        hitboxCenter = new Vector2(0, 0);
        hitboxDef = new FixtureDef();
        hitboxDef.isSensor = true;
        groundContact = 0;
        ragdollIndex = -1;
        jumpSensorIndex = -1;
        hitboxIndex = -1;
        overlapFixtures = new ArrayList<>();
        hitEffectTimer = 0;
    }

    public void init() {
        PolygonShape bodyCollision = new PolygonShape();
        bodyFixtureDef.shape = bodyCollision;
        bodyCollision.setAsBox(hitboxSize.x, hitboxSize.y, hitboxCenter, 0);

        Fixture bodyFixture = getBody().createFixture(bodyFixtureDef);
        bodyFixture.setUserData(this);

        PolygonShape jumpSensorShape = new PolygonShape();
        jumpSensorShape.setAsBox(hitboxSize.x - 0.1f, 0.2f, new Vector2(hitboxCenter.x, hitboxCenter.y - hitboxSize.y ), 0);
        FixtureDef jumpSensorDef = new FixtureDef();
        jumpSensorDef.shape = jumpSensorShape;
        jumpSensorDef.isSensor = true;
        jumpSensorDef.filter.categoryBits = GameContactListener.FilterBits.SENSOR.getBit();
        jumpSensorDef.filter.maskBits = GameContactListener.FilterBits.STATIC.getBit();
        Fixture sensorFixture = getBody().createFixture(jumpSensorDef);
        sensorFixture.setUserData(this);

        hitboxDef.shape = bodyCollision;
        Fixture hitboxFixture = getBody().createFixture(hitboxDef);
        hitboxFixture.setUserData(this);

        ragdollIndex = getBody().getFixtureList().indexOf(bodyFixture, true);
        jumpSensorIndex = getBody().getFixtureList().indexOf(sensorFixture, true);
        hitboxIndex = getBody().getFixtureList().indexOf(hitboxFixture, true);

        jumpSensorShape.dispose();
        bodyCollision.dispose();
    }

    public void setAsEnemy() {
        bodyFixtureDef.filter.categoryBits = GameContactListener.FilterBits.ENEMY.getBit();
        bodyFixtureDef.filter.groupIndex = GameContactListener.FilterGroup.ENEMYATTACK.getBit();
        hitboxDef.filter.categoryBits = GameContactListener.FilterBits.ENEMY.getBit();
        hitboxDef.filter.maskBits = (short)(GameContactListener.FilterBits.PLAYER.getBit() +
            GameContactListener.FilterBits.SENSOR.getBit() + GameContactListener.FilterBits.EFFECT.getBit());
        hitboxDef.filter.groupIndex = GameContactListener.FilterGroup.ENEMYATTACK.getBit();
    }

    public void setAsPlayer() {
        bodyFixtureDef.filter.categoryBits = GameContactListener.FilterBits.PLAYER.getBit();
        bodyFixtureDef.filter.groupIndex = GameContactListener.FilterGroup.PLAYERATTACK.getBit();
        getBody().setSleepingAllowed(false);
        hitboxDef.filter.categoryBits = GameContactListener.FilterBits.PLAYER.getBit();
        hitboxDef.filter.maskBits = (short)(GameContactListener.FilterBits.ENEMY.getBit()
            + GameContactListener.FilterBits.SENSOR.getBit() + GameContactListener.FilterBits.EFFECT.getBit());
        hitboxDef.filter.groupIndex = GameContactListener.FilterGroup.PLAYERATTACK.getBit();
    }

    public void setDensity(float density) {
        bodyFixtureDef.density = density;
    }

    public void setHitboxShape(Vector2 hitboxSize) {
        this.hitboxSize = hitboxSize;
        this.hitboxCenter = new Vector2(0, hitboxSize.y - getHeight() / 2);
    }

    public void setHitboxShape(Vector2 hitboxSize, Vector2 hitboxCenter) {
        this.hitboxSize = hitboxSize;
        this.hitboxCenter = hitboxCenter;
    }

    public Vector2 getCenter() {
        return new Vector2( getWidth() / 2 + hitboxCenter.x, getHeight() / 2 + hitboxCenter.y);
    }

    public void setStats(CreatureStats stats) {
        this.stats = stats;
    }

    @Override
    public void act(float delta) {
        if (!overlapFixtures.isEmpty()) {
            Vector2 avgPos = new Vector2(getBody().getPosition());
            float avgMass = getBody().getMass();
            for (Fixture fixture : overlapFixtures) {
                Creature c = (Creature) fixture.getUserData();
                avgPos.add(fixture.getBody().getPosition()).add(c.getCenter());
                avgMass += fixture.getBody().getMass();
            }
            avgPos.scl(1f / (overlapFixtures.size() + 1));
            avgMass /= (overlapFixtures.size() + 1);
            if (getBody().getMass() <= avgMass) {
                float forceX = 100;
                float maxVel = 3;
                if (getCenter().add(getBody().getPosition()).x < avgPos.x
                    && getBody().getLinearVelocity().x > -maxVel) {
                    getBody().applyForceToCenter(
                        (-forceX - getBody().getLinearVelocity().x) * getBody().getMass(), 0,
                        true
                    );
                } else if (getBody().getLinearVelocity().x < maxVel){
                    getBody().applyForceToCenter(
                        (forceX - getBody().getLinearVelocity().x) * getBody().getMass(), 0,
                        true
                    );
                }
            }
        }
        if (hitEffectTimer >= 0) hitEffectTimer -= delta;
    }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        if (getBody().getFixtureList().indexOf(entityFixture, true) == getJumpSensorIndex()) {
            groundContact++;
        }
        if (getBody().getFixtureList().indexOf(entityFixture, true) == getHitboxIndex()
            && contactFixture.getUserData() instanceof Creature) {
            overlapFixtures.add(contactFixture);
        }
    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {
        if (getBody().getFixtureList().indexOf(entityFixture, true) == getJumpSensorIndex()) {
            groundContact--;
        }
        if (getBody().getFixtureList().indexOf(entityFixture, true) == getHitboxIndex()) {
            overlapFixtures.remove(contactFixture);
        }
    }

    public boolean isGrounded() {
        return groundContact > 0;
    }

    public boolean damage(float attackDamage, Vector2 attackOrigin, float knockback) {
        if (!stats().getInvulnerable()) {
            stats().setHealth(stats().getHealth() - attackDamage);
            getBody().applyLinearImpulse(getBody().getLinearVelocity().scl(-1), getBody().getPosition(), true);
            if (attackOrigin.x - getBody().getPosition().x < 0) {
                getBody().applyLinearImpulse(
                    new Vector2(knockback, knockback).add(getBody().getLinearVelocity().scl(-2)),
                    getBody().getPosition(), true
                );
            } else {
                getBody().applyLinearImpulse(
                    new Vector2(-knockback, knockback).add(getBody().getLinearVelocity().scl(-2)),
                    getBody().getPosition(), true
                );
            }
            hitVisuals();
            return true;
        }
        return false;
    }

    private void hitVisuals() {
        this.hitEffectTimer = 0.15f;
    }

    public float getHitEffectTimer() {
        return hitEffectTimer;
    }

    public boolean getHitFlash() {
        return hitEffectTimer > 0;
    }

    public abstract void death();

    public CreatureStats stats() {
        return stats;
    }

    public int getJumpSensorIndex() {
        return jumpSensorIndex;
    }

    public int getHitboxIndex() {
        return hitboxIndex;
    }

    public int getRagdollIndex() {
        return ragdollIndex;
    }

    public abstract static class CreatureStats {
        private float maxHealth;
        private float health;
        private float invulnerability;
        private final Map<Integer, Float> hitMap;

        public CreatureStats(int maxHealth) {
            setMaxHealth(maxHealth);
            setHealth(maxHealth);
            invulnerability = 0;
            hitMap = new HashMap<>();
        }

        public boolean getHitGroupInvul(int hitGroup) {
            return hitMap.containsKey(hitGroup);
        }

        public void addHitGroupInvul(int hitGroup, float cooldown) {
            if (hitGroup != -1 && cooldown > 0) hitMap.put(hitGroup, cooldown);
        }

        public void updateCooldowns(float delta) {
            if (getInvulnerable()) setInvulnerability(invulnerability - delta);
            for (int group : hitMap.keySet()) {
                if (hitMap.get(group) - delta < 0) hitMap.remove(group);
                else hitMap.replace(group, hitMap.get(group) - delta);
            }
        }

        public float getMaxHealth() {
            return maxHealth;
        }

        public void setMaxHealth(float maxHealth) {
            this.maxHealth = maxHealth;
        }

        public float getHealth() {
            return health;
        }

        public void setHealth(float health) {
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
