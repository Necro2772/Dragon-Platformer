package io.github.dragonplatformer.Entity.AttackEffect;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.Entity;
import io.github.dragonplatformer.GameContactListener;

import java.util.Map;

public abstract class AttackEffect extends Entity {
    protected final Map<AttackState, Animation<TextureRegion>> anims;
    private float stateTime;
    private AttackState state;
    private float rotation;

    public AttackEffect(float x, float y, float width, float height, World world,
                        Map<AttackState, Animation<TextureRegion>> anims, short maskBits) {
        super(x, y, width, height, world);
        PolygonShape collisionRec = new PolygonShape();
        collisionRec.setAsBox(width / 2f, height / 2f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = collisionRec;
        Filter filter = new Filter();
        filter.categoryBits = GameContactListener.FilterBits.EFFECT.getBit();
        filter.maskBits = maskBits;
        getBody().createFixture(fixtureDef).setFilterData(filter);
        collisionRec.dispose();

        this.anims = anims;
        stateTime = 0;
        state = AttackState.IDLE;
        rotation = 0;
    }

    @Override
    public void draw(SpriteBatch batch, float delta) {
        stateTime = stateTime + delta;
        TextureRegion frame = anims.get(getState()).getKeyFrame(getStateTime());
        batch.draw(frame,
            this.getBody().getPosition().x - getDirection() * getWidth() / 2f,
            this.getBody().getPosition().y - getHeight() / 2f,
            getWidth() / 2f, getHeight() / 2f, getWidth(), getHeight(), getDirection(), 1, getRotation());
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

    public enum AttackState {
        IDLE,
        DESTROYED,
    }
}
