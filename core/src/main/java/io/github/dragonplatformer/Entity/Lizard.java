package io.github.dragonplatformer.Entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AttackEffect.AttackEffect;
import io.github.dragonplatformer.Entity.AttackEffect.Projectile;
import io.github.dragonplatformer.GameContactListener;

import java.util.Map;

public class Lizard extends Enemy {
    final Map<AttackEffect.AttackState, Animation<TextureRegion>> fireballAnims;
    private float attackCD;

    public Lizard(TextureAtlas atlas, float x, float y, float width, float height, World world) {
        super(x, y, width, height, world, Map.ofEntries(
            Map.entry(EnemyState.IDLE, new Animation<>(
                1/3f,
                atlas.findRegions("lizard_idle"),
                Animation.PlayMode.LOOP
                )),
            Map.entry(EnemyState.ATTACKING, new Animation<>(
                1/6f,
                atlas.findRegions("lizard_attack"),
                Animation.PlayMode.NORMAL
            ))
            )
        );
        stats = new LizardStats();

        fireballAnims = Map.ofEntries(Map.entry(AttackEffect.AttackState.IDLE, new Animation<>(
                1/3f,
                atlas.findRegions("fireball"),
                Animation.PlayMode.LOOP
            ))
        );

    }

    @Override
    public void act(float delta) {
        int attackSpd = 15;
        attackCD -= delta;
        if (getPlayerSighted() && attackCD < 0) {
            setState(EnemyState.ATTACKING, EnemyState.IDLE);
            attackCD = 1;
            float posx = getBody().getPosition().x;
            float posy = getBody().getPosition().y;
            Vector2 dir = new Vector2(getPlayerPos().x - posx, getPlayerPos().y - posy).nor();
            dir.scl(attackSpd);
            Projectile fireball = new Projectile(getBody().getPosition().x, getBody().getPosition().y,
                1, 1, getBody().getWorld(), fireballAnims, 1,
                (short) (GameContactListener.FilterBits.PLAYER.getBit()
                    + GameContactListener.FilterBits.STATIC.getBit()));
            fireball.getBody().applyLinearImpulse(dir.x, dir.y, 0, 0, true);
            fireball.setRotation(dir.angleDeg());
        }
    }

    public class LizardStats extends EnemyStats {
        LizardStats() {
            super(10);
        }
    }
}
