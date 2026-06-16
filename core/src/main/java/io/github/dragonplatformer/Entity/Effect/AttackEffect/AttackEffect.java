package io.github.dragonplatformer.Entity.Effect.AttackEffect;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.Actor.Actor;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.Effect;
import io.github.dragonplatformer.Entity.Effect.EffectState;
import io.github.dragonplatformer.Entity.EffectManager;
import io.github.dragonplatformer.Entity.Entity;

import java.util.HashMap;
import java.util.Map;

public abstract class AttackEffect extends Effect {
    private final float damage;
    private final float knockback;
    private float rotation = 0;
    private Vector2 positionOffset = new Vector2();
    private float hitCD = -1;
    private int hitGroup = -1;
    private float hitTimer = Float.POSITIVE_INFINITY;
    private final Map<Fixture, Float> hitFixtures = new HashMap<>();
    private float hitStun = 0.1f;

    public AttackEffect(float damage, float knockback, float x, float y, float width, float height,
                        AnimationKey animKey, EffectManager effectManager, AnimationManager animManager, World world) {
        super(x, y, width, height, animKey, effectManager, animManager, world);
        this.knockback = knockback;
        this.damage = damage;
    }

    public AttackEffect(float damage, float knockback, float width, float height,
                        AnimationKey animKey, EffectManager effectManager, AnimationManager animManager, Body body) {
        super(width, height, animKey, effectManager, animManager, body);
        this.knockback = knockback;
        this.damage = damage;
    }

    @Override
    public void init() {
        super.init();
    }

    public void setHitCD(float hitCD) {
        this.hitCD = hitCD;
    }

    public float getHitGroupCD() {
        return hitCD;
    }

    public void setHitGroup(int hitGroup) {
        this.hitGroup = hitGroup;
    }

    public int getHitGroup() {
        return hitGroup;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        hitFixtures.replaceAll((fixture, timer) -> timer - delta);
        hitFixtures.forEach((fixture, timer) -> {
            if (timer - delta <= 0) {
                hit(fixture);
                if (hitTimer > 0) hitFixtures.replace(fixture, hitTimer);
            } else {
                hitFixtures.replace(fixture, timer - delta);
            }
        });
    }

    public void setPositionOffset(Vector2 posOffset) {
        this.positionOffset = posOffset;
    }

    public Vector2 getPositionOffset() {
        return new Vector2(positionOffset);
    }

    @Override
    public Vector2 getPosition() {
        return super.getPosition().add(getPositionOffset());
    }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        hitFixtures.put(contactFixture, 0f);
    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {
        hitFixtures.remove(contactFixture);
    }

    @Override
    protected void beginState() {
        super.beginState();
        if (this.state == EffectState.DESTROYED) {
            setRotation(0);
            if (getBody().getUserData() == this) getBody().setActive(false);
        }
    }

    public boolean hit(Fixture contactFixture) {
        if (contactFixture.getBody().getUserData() instanceof Actor) {
            Actor<?> actor = ((Actor<?>) contactFixture.getBody().getUserData());
            if (!actor.stats().getHitGroupInvul(getHitGroup())) {
                if (actor.damage(getDamage(), getPosition(), getKnockback(), contactFixture)) {
                    ((Entity<?>) getBody().getUserData()).setHitStunTimer(getHitStun());
                    actor.setHitStunTimer(getHitStun());
                    actor.stats().addHitGroupInvul(getHitGroup(), getHitGroupCD());
                    return true;
                } else return false;
            }
        }
        return true;
    }

    /**
     * Sets the rotation in degrees
     * @param rotation in degrees
     */
    public void setRotation(float rotation) {
        this.rotation = (rotation + 90) % 180 - 90;
    }

    public float getRotation() {
        return rotation;
    }

    public float getDamage() {
        return damage;
    }

    public float getKnockback() {
        return knockback;
    }

    public void setHitTimer(float hitTimer) {
        this.hitTimer = hitTimer;
    }

    public float getHitStun() {
        return hitStun;
    }

    public void setHitStun(float hitStun) {
        this.hitStun = hitStun;
    }
}
