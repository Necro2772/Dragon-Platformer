package io.github.dragonplatformer.Entity.AttackEffect;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.Entity;

import java.util.Map;

public abstract class AttackEffect extends Entity {
    protected final Map<AttackState, Animation<TextureRegion>> anims;
    private float stateTime;
    private AttackState state;
    private float rotation;
    private Vector2 positionOffset;
    private final float damage;

    public AttackEffect(float damage, float x, float y, float width, float height, int direction,
                        Map<AttackState, Animation<TextureRegion>> anims, Body body, World world) {
        super(x, y, width, height, world, body);
        this.damage = damage;
        setDirection(direction);
        this.anims = anims;
        stateTime = 0;
        state = AttackState.IDLE;
        rotation = 0;
        positionOffset = new Vector2();
    }

    @Override
    public void act(float delta) {
        if (getState() == AttackState.DESTROYED) {
            destroy();
        }
    }

    @Override
    public void draw(SpriteBatch batch, float delta) {
        stateTime = stateTime + delta;
        if (anims.get(getState()) == null) {
            destroy();
            return;
        }
        if (anims.get(getState()).getPlayMode() == Animation.PlayMode.NORMAL
            && anims.get(getState()).isAnimationFinished(getStateTime())) {
            setState(AttackState.DESTROYED);
            if (anims.get(getState()) == null) {
                destroy();
                return;
            }
        }
        TextureRegion frame = anims.get(getState()).getKeyFrame(getStateTime());
        batch.draw(frame,
            getBody().getPosition().x + positionOffset.x - getWidth() / 2f,
            getBody().getPosition().y + positionOffset.y - getHeight() / 2f,
            getWidth() / 2f, getHeight() / 2f, getWidth(), getHeight(), getDirection(), 1, getRotation());
    }

    public void setPositionOffset(Vector2 posOffset) {
        this.positionOffset = posOffset;
    }

    public void destroy() {
        getBody().getWorld().destroyBody(getBody());
    }

    public AttackState getState() {
        return state;
    }

    public void setState(AttackState state) {
        this.state = state;
    }

    public float getStateTime() {
        return stateTime;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getRotation() {
        return rotation;
    }

    public float getDamage() {
        return damage;
    }

    public enum AttackState {
        IDLE,
        DESTROYED,
    }
}
