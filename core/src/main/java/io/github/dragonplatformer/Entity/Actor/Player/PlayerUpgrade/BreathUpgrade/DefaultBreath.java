package io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.BreathUpgrade;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import io.github.dragonplatformer.Entity.Actor.Player.DragonElement;
import io.github.dragonplatformer.Entity.Actor.Player.Player;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile.Fireball;
import io.github.dragonplatformer.Entity.EffectManager;

public class DefaultBreath extends BreathUpgrade {
    public DefaultBreath(int rarity) {
        super(DragonElement.FIRE, rarity);
    }

    @Override
    public ParticleEffect getChargeParticle(EffectManager effectManager) {
        return effectManager.obtainChargeFirePlayer();
    }

    @Override
    public void use(Player player, EffectManager effectManager, AnimationManager animManager) {
        if (player.stats().getProjectileCD() <= 0) {
            Vector2 pos = getBreathStartPos(player);
            new Fireball(
                1, 3, 1, pos.x, pos.y, 1,
                player.getTargetPos(), effectManager, animManager, true, player.getBody().getWorld()
            );
            player.stats().resetProjectileCD();
            player.input().resetProjectileCharge();
        }
    }
}
