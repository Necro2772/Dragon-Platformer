package io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.BreathUpgrade;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import io.github.dragonplatformer.Entity.Actor.Player.DragonElement;
import io.github.dragonplatformer.Entity.Actor.Player.Player;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile.ExplosiveFireball;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile.Fireball;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.VisualEffect.ProjectileShootVisual;
import io.github.dragonplatformer.Entity.EffectManager;

public class FireBreath extends BreathUpgrade {
    public FireBreath(int rarity) {
        super(DragonElement.FIRE, rarity);
    }

    @Override
    public ParticleEffect getChargeParticle(EffectManager effectManager) {
        return effectManager.obtainChargeFirePlayer();
    }

    @Override
    public void use(Player player, EffectManager effectManager, AnimationManager animManager) {
        Vector2 pos = new Vector2(
            player.getBody().getPosition().x + player.getSpriteDirection() * 2,
            player.getBody().getPosition().y - 0.25f
        );
        float charge = player.input().getProjectileCharge();
        if (charge > 0.8f) {
            new ExplosiveFireball(1, 5, 5, pos.x, pos.y, 1,
                player.getTargetPos(), effectManager, animManager, true, player.getBody().getWorld());
            player.applyWeightedImpulse(player.getPosition().sub(player.getTargetPos())
                .setLength(player.stats().fireballLargeRecoil));
            new ProjectileShootVisual(pos.x - player.getSpriteDirection(), pos.y,
                effectManager, animManager, player.getBody().getWorld());
            player.stats().resetProjectileCD();
            player.input().resetProjectileCharge();
        } else if (player.stats().getProjectileCD() <= 0) {
            new Fireball(
                1, 3, 1, pos.x, pos.y, 1,
                player.getTargetPos(), effectManager, animManager, true, player.getBody().getWorld()
            );
            player.stats().resetProjectileCD();
            player.input().resetProjectileCharge();
        }
    }
}
