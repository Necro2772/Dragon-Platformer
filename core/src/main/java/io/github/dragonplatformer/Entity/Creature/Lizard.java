package io.github.dragonplatformer.Entity.Creature;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.AttackEffect.AttackEffect;
import io.github.dragonplatformer.Entity.AttackEffect.Fireball;
import io.github.dragonplatformer.Entity.AttackEffect.Projectile;
import io.github.dragonplatformer.GameContactListener;

import java.util.Map;

public class Lizard extends Enemy {
    private float attackCD;

    public Lizard(float x, float y, float width, float height, World world, AnimationManager animManager) {
        super(x, y, width, height, new Vector2(width/2, height/2), world, animManager, AnimationManager.AnimationKeys.ENEMY_LIZARD,
            new Vector2(15, 10), false);
        getStats().init(4);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        int attackSpd = 15;
        float attackMaxCD = 1.5f;

        attackCD -= delta;
        if (getPlayerSighted() && attackCD < 0) {
            setState(EnemyState.ATTACKING, EnemyState.IDLE);
            attackCD = attackMaxCD;
            float posx = getBody().getPosition().x;
            float posy = getBody().getPosition().y;
            Vector2 dir = new Vector2(getPlayerPos().x - posx, getPlayerPos().y - posy).nor();
            dir.scl(attackSpd);
            Projectile fireball = new Fireball(1, 1, getBody().getPosition().x, getBody().getPosition().y,
                1, 1, getDirection(), animManager, false, getBody().getWorld());
            fireball.getBody().applyLinearImpulse(dir.x, dir.y, 0, 0, true);
            fireball.setRotation(dir.angleDeg());
        }
    }

//    public class LizardStats extends EnemyStats {
//        LizardStats() {
//            super(2);
//        }
//    }
}
