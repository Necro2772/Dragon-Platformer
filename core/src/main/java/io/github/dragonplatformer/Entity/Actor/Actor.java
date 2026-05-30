package io.github.dragonplatformer.Entity.Actor;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.AnimationEvent;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Entity;
import io.github.dragonplatformer.Entity.EntityState;
import io.github.dragonplatformer.GameContactListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Actor<T extends EntityState> extends Entity<T> {
    private final FixtureDef bodyFixtureDef;
    private final FixtureDef hitboxDef;
    private int jumpSensorIndex;
    private int hitboxIndex;
    private int ragdollIndex;
    private Vector2 hitboxSize;
    private float hitboxRadius;
    private Vector2 hitboxCenter;
    private int groundContact;
    private final ArrayList<Fixture> overlapFixtures;
    private float hitEffectTimer;
    private ActorStats stats;

    public Actor(float x, float y, float width, float height, Map<T, Animation<TextureRegion>> anims,
                 Map<T, List<AnimationEvent>> animEvents, AnimationManager animManager, World world) {
        super(x, y, width, height, anims, animEvents, animManager, world);
        bodyFixtureDef = new FixtureDef();
        bodyFixtureDef.density = 0.5f;
        bodyFixtureDef.friction = 0;
        bodyFixtureDef.filter.maskBits = GameContactListener.FilterBits.STATIC.getBit();
        hitboxSize = new Vector2(width/2, height/2);
        hitboxRadius = -1;
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
        Shape bodyCollision;
        if (hitboxRadius <= 0) {
            bodyCollision = new PolygonShape();
            ((PolygonShape) bodyCollision).setAsBox(hitboxSize.x, hitboxSize.y, hitboxCenter, 0);
        } else {
            bodyCollision = new CircleShape();
            bodyCollision.setRadius(hitboxRadius);
            ((CircleShape) bodyCollision).setPosition(hitboxCenter);
        }
        bodyFixtureDef.shape = bodyCollision;

        Fixture bodyFixture = getBody().createFixture(bodyFixtureDef);
        bodyFixture.setUserData(this);

        PolygonShape jumpSensorShape = new PolygonShape();
        if (hitboxRadius <= 0) {
            jumpSensorShape.setAsBox(hitboxSize.x - 0.1f, 0.2f,
                new Vector2(hitboxCenter.x, hitboxCenter.y - hitboxSize.y ), 0);
        } else {
            jumpSensorShape.setAsBox(hitboxRadius - 0.1f, 0.2f,
                new Vector2(hitboxCenter.x, hitboxCenter.y - hitboxRadius ), 0);
        }
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

    public void setMass(float mass) {
        float area;
        if (hitboxRadius <= 0) {
            area = hitboxSize.x * hitboxSize.y;
        } else {
            area = (float) (Math.pow(hitboxRadius, 2) * Math.PI);
        }
        setDensity(mass / area);
    }

    public void setHitboxShape(Vector2 hitboxSize) {
        this.hitboxSize = hitboxSize;
        this.hitboxCenter = new Vector2(0, hitboxSize.y - getHeight() / 2);
    }

    public void setHitboxShapeCircle(float radius, Vector2 offset) {
        this.hitboxCenter = offset;
        this.hitboxRadius = radius;
    }

    public void setHitboxShapeCircle(float radius) {
        this.hitboxRadius = radius;
        this.hitboxCenter = new Vector2(0, radius - (getHeight() / 2));
    }

    public void setHitboxShape(Vector2 hitboxSize, Vector2 hitboxCenter) {
        this.hitboxSize = hitboxSize;
        this.hitboxCenter = hitboxCenter;
    }

    public Vector2 getCenter() {
        return new Vector2( getWidth() / 2 + hitboxCenter.x, getHeight() / 2 + hitboxCenter.y);
    }

    public void setStats(ActorStats stats) {
        this.stats = stats;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stats().update(delta);
        if (!overlapFixtures.isEmpty()) {
            Vector2 avgPos = new Vector2(getBody().getPosition());
            float avgMass = getBody().getMass();
            for (Fixture fixture : overlapFixtures) {
                Actor<?> c = (Actor<?>) fixture.getUserData();
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

    public Vector2 getHitboxPosition() {
        return ((CircleShape) getBody().getFixtureList().get(getHitboxIndex()).getShape()).getPosition();
    }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        if (getBody().getFixtureList().indexOf(entityFixture, true) == getJumpSensorIndex()) {
            groundContact++;
        }
        if (getBody().getFixtureList().indexOf(entityFixture, true) == getHitboxIndex()
            && contactFixture.getUserData() instanceof Actor) {
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
            startHitVisuals();
            return true;
        }
        return false;
    }

    private void startHitVisuals() {
        this.hitEffectTimer = 0.15f;
    }

    public float getHitEffectTimer() {
        return hitEffectTimer;
    }

    public boolean getHitFlash() {
        return hitEffectTimer > 0;
    }

    public abstract void death();

    public ActorStats stats() {
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

}
