package io.github.dragonplatformer.Entity.AttackEffect;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.Creature.Creature;
import io.github.dragonplatformer.GameContactListener;

import java.util.Map;

public abstract class Projectile extends AttackEffect {

    public Projectile(float x, float y, float width, float height, int direction,
                      Map<AttackState, Animation<TextureRegion>> anims, boolean isPlayer, World world) {
        super(x, y, width, height, direction, anims, null, world);
        PolygonShape fixtureShape = new PolygonShape();
        fixtureShape.setAsBox(width / 2f, height / 2f, new Vector2(), 0);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.isSensor = true;
        fixtureDef.shape = fixtureShape;
        Filter filter = new Filter();
        filter.categoryBits = GameContactListener.FilterBits.EFFECT.getBit();
        if (isPlayer) {
            filter.maskBits = (short) (GameContactListener.FilterBits.EFFECT.getBit()
                + GameContactListener.FilterBits.ENEMY.getBit()
                + GameContactListener.FilterBits.STATIC.getBit());
            filter.groupIndex = GameContactListener.FilterGroup.PLAYERATTACK.getBit();
        } else {
            filter.maskBits = (short) (GameContactListener.FilterBits.EFFECT.getBit()
                + GameContactListener.FilterBits.PLAYER.getBit()
                + GameContactListener.FilterBits.STATIC.getBit());
            filter.groupIndex = GameContactListener.FilterGroup.ENEMYATTACK.getBit();
        }
        Fixture fixture = getBody().createFixture(fixtureDef);
        fixture.setFilterData(filter);
        fixture.setUserData(this);
        fixtureShape.dispose();

        getBody().setGravityScale(0);
    }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        if (contactFixture.getUserData() instanceof Creature) {
            Creature creature = ((Creature) contactFixture.getBody().getUserData());
            creature.damage(1, getBody().getPosition());
        }
        setState(AttackState.DESTROYED);
    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {

    }
}
