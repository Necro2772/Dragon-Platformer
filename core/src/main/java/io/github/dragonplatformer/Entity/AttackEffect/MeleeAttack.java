package io.github.dragonplatformer.Entity.AttackEffect;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.Creature;

import java.util.Map;

public class MeleeAttack extends AttackEffect {
    public MeleeAttack(float x, float y, float width, float height, World world,
                       Map<AttackState, Animation<TextureRegion>> anims, short maskBits, short group,
                       Body body) {
        super(x, y, width, height, world, anims, maskBits, group, body);
    }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        if (contactFixture.getBody().getUserData() instanceof Creature) {
            Creature creature = ((Creature) contactFixture.getBody().getUserData());
            creature.damage(1, getBody().getPosition());
        }
    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {

    }
}
