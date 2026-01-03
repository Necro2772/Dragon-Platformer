package io.github.dragonplatformer.Entity.AttackEffect;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.Entity;
import io.github.dragonplatformer.GameContactListener;

import java.util.Map;

public abstract class AttackEffect extends Entity {
    protected final Map<AttackState, Animation<TextureRegion>> anims;
    private float stateTime;
    private AttackState state;
    private float rotation;
    private final boolean destroyBody;
    private final Fixture fixture;
    private final Vector2 positionOffset;

    public AttackEffect(float x, float y, float width, float height, World world,
                        Map<AttackState, Animation<TextureRegion>> anims, short maskBits, short group,
                        Body body) {
        super(x, y, width, height, world, body);
        destroyBody = (body == null);
        PolygonShape collisionRec = new PolygonShape();
        if (body == null) {
            collisionRec.setAsBox(width / 2f, height / 2f);
            positionOffset = new Vector2(0, 0);
        }
        else {
            collisionRec.setAsBox(width / 2f, height / 2f, new Vector2(x, y), 0);
            positionOffset = new Vector2(x, y);
        }
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = collisionRec;
        fixtureDef.density = 0;
        fixtureDef.isSensor = true;
        Filter filter = new Filter();
        filter.categoryBits = GameContactListener.FilterBits.EFFECT.getBit();
        filter.maskBits = maskBits;
        filter.groupIndex = group;
        fixture = getBody().createFixture(fixtureDef);
        fixture.setFilterData(filter);
        fixture.setUserData(this);
        collisionRec.dispose();

        this.anims = anims;
        stateTime = 0;
        state = AttackState.IDLE;
        rotation = 0;
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

    public void destroy() {
        if (destroyBody) getBody().getWorld().destroyBody(getBody());
        else getBody().destroyFixture(fixture);
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

    public enum AttackState {
        IDLE,
        DESTROYED,
    }
}
