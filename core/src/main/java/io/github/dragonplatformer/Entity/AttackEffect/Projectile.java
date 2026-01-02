package io.github.dragonplatformer.Entity.AttackEffect;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.Player;

import java.util.Map;

public class Projectile extends AttackEffect {
    int attackDamage;

    public Projectile(float x, float y, float width, float height, World world,
                      Map<AttackState, Animation<TextureRegion>> anims, int attackDamage,
                      short maskBits) {
        super(x, y, width, height, world, anims, maskBits);
        getBody().setGravityScale(0);
        this.attackDamage = attackDamage;
    }

    @Override
    public void act(float delta) {
        if (getState() == AttackState.DESTROYED) {
            getBody().getWorld().destroyBody(getBody());
        }
    }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        if (contactFixture.getBody().getUserData() instanceof Player) {
            Player player = ((Player) contactFixture.getBody().getUserData());
            player.damage(1, getBody().getPosition());
        }
        setState(AttackState.DESTROYED);
    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {

    }
}
