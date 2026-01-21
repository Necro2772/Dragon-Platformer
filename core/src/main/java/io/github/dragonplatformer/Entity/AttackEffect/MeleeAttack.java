package io.github.dragonplatformer.Entity.AttackEffect;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.Creature.Creature;
import io.github.dragonplatformer.Entity.Creature.Player;
import io.github.dragonplatformer.GameContactListener;

import java.util.Map;

public abstract class MeleeAttack extends AttackEffect {
    private final Fixture attackFixture;
    private float recoil;
    public MeleeAttack(float damage, float knockback, float width, float height, Vector2 offset, int direction, Map<AttackState, Animation<TextureRegion>> anims, Body body) {
        super(damage, knockback, 0, 0, width, height, direction, anims, body, body.getWorld());
        PolygonShape fixtureShape = new PolygonShape();
        fixtureShape.setAsBox(width / 2f, height / 2f, offset, 0);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.isSensor = true;
        fixtureDef.shape = fixtureShape;
        Filter filter = new Filter();
        filter.categoryBits = GameContactListener.FilterBits.EFFECT.getBit();
        if (body.getUserData() instanceof Player) {
            filter.maskBits = (short)(GameContactListener.FilterBits.EFFECT.getBit()
                + GameContactListener.FilterBits.ENEMY.getBit());
            filter.groupIndex = GameContactListener.FilterGroup.PLAYERATTACK.getBit();
        }
        else {
            filter.maskBits = (short)(GameContactListener.FilterBits.EFFECT.getBit()
                + GameContactListener.FilterBits.PLAYER.getBit());
            filter.groupIndex = GameContactListener.FilterGroup.ENEMYATTACK.getBit();
        }
        attackFixture = getBody().createFixture(fixtureDef);
        attackFixture.setFilterData(filter);
        attackFixture.setUserData(this);
        fixtureShape.dispose();

        setPositionOffset(offset);
        recoil = 0;
    }

    public void setRecoil(float recoil) {
        this.recoil = recoil;
    }

    @Override
    public void destroy() {
        getBody().destroyFixture(this.attackFixture);
    }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        if (contactFixture.getBody().getUserData() instanceof Creature) {
            Creature creature = ((Creature) contactFixture.getBody().getUserData());
            creature.damage(getDamage(), getBody().getPosition(), getKnockback());
        }
        if (getBody().getUserData() instanceof Player) {
            Player player = ((Player) getBody().getUserData());
            player.recoil(contactFixture.getBody().getPosition(), recoil);
        }
    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {

    }
}
