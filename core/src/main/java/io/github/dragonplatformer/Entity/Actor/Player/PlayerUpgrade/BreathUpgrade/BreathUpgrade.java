package io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.BreathUpgrade;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.math.Vector2;
import io.github.dragonplatformer.Entity.Actor.Player.DragonElement;
import io.github.dragonplatformer.Entity.Actor.Player.Player;
import io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.PlayerUpgrade;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.EffectManager;

public abstract class BreathUpgrade extends PlayerUpgrade {
    ParticleEffect chargeEffect;
    public BreathUpgrade(DragonElement element, int rarity) {
        super(element, rarity);
    }

    public Vector2 getBreathStartPos(Player player) {
        return new Vector2(
            player.getBody().getPosition().x + player.getSpriteDirection() * 2,
            player.getBody().getPosition().y - 0.25f
        );
    }

    @Override
    public void act(float delta, Player player, EffectManager effectManager, AnimationManager animManager) {
        super.act(delta, player, effectManager, animManager);
        if (player.input().getUseProjectile()) {
            Vector2 pos = getBreathStartPos(player);
            float angle = new Vector2(player.getTargetPos()).sub(player.getPosition()).angleDeg();
            if (chargeEffect == null) {
                chargeEffect = getChargeParticle(effectManager);
                if (chargeEffect == null) return;
                chargeEffect.setPosition(pos.x, pos.y);
                chargeEffect.start();
            }
            chargeEffect.setPosition(pos.x, pos.y);
            for (ParticleEmitter emitter : chargeEffect.getEmitters()) {
                emitter.getAngle().setHigh(angle);
            }
        } else {
            if (chargeEffect != null) {
                chargeEffect.allowCompletion();
                chargeEffect = null;
            }
        }
    }

    public ParticleEffect getChargeParticle(EffectManager effectManager) {
        return null;
    }
}
