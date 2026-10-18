package io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.EvadeUpgrade;

import com.badlogic.gdx.math.Vector2;
import io.github.dragonplatformer.Entity.Actor.Player.DragonElement;
import io.github.dragonplatformer.Entity.Actor.Player.Player;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack.ProjectileReflect;
import io.github.dragonplatformer.Entity.EffectManager;

public class IceEvade extends EvadeUpgrade {
    public IceEvade(int rarity) {
        super(DragonElement.ICE, rarity);
    }

    @Override
    public void use(Player player, EffectManager effectManager, AnimationManager animManager) {
        super.use(player, effectManager, animManager);
        new ProjectileReflect(0.2f, 6f, new Vector2(), effectManager, animManager, player.getBody());
    }
}
