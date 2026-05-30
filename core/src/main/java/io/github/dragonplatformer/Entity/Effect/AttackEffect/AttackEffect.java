package io.github.dragonplatformer.Entity.Effect.AttackEffect;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.Effect;
import io.github.dragonplatformer.Entity.Effect.EffectState;

public abstract class AttackEffect extends Effect {
    protected boolean useHitEffect;
    protected int hitCount;
    private float rotation;
    private Vector2 positionOffset;
    private final float damage;
    private final float knockback;
    private float hitCD;
    private int hitGroup;

    public AttackEffect(float damage, float knockback, float x, float y, float width, float height,
                        AnimationKey animKey, AnimationManager animManager, World world) {
        super(x, y, width, height, animKey, animManager, world);
        this.knockback = knockback;
        this.damage = damage;
    }

    public AttackEffect(float damage, float knockback, float width, float height,
                        AnimationKey animKey, AnimationManager animManager, Body body) {
        super(width, height, animKey, animManager, body);
        this.knockback = knockback;
        this.damage = damage;
    }

    @Override
    public void init() {
        super.init();
        useHitEffect = false;
        rotation = 0;
        positionOffset = new Vector2();
        hitCD = -1;
        hitGroup = -1;
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
        if (useHitEffect) {
            onHit();
            useHitEffect = false;
        }
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

    public void onHit() { }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        hitCount++;
        if (hitCount == 1) useHitEffect = true;
    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {

    }

    @Override
    protected void beginState() {
        super.beginState();
        if (this.state == EffectState.DESTROYED) {
            setRotation(0);
            if (getBody().getUserData() == this) getBody().setActive(false);
        }
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
}
