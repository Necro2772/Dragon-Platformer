package io.github.dragonplatformer.Entity.AttackEffect;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.Creature.Creature;
import io.github.dragonplatformer.GameContactListener;

import java.util.Map;

public abstract class Projectile extends AttackEffect {
    private float health;

    public Projectile(float damage, float health, float x, float y, float width, float height, int direction,
                      Map<AttackState, Animation<TextureRegion>> anims, boolean isPlayer, World world) {
        super(damage, x, y, width, height, direction, anims, null, world);
        this.health = health;
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
    public void act(float delta) {
        super.act(delta);
        if (this.health <= 0) setState(AttackState.DESTROYED);
    }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        if (contactFixture.getUserData() instanceof Creature) {
            Creature creature = ((Creature) contactFixture.getBody().getUserData());
            creature.damage(getDamage(), getBody().getPosition());
            this.health = 0;
        } else if (contactFixture.getUserData() instanceof Projectile) {
            Projectile collideProjectile = (Projectile) contactFixture.getUserData();
            collideProjectile.health -= this.health;
            this.health -= collideProjectile.health;
        } else {
            this.health = 0;
        }
    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {

    }
}
