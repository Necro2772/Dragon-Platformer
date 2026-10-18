package io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.BreathUpgrade;

import io.github.dragonplatformer.Entity.Actor.Player.DragonElement;
import io.github.dragonplatformer.Entity.Actor.Player.Player;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.EffectManager;

public class EarthBreath extends BreathUpgrade {
    public EarthBreath(int rarity) {
        super(DragonElement.EARTH, rarity);
    }

    @Override
    public void use(Player player, EffectManager effectManager, AnimationManager animManager) {

    }
}
