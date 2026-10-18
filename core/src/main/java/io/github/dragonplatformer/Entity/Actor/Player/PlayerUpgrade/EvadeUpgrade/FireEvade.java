package io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.EvadeUpgrade;

import com.badlogic.gdx.math.Vector2;
import io.github.dragonplatformer.Entity.Actor.Player.DragonElement;
import io.github.dragonplatformer.Entity.Actor.Player.Player;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack.FireballExplosion;
import io.github.dragonplatformer.Entity.EffectManager;

public class FireEvade extends EvadeUpgrade {
    public FireEvade(int rarity) {
        super(DragonElement.FIRE, rarity);
    }

    @Override
    public void use(Player player, EffectManager effectManager, AnimationManager animManager) {
        super.use(player, effectManager, animManager);
        new FireballExplosion(player.getPosition(), 1, 1, new Vector2(), effectManager, animManager,
            true, player.getBody().getWorld());
    }
}
