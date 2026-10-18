package io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade;

import io.github.dragonplatformer.Entity.Actor.Player.DragonElement;
import io.github.dragonplatformer.Entity.Actor.Player.Player;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.EffectManager;

public abstract class PlayerUpgrade {
    public final DragonElement element;
    public final int rarity;

    protected PlayerUpgrade(DragonElement element, int rarity) {
        this.element = element;
        this.rarity = rarity;
    }

    public void act(float delta, Player player, EffectManager effectManager, AnimationManager animManager) { };
    public abstract void use(Player player, EffectManager effectManager, AnimationManager animManager);
}
