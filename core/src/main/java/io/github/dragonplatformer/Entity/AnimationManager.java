package io.github.dragonplatformer.Entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.dragonplatformer.Entity.Actor.Enemy.EnemyState;
import io.github.dragonplatformer.Entity.Actor.Player.PlayerState;
import io.github.dragonplatformer.Entity.Effect.EffectState;

import java.util.*;

public class AnimationManager {
    private final TextureAtlas atlas;
    private final Map<AnimationKey, Map<PlayerState, Animation<TextureRegion>>> playerAnimations;
    private final Map<PlayerState, List<AnimationEvent>> playerAnimationEvents;
    private final Map<AnimationKey, Map<EnemyState, Animation<TextureRegion>>> enemyAnimations;
    private final Map<AnimationKey, Map<EnemyState, List<AnimationEvent>>> enemyAnimationEvents;
    private final Map<AnimationKey, Map<EffectState, Animation<TextureRegion>>> effectAnimations;
    private final Map<AnimationKey, Map<EffectState, List<AnimationEvent>>> effectAnimationEvents;

    public AnimationManager(TextureAtlas atlas) {
        this.atlas = atlas;
        playerAnimations = new HashMap<>();
        playerAnimationEvents = new HashMap<>();
        enemyAnimations = new HashMap<>();
        enemyAnimationEvents = new HashMap<>();
        effectAnimations = new HashMap<>();
        effectAnimationEvents = new HashMap<>();
    }

    public Map<PlayerState, Animation<TextureRegion>> getPlayerAnimations() {
        AnimationKey key = AnimationKey.PLAYER;
        if (!playerAnimations.containsKey(key)) {
            playerAnimations.put(key, Map.ofEntries(
                Map.entry(PlayerState.IDLE, new Animation<>(
                    1/3f,
                    atlas.findRegions("dragon_idle"),
                    Animation.PlayMode.LOOP
                )), Map.entry(PlayerState.RUNNING, new Animation<>(
                    1/3f,
                    atlas.findRegions("dragon_run"),
                    Animation.PlayMode.LOOP_PINGPONG
                )), Map.entry(PlayerState.JUMPING, new Animation<>(
                    1/9f,
                    atlas.findRegions("dragon_flap"),
                    Animation.PlayMode.LOOP
                )), Map.entry(PlayerState.FLAPPING, new Animation<>(
                    1/9f,
                    atlas.findRegions("dragon_flap"),
                    Animation.PlayMode.LOOP
                )), Map.entry(PlayerState.FLYING, new Animation<>(
                    1/6f,
                    atlas.findRegions("dragon_fly"),
                    Animation.PlayMode.LOOP
                )), Map.entry(PlayerState.GLIDING, new Animation<>(
                    1/3f,
                    atlas.findRegions("dragon_glide"),
                    Animation.PlayMode.LOOP
                )), Map.entry(PlayerState.DIVING, new Animation<>(
                    1/3f,
                    atlas.findRegions("dragon_dive"),
                    Animation.PlayMode.LOOP
                )), Map.entry(PlayerState.SOAR, new Animation<>(
                    1/3f,
                    atlas.findRegions("dragon_divesoar"),
                    Animation.PlayMode.LOOP
                )), Map.entry(PlayerState.EVADE, new Animation<>(
                    1/4f,
                    atlas.findRegions("dragon_dash"),
                    Animation.PlayMode.LOOP
                )), Map.entry(PlayerState.EVADE_UP, new Animation<>(
                    1/4f,
                    atlas.findRegions("dragon_dash"),
                    Animation.PlayMode.LOOP
                )), Map.entry(PlayerState.EVADE_DOWN, new Animation<>(
                    1/4f,
                    atlas.findRegions("dragon_dash"),
                    Animation.PlayMode.LOOP
                )), Map.entry(PlayerState.EVADE_DASH, new Animation<>(
                    1/4f,
                    atlas.findRegions("dragon_dash"),
                    Animation.PlayMode.LOOP
                )), Map.entry(PlayerState.EVADE_DASH_UP, new Animation<>(
                    1/4f,
                    atlas.findRegions("dragon_dash"),
                    Animation.PlayMode.LOOP
                )), Map.entry(PlayerState.EVADE_DASH_DOWN, new Animation<>(
                    1/4f,
                    atlas.findRegions("dragon_dive"),
                    Animation.PlayMode.LOOP
                )), Map.entry(PlayerState.ATTACK_FORWARD1, new Animation<>(
                    0.25f,
                    atlas.findRegions("dragon_attackforward"),
                    Animation.PlayMode.NORMAL
                )), Map.entry(PlayerState.ATTACK_FORWARD2, new Animation<>(
                    0.25f,
                    atlas.findRegions("dragon_attackforward2"),
                    Animation.PlayMode.NORMAL
                )), Map.entry(PlayerState.ATTACK_UP, new Animation<>(
                    0.33f,
                    atlas.findRegions("dragon_attackup"),
                    Animation.PlayMode.NORMAL
                )), Map.entry(PlayerState.ATTACK_DOWN, new Animation<>(
                    0.33f,
                    atlas.findRegions("dragon_attackdown"),
                    Animation.PlayMode.NORMAL
                )), Map.entry(PlayerState.ATTACK_GLIDE, new Animation<>(
                    1.0f,
                    atlas.findRegions("dragon_attackforward3"),
                    Animation.PlayMode.NORMAL
                )), Map.entry(PlayerState.ATTACK_GLIDE_HIT, new Animation<>(
                    0.5f,
                    atlas.findRegions("dragon_attackforward3"),
                    Animation.PlayMode.NORMAL
                )), Map.entry(PlayerState.ATTACK_SOAR, new Animation<>(
                    0.73f,
                    atlas.findRegions("dragon_attackforward3"),
                    Animation.PlayMode.NORMAL
                )), Map.entry(PlayerState.ATTACK_DIVE, new Animation<>(
                    0.73f,
                    atlas.findRegions("dragon_attackforward3"),
                    Animation.PlayMode.NORMAL
                )), Map.entry(PlayerState.ATTACK_DIVE_LAND, new Animation<>(
                    0.73f,
                    atlas.findRegions("dragon_attackforward3"),
                    Animation.PlayMode.NORMAL
                )), Map.entry(PlayerState.ATTACK_GROUND1, new Animation<>(
                    0.25f,
                    atlas.findRegions("dragon_attackforward"),
                    Animation.PlayMode.NORMAL
                )), Map.entry(PlayerState.ATTACK_GROUND2, new Animation<>(
                    0.25f,
                    atlas.findRegions("dragon_attackforward2"),
                    Animation.PlayMode.NORMAL
                )), Map.entry(PlayerState.DEATH, new Animation<>(
                    1/2f,
                    atlas.findRegions("dragon_flap"),
                    Animation.PlayMode.NORMAL
                ))
            ));
        }
        return playerAnimations.get(key);
    }

    public Map<PlayerState, List<AnimationEvent>> getPlayerAnimEvents() {
        if (playerAnimationEvents.isEmpty()) {
            playerAnimationEvents.putAll(Map.ofEntries(
                Map.entry(PlayerState.EVADE, Arrays.asList(
                    new AnimationEvent(0 / 60f, "evadestart"),
                    new AnimationEvent(15 / 60f, "evadeend")
                )), Map.entry(PlayerState.EVADE_DOWN, Arrays.asList(
                    new AnimationEvent(0 / 60f, "evadestart"),
                    new AnimationEvent(15 / 60f, "evadeend")
                )), Map.entry(PlayerState.EVADE_UP, Arrays.asList(
                    new AnimationEvent(0 / 60f, "evadestart"),
                    new AnimationEvent(15 / 60f, "evadeend")
                )), Map.entry(PlayerState.EVADE_DASH, Arrays.asList(
                    new AnimationEvent(0 / 60f, "invstart"),
                    new AnimationEvent(15 / 60f, "invend")
                )), Map.entry(PlayerState.EVADE_DASH_UP, Arrays.asList(
                    new AnimationEvent(0 / 60f, "invstart"),
                    new AnimationEvent(15 / 60f, "invend")
                )), Map.entry(PlayerState.EVADE_DASH_DOWN, Arrays.asList(
                    new AnimationEvent(0 / 60f, "invstart"),
                    new AnimationEvent(15 / 60f, "invend")
                ))
            ));
        }
        return playerAnimationEvents;
    }

    public Map<EnemyState, Animation<TextureRegion>> getEnemyAnims(AnimationKey key) {
        if (!enemyAnimations.containsKey(key)) {
            switch (key) {
                case ENEMY_LIZARD:
                    enemyAnimations.put(key, Map.ofEntries(
                        Map.entry(EnemyState.IDLE, new Animation<>(
                            1 / 3f,
                            atlas.findRegions("lizard_idle"),
                            Animation.PlayMode.LOOP
                        )),
                        Map.entry(EnemyState.ATTACKING, new Animation<>(
                            1 / 6f,
                            atlas.findRegions("lizard_attack"),
                            Animation.PlayMode.NORMAL
                        ))
                    ));
                    break;
                case ENEMY_BAT:
                    enemyAnimations.put(key, Map.ofEntries(
                        Map.entry(EnemyState.IDLE, new Animation<>(
                            1 / 4f,
                            atlas.findRegions("bat_idle"),
                            Animation.PlayMode.LOOP
                        )), Map.entry(EnemyState.ATTACKING, new Animation<>(
                            1 / 6f,
                            atlas.findRegions("bat_idle"),
                            Animation.PlayMode.LOOP
                        ))
                    ));
                    break;
                case ENEMY_SPIKYLIZARD:
                    enemyAnimations.put(key, Map.ofEntries(
                        Map.entry(EnemyState.IDLE, new Animation<>(
                            1 / 2f,
                            atlas.findRegions("spikylizard_idle"),
                            Animation.PlayMode.LOOP
                        )),
                        Map.entry(EnemyState.ATTACKING, new Animation<>(
                            0.7f,
                            atlas.findRegions("spikylizard_attack"),
                            Animation.PlayMode.NORMAL
                        ))
                    ));
                    break;
                case ENEMY_MANTICORE:
                    enemyAnimations.put(key, Map.ofEntries(
                        Map.entry(EnemyState.IDLE, new Animation<>(
                            1/3f,
                            atlas.findRegions("manticore_idle"),
                            Animation.PlayMode.LOOP
                        )),
                        Map.entry(EnemyState.FLYIDLE, new Animation<>(
                            1/3f,
                            atlas.findRegions("manticore_fly"),
                            Animation.PlayMode.LOOP
                        )),
                        Map.entry(EnemyState.CHARGELUNGE, new Animation<>(
                            1/2f,
                            atlas.findRegions("manticore_chargepounce"),
                            Animation.PlayMode.NORMAL
                        )),
                        Map.entry(EnemyState.FLYCHARGELUNGE, new Animation<>(
                            1/2f,
                            atlas.findRegions("manticore_flychargepounce"),
                            Animation.PlayMode.NORMAL
                        )),
                        Map.entry(EnemyState.LUNGE, new Animation<>(
                            1f,
                            atlas.findRegions("manticore_pounce"),
                            Animation.PlayMode.NORMAL
                        )),
                        Map.entry(EnemyState.CHARGESHOOTPROJECTILE, new Animation<>(
                            2f,
                            atlas.findRegions("manticore_tailswipe"),
                            Animation.PlayMode.NORMAL
                        )),
                        Map.entry(EnemyState.SHOOTPROJECTILE, new Animation<>(
                            0.5f,
                            atlas.findRegions("manticore_tailswipe"),
                            Animation.PlayMode.NORMAL
                        )),
                        Map.entry(EnemyState.FLYCHARGESHOOTPROJECTILE, new Animation<>(
                            2f,
                            atlas.findRegions("manticore_flytailswipe"),
                            Animation.PlayMode.NORMAL
                        )),
                        Map.entry(EnemyState.FLYSHOOTPROJECTILE, new Animation<>(
                            0.5f,
                            atlas.findRegions("manticore_flytailswipe"),
                            Animation.PlayMode.NORMAL
                        ))
                    ));
                    break;
            }
        }
        return enemyAnimations.get(key);
    }

    public Map<EnemyState, List<AnimationEvent>> getEnemyAnimEvents(AnimationKey key) {
        if (!enemyAnimationEvents.containsKey(key)) {
            switch (key) {
                default:
                    enemyAnimationEvents.put(key, Map.ofEntries(
                        Map.entry(EnemyState.IDLE, Arrays.asList(
                            new AnimationEvent(0 / 60f, "test")
                        ))
                    ));
                        break;
            }
        }
        return enemyAnimationEvents.get(key);
    }

    public Map<EffectState, Animation<TextureRegion>> getEffectAnims(AnimationKey key) {
        if (!effectAnimations.containsKey(key)) {
            switch (key) {
                case EFFECT_FIREBALL:
                    effectAnimations.put(key, Map.ofEntries(
                        Map.entry(EffectState.IDLE, new Animation<>(
                        1/12f,
                        atlas.findRegions("fireball"),
                        Animation.PlayMode.LOOP)),
                        Map.entry(EffectState.DESTROYED, new Animation<>(
                            1/12f,
                            atlas.findRegions("fireball_destroy"),
                            Animation.PlayMode.NORMAL))
                    ));
                    break;
                case EFFECT_EXPLOSION:
                    effectAnimations.put(key, Map.ofEntries(
                        Map.entry(EffectState.IDLE, new Animation<>(
                            1/12f,
                            atlas.findRegions("fireball_destroy"),
                            Animation.PlayMode.NORMAL)),
                        Map.entry(EffectState.DESTROYED, new Animation<>(
                            1/12f,
                            atlas.findRegions("fireball_destroy"),
                            Animation.PlayMode.NORMAL))
                    ));
                    break;
                case EFFECT_PROJECTILESHOOT:
                    effectAnimations.put(key, Map.ofEntries(
                        Map.entry(EffectState.IDLE, new Animation<>(
                            1/12f,
                            atlas.findRegions("shooteffect"),
                            Animation.PlayMode.NORMAL
                        ))
                    ));
                    break;
                case EFFECT_CLAWSWIPE:
                    effectAnimations.put(key, Map.ofEntries(
                        Map.entry(EffectState.IDLE, new Animation<>(
                        1/12f,
                        atlas.findRegions("clawswipe"),
                        Animation.PlayMode.NORMAL))
                    ));
                    break;
                case EFFECT_SLASH:
                    effectAnimations.put(key, Map.ofEntries(
                        Map.entry(EffectState.IDLE, new Animation<>(
                            1/12f,
                            atlas.findRegions("sweep"),
                            Animation.PlayMode.NORMAL))
                    ));
                    break;
                case EFFECT_ENEMYDEATH:
                    effectAnimations.put(key, Map.ofEntries(
                        Map.entry(EffectState.IDLE, new Animation<>(
                            1/12f,
                            atlas.findRegions("enemydeath"),
                            Animation.PlayMode.NORMAL))
                    ));
                    break;
                case EFFECT_LOOT_CRYSTAL:
                    effectAnimations.put(key, Map.ofEntries(
                        Map.entry(EffectState.IDLE, new Animation<>(
                        1/6f,
                        atlas.findRegions("crystalshard"),
                        Animation.PlayMode.LOOP))
                    ));
                    break;
            }
        }
        return effectAnimations.get(key);
    }

    public Map<EffectState, List<AnimationEvent>> getEffectAnimEvents(AnimationKey key) {
        if (!effectAnimationEvents.containsKey(key)) {
            switch (key) {
                default:
                    effectAnimationEvents.put(key, Map.ofEntries(
                        Map.entry(EffectState.IDLE, Arrays.asList(
                            new AnimationEvent(0 / 60f, "test")
                        ))
                    ));
                    break;
            }
        }
        return effectAnimationEvents.get(key);
    }
}
