package io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.MeleeUpgrade;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import io.github.dragonplatformer.Entity.Actor.Player.DragonElement;
import io.github.dragonplatformer.Entity.Actor.Player.Player;
import io.github.dragonplatformer.Entity.Actor.Player.PlayerState;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack.FireballExplosion;
import io.github.dragonplatformer.Entity.EffectManager;
import io.github.dragonplatformer.Entity.Entity;

import java.util.List;

public class FireMelee extends MeleeUpgrade {
    public FireMelee(int rarity) {
        super(DragonElement.FIRE, rarity);
    }

    @Override
    public void hitEffect(Player player, EffectManager effectManager, AnimationManager animationManager) {
        super.hitEffect(player, effectManager, animationManager);
        float recoil = -2;
        switch (player.getState()) {
            case ATTACK_DIVE:
            case ATTACK_GLIDE:
            case ATTACK_SOAR:
                new FireballExplosion(player.getPosition(), 1, 1, new Vector2(),
                    effectManager, animationManager, true, player.getBody().getWorld());
                player.applyWeightedImpulse(new Vector2(player.getBody().getLinearVelocity().scl(recoil)));
                player.setState(PlayerState.FLY);
                break;
        }
    }

    @Override
    public ParticleEffect getMeleeEffect(EffectManager effectManager) {
        return effectManager.obtainFireMelee();
    }
}
