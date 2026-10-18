package io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.MeleeUpgrade;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import io.github.dragonplatformer.Entity.Actor.Player.DragonElement;
import io.github.dragonplatformer.Entity.Actor.Player.Player;
import io.github.dragonplatformer.Entity.EffectManager;
import io.github.dragonplatformer.Entity.Entity;

import java.util.List;

public class EarthMelee extends MeleeUpgrade {
    public EarthMelee(int rarity) {
        super(DragonElement.EARTH, rarity);
        hitboxSizeModifier = 1.5f;
    }

    @Override
    public ParticleEffect getMeleeEffect(EffectManager effectManager) {
        return effectManager.obtainEarthMelee();
    }

    @Override
    public ParticleEffect getGlideAttackEffect(EffectManager effectManager) {
        return effectManager.obtainEarthGlideAttack();
    }
}
