package io.github.dragonplatformer.Entity.AttackEffect;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.Creature.Creature;

import java.util.Map;

public class Projectile extends AttackEffect {
    int attackDamage;

    public Projectile(float x, float y, float width, float height, World world,
                      Map<AttackState, Animation<TextureRegion>> anims, int attackDamage,
                      short maskBits, short group) {
        super(x, y, width, height, world, anims, maskBits, group, null);
        getBody().setGravityScale(0);
        this.attackDamage = attackDamage;
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
