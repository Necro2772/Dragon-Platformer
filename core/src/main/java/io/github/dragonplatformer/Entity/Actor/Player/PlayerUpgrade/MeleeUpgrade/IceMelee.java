package io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.MeleeUpgrade;

import io.github.dragonplatformer.Entity.Actor.Player.DragonElement;
import io.github.dragonplatformer.Entity.Actor.Player.Player;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack.ProjectileReflect;
import io.github.dragonplatformer.Entity.EffectManager;

public class IceMelee extends MeleeUpgrade {
    public IceMelee(int rarity) {
        super(DragonElement.ICE, rarity);
        reflect = true;
    }

    @Override
    public void use(Player player, EffectManager effectManager, AnimationManager animManager) {
        super.use(player, effectManager, animManager);
        switch (player.getState()) {
            case ATTACK_GLIDE:
            case ATTACK_DIVE:
            case ATTACK_SOAR:
                new ProjectileReflect(
                    0.4f, 6, player.getHitboxPosition(),
                    effectManager, animManager, player.getBody()
                ).setSpawnDelay(0.1f).setReflect(true);
                break;
        }
    }
}
