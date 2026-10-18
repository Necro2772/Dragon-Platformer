package io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.EvadeUpgrade;

import com.badlogic.gdx.math.Vector2;
import io.github.dragonplatformer.Entity.Actor.Player.DragonElement;
import io.github.dragonplatformer.Entity.Actor.Player.Player;
import io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.PlayerUpgrade;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack.WindAura;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.VisualEffect.ProjectileShootVisual;
import io.github.dragonplatformer.Entity.EffectManager;

public abstract class EvadeUpgrade extends PlayerUpgrade {
    private WindAura windAura;
    private float charge;
    public EvadeUpgrade(DragonElement element, int rarity) {
        super(element, rarity);
    }

    public void evadeStart(Player player, EffectManager effectManager, AnimationManager animManager) {
        windAura = new WindAura(10, new Vector2(), effectManager, animManager, player.getBody());
        player.stats().setEvading(true);
        charge = 0;
    }

    @Override
    public void act(float delta, Player player, EffectManager effectManager, AnimationManager animManager) {
        charge += delta;
        super.act(delta, player, effectManager, animManager);
    }

    public void evadeEnd(Player player) {
        player.stats().setEvading(false);
        windAura.destroySafe();
        windAura = null;
        Vector2 dir = new Vector2(player.input().getInputDirection(), 0);
        if (player.input().upMove) {
            dir.y = 1;
        } else if (player.input.downMove) {
            dir.y = -1;
        }
        dir.nor();
        float chargeTime = 1;
        float dashForce = player.stats().evadeImpulse * Math.min(charge, chargeTime) / chargeTime;
        player.applyTimedImpulse(dir.scl(dashForce * 2), 0.2f);
        player.input().setGlide(true);
        player.stats().chargeGlide(0.5f);
        player.stats().chargeSoar(0.5f);
    }

    @Override
    public void use(Player player, EffectManager effectManager, AnimationManager animManager) {
//        switch (player.getState()) {
//            case EVADE_HORIZONTAL:
//                new ProjectileShootVisual(player.getPosition().x, player.getPosition().y, effectManager, animManager, player.getBody().getWorld());
//                int dir = player.input().getInputDirection();
//                if (dir == 0) dir = player.getSpriteDirection();
//                player.applyTimedImpulse(player.stats().evadeImpulse * dir, 0, 0.1f);
//                player.input().setGlide(true);
//                player.stats().chargeGlide(0.5f);
//                player.stats().chargeSoar(0.5f);
//                break;
//            case EVADE_UP:
//                new ProjectileShootVisual(player.getPosition().x, player.getPosition().y, effectManager, animManager, player.getBody().getWorld());
//                player.applyTimedImpulse(0, player.stats().evadeImpulse, 0.1f);
//                player.input().setGlide(true);
//                player.stats().chargeGlide(0.5f);
//                player.stats().chargeSoar(0.5f);
//                break;
//            case EVADE_DOWN:
//                new ProjectileShootVisual(player.getPosition().x, player.getPosition().y, effectManager, animManager, player.getBody().getWorld());
//                player.applyTimedImpulse(0, -player.stats().evadeImpulse, 0.1f);
//                player.input().setGlide(true);
//                player.stats().chargeGlide(0.5f);
//                player.stats().chargeSoar(0.5f);
//                break;
//        }
    }
}
