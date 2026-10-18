package io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.BreathUpgrade;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import io.github.dragonplatformer.Entity.Actor.Player.DragonElement;
import io.github.dragonplatformer.Entity.Actor.Player.Player;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile.IceSpike;
import io.github.dragonplatformer.Entity.EffectManager;

public class IceBreath extends BreathUpgrade {
    public IceBreath(int rarity) {
        super(DragonElement.ICE, rarity);
    }

    @Override
    public void use(Player player, EffectManager effectManager, AnimationManager animManager) {
        if (player.stats().getProjectileCD() <= 0) {
            new IceSpike(1, 1, getBreathStartPos(player).x, getBreathStartPos(player).y,
                player.getTargetPos(), effectManager, animManager, true, player.getBody().getWorld());
            player.stats().resetProjectileCD();
            player.input().resetProjectileCharge();
        }
    }

    @Override
    public ParticleEffect getChargeParticle(EffectManager effectManager) {
        return effectManager.obtainChargeIcePlayer();
    }
}
