package io.github.dragonplatformer.Entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import io.github.dragonplatformer.Entity.Actor.Enemy.EnemyState;
import io.github.dragonplatformer.Entity.Actor.Player.PlayerState;
import io.github.dragonplatformer.Entity.Effect.EffectState;

import java.util.*;

public class AnimationManager {
    private final TextureAtlas atlas;
    private final Map<AnimationKey, Map<PlayerState, AnimationWrapper>> playerAnimations;
    private final Map<PlayerState, List<AnimationEvent>> playerAnimationEvents;
    private final Map<AnimationKey, Map<EnemyState, AnimationWrapper>> enemyAnimations;
    private final Map<AnimationKey, Map<EnemyState, List<AnimationEvent>>> enemyAnimationEvents;
    private final Map<AnimationKey, Map<EffectState, AnimationWrapper>> effectAnimations;
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

    public Map<PlayerState, AnimationWrapper> getPlayerAnimations() {
        AnimationKey key = AnimationKey.PLAYER_FIREDRAGON;
        if (!playerAnimations.containsKey(key)) {
            String skinName = "firedragon";
            skinName += "_";
            playerAnimations.put(key, Map.ofEntries(
                Map.entry(
                    PlayerState.IDLE, new AnimationWrapper(
                        1/20f,
                        atlas.findRegions(skinName + "idle"),
                        Animation.PlayMode.LOOP
                    )
                ), Map.entry(
                    PlayerState.RUN, new AnimationWrapper(
                        1/20f,
                        atlas.findRegions(skinName + "run"),
                        Animation.PlayMode.LOOP
                    )
                ), Map.entry(
                    PlayerState.JUMP, new AnimationWrapper(
                        1/20f,
                        atlas.findRegions(skinName + "flap"),
                        Animation.PlayMode.NORMAL
                    )
                ), Map.entry(
                    PlayerState.FLY, new AnimationWrapper(
                        1/20f,
                        atlas.findRegions(skinName + "fly"),
                        Animation.PlayMode.LOOP
                    )
                ), Map.entry(
                    PlayerState.GLIDE, new AnimationWrapper(
                        1/20f,
                        atlas.findRegions(skinName + "glide"),
                        Animation.PlayMode.LOOP
                    )
                ), Map.entry(
                    PlayerState.DIVE, new AnimationWrapper(
                        1/20f,
                        atlas.findRegions(skinName + "dive"),
                        Animation.PlayMode.LOOP
                    )
                ), Map.entry(
                    PlayerState.SOAR, new AnimationWrapper(
                        1/20f,
                        atlas.findRegions(skinName + "soar"),
                        Animation.PlayMode.LOOP
                    )
                ), Map.entry(
                    PlayerState.EVADE, new AnimationWrapper(
                        0.75f / 15,
                        atlas.findRegions(skinName + "evade"),
                        Animation.PlayMode.NORMAL
                    )
                ), Map.entry(
                    PlayerState.EVADE_HORIZONTAL, new AnimationWrapper(
                        0.5f / 10,
                        atlas.findRegions(skinName + "evadeforward"),
                        Animation.PlayMode.NORMAL
                    )
                ), Map.entry(
                    PlayerState.EVADE_UP, new AnimationWrapper(
                        1/20f,
                        atlas.findRegions(skinName + "evadeup"),
                        Animation.PlayMode.NORMAL
                    )
                ), Map.entry(
                    PlayerState.EVADE_DOWN, new AnimationWrapper(
                        1/20f,
                        atlas.findRegions(skinName + "evadedown"),
                        Animation.PlayMode.NORMAL
                    )
                ), Map.entry(
                    PlayerState.ATTACK_FORWARD1, new AnimationWrapper(
                        1/20f,
                        atlas.findRegions(skinName + "attackforward"),
                        Animation.PlayMode.NORMAL,
                        new Vector2(-32, 0)
                    )
                ), Map.entry(
                    PlayerState.ATTACK_FORWARD2, new AnimationWrapper(
                        1/20f,
                        atlas.findRegions(skinName + "attackforward2"),
                        Animation.PlayMode.NORMAL,
                        new Vector2(-32, 0)
                    )
                ), Map.entry(
                    PlayerState.ATTACK_UP, new AnimationWrapper(
                        1/20f,
                        atlas.findRegions(skinName + "attackup"),
                        Animation.PlayMode.NORMAL,
                        new Vector2(0, -32)
                    )
                ), Map.entry(
                    PlayerState.ATTACK_DOWN, new AnimationWrapper(
                        1/20f,
                        atlas.findRegions(skinName + "attackdown"),
                        Animation.PlayMode.NORMAL,
                        new Vector2(0, 32)
                    )
                ), Map.entry(
                    PlayerState.ATTACK_GLIDE, new AnimationWrapper(
                        0.75f / 14,
                        atlas.findRegions(skinName + "glideattack"),
                        Animation.PlayMode.NORMAL
                    )
                ), Map.entry(
                    PlayerState.ATTACK_SOAR, new AnimationWrapper(
                        0.5f / 14,
                        atlas.findRegions(skinName + "glideattack"),
                        Animation.PlayMode.NORMAL
                    )
                ), Map.entry(
                    PlayerState.ATTACK_DIVE, new AnimationWrapper(
                        0.5f / 14,
                        atlas.findRegions(skinName + "glideattack"),
                        Animation.PlayMode.NORMAL
                    )
                ), Map.entry(
                    PlayerState.ATTACK_DIVE_LAND, new AnimationWrapper(
                        0.33f / 8,
                        atlas.findRegions(skinName + "attackforward"),
                        Animation.PlayMode.NORMAL
                    )
                ), Map.entry(
                    PlayerState.ATTACK_GROUND1, new AnimationWrapper(
                        0.05f,
                        atlas.findRegions(skinName + "attackforward"),
                        Animation.PlayMode.NORMAL,
                        new Vector2(-32, 0)
                    )
                ), Map.entry(
                    PlayerState.ATTACK_GROUND2, new AnimationWrapper(
                        0.05f,
                        atlas.findRegions(skinName + "attackforward2"),
                        Animation.PlayMode.NORMAL,
                        new Vector2(-32, 0)
                    )
                ), Map.entry(
                    PlayerState.DEATH, new AnimationWrapper(
                        1/2f,
                        atlas.findRegions(skinName + "flap"),
                        Animation.PlayMode.NORMAL
                    )
                )
            ));
        }
        return playerAnimations.get(key);
    }

    public Map<PlayerState, List<AnimationEvent>> getPlayerAnimEvents() {
        if (playerAnimationEvents.isEmpty()) {
            playerAnimationEvents.putAll(Map.ofEntries(
                Map.entry(PlayerState.EVADE, Arrays.asList(
                    new AnimationEvent(0f, AnimationEventKey.evadestart),
                    new AnimationEvent(0.4f, AnimationEventKey.evadeend)
                )), Map.entry(PlayerState.EVADE_HORIZONTAL, Arrays.asList(
                    new AnimationEvent(0f, AnimationEventKey.iframestart),
                    new AnimationEvent(0.2f, AnimationEventKey.iframeend)
                )), Map.entry(PlayerState.EVADE_UP, Arrays.asList(
                    new AnimationEvent(0f, AnimationEventKey.iframestart),
                    new AnimationEvent(0f, AnimationEventKey.iframestart),
                    new AnimationEvent(0.2f, AnimationEventKey.iframeend)
                )), Map.entry(PlayerState.EVADE_DOWN, Arrays.asList(
                    new AnimationEvent(0f, AnimationEventKey.iframestart),
                    new AnimationEvent(0.2f, AnimationEventKey.iframeend)
                )), Map.entry(PlayerState.ATTACK_FORWARD1, List.of(
                    new AnimationEvent(0.2f, AnimationEventKey.hitframe)
                )), Map.entry(PlayerState.ATTACK_FORWARD2, List.of(
                    new AnimationEvent(0.2f, AnimationEventKey.hitframe)
                )), Map.entry(PlayerState.ATTACK_GROUND1, List.of(
                    new AnimationEvent(0.2f, AnimationEventKey.hitframe)
                )), Map.entry(PlayerState.ATTACK_GROUND2, List.of(
                    new AnimationEvent(0.2f, AnimationEventKey.hitframe)
                ))
            ));
        }
        return playerAnimationEvents;
    }

    public Map<EnemyState, AnimationWrapper> getEnemyAnims(AnimationKey key) {
        if (!enemyAnimations.containsKey(key)) {
            switch (key) {
                case ENEMY_LIZARD:
                    enemyAnimations.put(key, Map.ofEntries(
                        Map.entry(EnemyState.IDLE, new AnimationWrapper(
                            1 / 3f,
                            atlas.findRegions("lizard_idle"),
                            Animation.PlayMode.LOOP
                        )),
                        Map.entry(EnemyState.ATTACK, new AnimationWrapper(
                            1 / 6f,
                            atlas.findRegions("lizard_attack"),
                            Animation.PlayMode.NORMAL
                        ))
                    ));
                    break;
                case ENEMY_BAT:
                    enemyAnimations.put(key, Map.ofEntries(
                        Map.entry(EnemyState.IDLE, new AnimationWrapper(
                            1 / 4f,
                            atlas.findRegions("bat_idle"),
                            Animation.PlayMode.LOOP
                        )), Map.entry(EnemyState.ATTACK, new AnimationWrapper(
                            1 / 6f,
                            atlas.findRegions("bat_idle"),
                            Animation.PlayMode.LOOP
                        ))
                    ));
                    break;
                case ENEMY_SPIKYLIZARD:
                    enemyAnimations.put(key, Map.ofEntries(
                        Map.entry(EnemyState.IDLE, new AnimationWrapper(
                            1 / 2f,
                            atlas.findRegions("spikylizard_idle"),
                            Animation.PlayMode.LOOP
                        )),
                        Map.entry(EnemyState.ATTACK, new AnimationWrapper(
                            0.7f,
                            atlas.findRegions("spikylizard_attack"),
                            Animation.PlayMode.NORMAL
                        ))
                    ));
                    break;
                case ENEMY_MANTICORE:
                    enemyAnimations.put(key, Map.ofEntries(
                        Map.entry(EnemyState.IDLE, new AnimationWrapper(
                            1/3f,
                            atlas.findRegions("manticore_fly"),
                            Animation.PlayMode.LOOP
                        )),
                        Map.entry(EnemyState.FLYIDLE, new AnimationWrapper(
                            1/3f,
                            atlas.findRegions("manticore_fly"),
                            Animation.PlayMode.LOOP
                        )),
                        Map.entry(EnemyState.CHARGELUNGE, new AnimationWrapper(
                            1/2f,
                            atlas.findRegions("manticore_chargepounce"),
                            Animation.PlayMode.NORMAL
                        )),
                        Map.entry(EnemyState.FLYCHARGELUNGE, new AnimationWrapper(
                            1/2f,
                            atlas.findRegions("manticore_flychargepounce"),
                            Animation.PlayMode.NORMAL
                        )),
                        Map.entry(EnemyState.LUNGE, new AnimationWrapper(
                            1f,
                            atlas.findRegions("manticore_pounce"),
                            Animation.PlayMode.NORMAL
                        )),
                        Map.entry(EnemyState.CHARGESHOOTPROJECTILE, new AnimationWrapper(
                            2f,
                            atlas.findRegions("manticore_tailswipe"),
                            Animation.PlayMode.NORMAL
                        )),
                        Map.entry(EnemyState.SHOOTPROJECTILE, new AnimationWrapper(
                            0.5f,
                            atlas.findRegions("manticore_tailswipe"),
                            Animation.PlayMode.NORMAL
                        )),
                        Map.entry(EnemyState.FLYCHARGESHOOTPROJECTILE, new AnimationWrapper(
                            2f,
                            atlas.findRegions("manticore_flytailswipe"),
                            Animation.PlayMode.NORMAL
                        )),
                        Map.entry(EnemyState.FLYSHOOTPROJECTILE, new AnimationWrapper(
                            0.5f,
                            atlas.findRegions("manticore_flytailswipe"),
                            Animation.PlayMode.NORMAL
                        ))
                    ));
                    break;
                case ENEMY_GARGOYLE:
                    enemyAnimations.put(key, Map.ofEntries(
                        Map.entry(EnemyState.IDLE, new AnimationWrapper(
                            0.8f / 2,
                            atlas.findRegions("gargoyle-idle"),
                            Animation.PlayMode.LOOP
                        )),
                        Map.entry(EnemyState.ATTACK, new AnimationWrapper(
                            1.0f / 4,
                            atlas.findRegions("gargoyle-melee"),
                            Animation.PlayMode.NORMAL
                        ))
                    ));
                    break;
                case ENEMY_WYVERN:
                    enemyAnimations.put(key, Map.ofEntries(
                        Map.entry(EnemyState.IDLE, new AnimationWrapper(
                            0.6f / 2,
                            atlas.findRegions("wyvern-idle"),
                            Animation.PlayMode.LOOP
                        )),
                        Map.entry(EnemyState.ATTACK, new AnimationWrapper(
                            1.0f / 4,
                            atlas.findRegions("wyvern-projectile"),
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
                case ENEMY_GARGOYLE:
                case ENEMY_WYVERN:
                    enemyAnimationEvents.put(key, Map.ofEntries(
                        Map.entry(EnemyState.ATTACK, List.of(
                            new AnimationEvent(0.5f, AnimationEventKey.hitframe)
                        ))
                    ));
                    break;
                default:
                    enemyAnimationEvents.put(key, Map.ofEntries());
                        break;
            }
        }
        return enemyAnimationEvents.get(key);
    }

    public Map<EffectState, AnimationWrapper> getEffectAnims(AnimationKey key) {
        if (!effectAnimations.containsKey(key)) {
            switch (key) {
                case EFFECT_FIREBALL:
                    effectAnimations.put(key, Map.ofEntries(
                        Map.entry(EffectState.IDLE, new AnimationWrapper(
                        1/12f,
                        atlas.findRegions("fireball"),
                        Animation.PlayMode.LOOP))
                    ));
                    break;
                case EFFECT_EXPLOSION:
                    effectAnimations.put(key, Map.ofEntries(
                        Map.entry(EffectState.IDLE, new AnimationWrapper(
                            1/12f,
                            atlas.findRegions("fireball_destroy"),
                            Animation.PlayMode.NORMAL))
                    ));
                    break;
                case EFFECT_PROJECTILESHOOT:
                    effectAnimations.put(key, Map.ofEntries(
                        Map.entry(EffectState.IDLE, new AnimationWrapper(
                            1/12f,
                            atlas.findRegions("shooteffect"),
                            Animation.PlayMode.NORMAL
                        ))
                    ));
                    break;
                case EFFECT_CLAWSWIPE:
                    effectAnimations.put(key, Map.ofEntries(
                        Map.entry(EffectState.IDLE, new AnimationWrapper(
                        1/12f,
                        atlas.findRegions("clawswipe"),
                        Animation.PlayMode.NORMAL))
                    ));
                    break;
                case EFFECT_SLASH:
                    effectAnimations.put(key, Map.ofEntries(
                        Map.entry(EffectState.IDLE, new AnimationWrapper(
                            1/12f,
                            atlas.findRegions("sweep"),
                            Animation.PlayMode.NORMAL))
                    ));
                    break;
                case EFFECT_ENEMYDEATH:
                    effectAnimations.put(key, Map.ofEntries(
                        Map.entry(EffectState.IDLE, new AnimationWrapper(
                            1/12f,
                            atlas.findRegions("enemydeath"),
                            Animation.PlayMode.NORMAL))
                    ));
                    break;
                case EFFECT_LOOT_CRYSTAL:
                    effectAnimations.put(key, Map.ofEntries(
                        Map.entry(EffectState.IDLE, new AnimationWrapper(
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
                    effectAnimationEvents.put(key, Map.ofEntries());
                    break;
            }
        }
        return effectAnimationEvents.get(key);
    }
}
