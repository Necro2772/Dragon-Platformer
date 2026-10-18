package io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.MeleeUpgrade;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import io.github.dragonplatformer.Entity.Actor.Player.DragonElement;
import io.github.dragonplatformer.Entity.Actor.Player.Player;
import io.github.dragonplatformer.Entity.Actor.Player.PlayerState;
import io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.PlayerUpgrade;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack.EmptyMelee;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack.GlideAttack;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.VisualEffect.MeleeElementalVisual;
import io.github.dragonplatformer.Entity.EffectManager;
import io.github.dragonplatformer.Entity.Entity;

import java.util.Arrays;
import java.util.List;

public abstract class MeleeUpgrade extends PlayerUpgrade {
    protected boolean reflect = false;
    protected float hitboxSizeModifier = 1;

    public MeleeUpgrade(DragonElement element, int rarity) {
        super(element, rarity);
    }

    public void startMelee(Player player) {
        float angle = new Vector2(player.getTargetPos()).sub(player.getPosition()).angleDeg();
        if (player.input().isGliding()) {
            if (player.input.downMove && !player.isGrounded()) {
                player.setState(PlayerState.ATTACK_DIVE);
            } else if (player.input.upMove) {
                player.setState(PlayerState.ATTACK_SOAR);
            } else {
                player.setState(PlayerState.ATTACK_GLIDE);
            }
        } else {
            if (angle > 90 && angle < 270) player.setSpriteDirection(-1);
            else player.setSpriteDirection(1);
            if (angle > 45 && angle < 135) {
                player.setState(PlayerState.ATTACK_UP);
            } else if (angle > 225 && angle < 315 && !player.isGrounded()) {
                player.setState(PlayerState.ATTACK_DOWN);
            } else if (player.isGrounded()) {
                if (player.getState() == PlayerState.ATTACK_GROUND1) player.setState(PlayerState.ATTACK_GROUND2);
                else player.setState(PlayerState.ATTACK_GROUND1);
            } else {
                if (player.getState() == PlayerState.ATTACK_FORWARD1) player.setState(PlayerState.ATTACK_FORWARD2);
                else player.setState(PlayerState.ATTACK_FORWARD1);
            }
        }
    }

    @Override
    public void use(Player player, EffectManager effectManager, AnimationManager animManager) {
        List<Entity.PatrolPoint> curve = getMeleeCurve(player);
        if (!curve.isEmpty()) {
            new MeleeElementalVisual(player.getPosition().x, player.getPosition().y, player.getBody().getPosition(),
                player.getBody().getLinearVelocity(), getMeleeCurve(player), getMeleeEffect(effectManager),
                effectManager, animManager, player.getBody().getWorld());
        }
        switch (player.getState()) {
            case ATTACK_GROUND1:
            case ATTACK_FORWARD1:
            case ATTACK_GROUND2:
            case ATTACK_FORWARD2:
                new EmptyMelee(
                    1, 10, 0.3f,
                    3 * hitboxSizeModifier, 1.5f * hitboxSizeModifier,
                    player.getHitboxPosition().add(2f * player.getSpriteDirection(), 0.2f),
                    effectManager, animManager, player.getBody()
                ).setSpawnDelay(0.2f).setReflect(reflect);
                break;
            case ATTACK_DOWN:
                new EmptyMelee(
                    1, 10, 0.4f,
                    2 * hitboxSizeModifier, 2 * hitboxSizeModifier,
                    player.getHitboxPosition().add(0, -2), effectManager, animManager, player.getBody()
                ).setSpawnDelay(0.2f).setReflect(reflect);
                break;
            case ATTACK_UP:
                new EmptyMelee(
                    1, 10, 0.4f,
                    2 * hitboxSizeModifier, 2 * hitboxSizeModifier,
                    player.getHitboxPosition().add(0, 2), effectManager, animManager, player.getBody()
                ).setSpawnDelay(0.2f).setReflect(reflect);
                break;
            case ATTACK_GLIDE:
                player.applyTimedImpulse(new Vector2(player.getBody().getLinearVelocity()).setLength(15), 0.4f);
//                player.applyTimedImpulse(
//                    player.stats().attackGlideImpulseX * player.getSpriteDirection(), player.stats().attackGlideImpulseY, 0.1f
//                );
                new GlideAttack(
                    1, 15, 0.4f,
                    3 * hitboxSizeModifier, 3 * hitboxSizeModifier,
                    player.getHitboxPosition().add(1f * player.getSpriteDirection() * hitboxSizeModifier, 0),
                    90 + player.getSpriteDirection() * 90,
                    getGlideAttackEffect(effectManager),
                    effectManager, animManager, player.getBody()
                ).setSpawnDelay(0.2f).setReflect(reflect);
                player.stats().chargeGlide(-1);
                break;
            case ATTACK_DIVE:
                player.applyTimedImpulse(new Vector2(player.getBody().getLinearVelocity()).setLength(15), 0.4f);
//                player.applyTimedImpulse(0, -player.stats().attackDiveImpulse, 0.1f);
                new GlideAttack(
                    1, 15, 0.4f,
                    3 * hitboxSizeModifier, 3 * hitboxSizeModifier,
                    player.getHitboxPosition().add(0, -1 * hitboxSizeModifier), -90,
                    getGlideAttackEffect(effectManager),
                    effectManager, animManager, player.getBody()
                ).setSpawnDelay(0.2f).setReflect(reflect);
                player.stats().chargeGlide(-1);
                break;
            case ATTACK_SOAR:
                player.applyTimedImpulse(new Vector2(player.getBody().getLinearVelocity()).setLength(15), 0.4f);
//                player.applyTimedImpulse(0, player.stats().attackSoarImpulse, 0.1f);
                new GlideAttack(
                    1, 15, 0.4f,
                    3 * hitboxSizeModifier, 3 * hitboxSizeModifier,
                    player.getHitboxPosition().add(0, 1 * hitboxSizeModifier), 90,
                    getGlideAttackEffect(effectManager),
                    effectManager, animManager, player.getBody()
                ).setSpawnDelay(0.2f).setReflect(reflect);
                player.stats().chargeGlide(-1);
                player.stats().chargeSoar(-1);
                break;
        }
    }

    public ParticleEffect getMeleeEffect(EffectManager effectManager) {
        return effectManager.obtainNeutralMelee();
    }

    public ParticleEffect getGlideAttackEffect(EffectManager effectManager) {
        return effectManager.obtainGlideAttack();
    }

    public List<Entity.PatrolPoint> getMeleeCurve(Player player) {
        switch (player.getState()) {
            case ATTACK_GROUND1:
            case ATTACK_FORWARD1:
                return Arrays.asList(
                    new Entity.PatrolPoint(0.05f, new Vector2(1f * player.getSpriteDirection(), -1).scl(hitboxSizeModifier)),
                    new Entity.PatrolPoint(0.1f, new Vector2(3.5f * player.getSpriteDirection(), 0).scl(hitboxSizeModifier)),
                    new Entity.PatrolPoint(0.2f, new Vector2(2f * player.getSpriteDirection(), 0.5f).scl(hitboxSizeModifier)),
                    new Entity.PatrolPoint(0.25f, new Vector2(1f * player.getSpriteDirection(), 0f).scl(hitboxSizeModifier))
                );
            case ATTACK_GROUND2:
            case ATTACK_FORWARD2:
                return Arrays.asList(
                    new Entity.PatrolPoint(0.05f, new Vector2(1f * player.getSpriteDirection(), 1f).scl(hitboxSizeModifier)),
                    new Entity.PatrolPoint(0.1f, new Vector2(3.5f * player.getSpriteDirection(), 0f).scl(hitboxSizeModifier)),
                    new Entity.PatrolPoint(0.2f, new Vector2(2f * player.getSpriteDirection(), -1f).scl(hitboxSizeModifier)),
                    new Entity.PatrolPoint(0.25f, new Vector2(1f * player.getSpriteDirection(), -0.5f).scl(hitboxSizeModifier))
                );
            case ATTACK_UP:
                return Arrays.asList(
                    new Entity.PatrolPoint(0.05f, new Vector2(-0.5f * player.getSpriteDirection(), 1).scl(hitboxSizeModifier)),
                    new Entity.PatrolPoint(0.1f, new Vector2(0f * player.getSpriteDirection(), 2f).scl(hitboxSizeModifier)),
                    new Entity.PatrolPoint(0.2f, new Vector2(0.5f * player.getSpriteDirection(), 2.5f).scl(hitboxSizeModifier)),
                    new Entity.PatrolPoint(0.3f, new Vector2(1f * player.getSpriteDirection(), 1f).scl(hitboxSizeModifier))
                );
            case ATTACK_DOWN:
                return Arrays.asList(
                    new Entity.PatrolPoint(0.05f, new Vector2(-0.5f * player.getSpriteDirection(), 1f).scl(hitboxSizeModifier)),
                    new Entity.PatrolPoint(0.1f, new Vector2(0f * player.getSpriteDirection(), -2.5f).scl(hitboxSizeModifier)),
                    new Entity.PatrolPoint(0.2f, new Vector2(0.5f * player.getSpriteDirection(), -3f).scl(hitboxSizeModifier)),
                    new Entity.PatrolPoint(0.3f, new Vector2(1f * player.getSpriteDirection(), 0f).scl(hitboxSizeModifier))
                );
            default: return List.of();
        }
    }

    public void hitEffect(Player player, EffectManager effectManager, AnimationManager animationManager) {
    }
}
