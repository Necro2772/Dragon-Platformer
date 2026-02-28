package io.github.dragonplatformer.Entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.dragonplatformer.Entity.AttackEffect.AttackEffect;
import io.github.dragonplatformer.Entity.Creature.Enemy;
import io.github.dragonplatformer.Entity.Creature.Player;

import java.util.HashMap;
import java.util.Map;

public class AnimationManager {
    private final TextureAtlas atlas;
    private final Map<AnimationKeys, Map<Player.PlayerState, Animation<TextureRegion>>> playerAnimations;
    private final Map<AnimationKeys, Map<Enemy.EnemyState, Animation<TextureRegion>>> enemyAnimations;
    private final Map<AnimationKeys, Map<AttackEffect.AttackState, Animation<TextureRegion>>> effectAnimations;
    private final Map<AnimationKeys, Animation<TextureRegion>> lootAnimations;

    public AnimationManager(TextureAtlas atlas) {
        this.atlas = atlas;
        playerAnimations = new HashMap<>();
        enemyAnimations = new HashMap<>();
        effectAnimations = new HashMap<>();
        lootAnimations = new HashMap<>();
    }

    public Map<Player.PlayerState, Animation<TextureRegion>> getPlayerAnimations() {
        AnimationKeys key = AnimationKeys.PLAYER;
        if (!playerAnimations.containsKey(key)) {
            playerAnimations.put(key, Map.ofEntries(
                Map.entry(Player.PlayerState.IDLE, new Animation<>(
                    1/3f,
                    atlas.findRegions("dragon_idle"),
                    Animation.PlayMode.LOOP
                )), Map.entry(Player.PlayerState.RUNNING, new Animation<>(
                    1/3f,
                    atlas.findRegions("dragon_run"),
                    Animation.PlayMode.LOOP_PINGPONG
                )), Map.entry(Player.PlayerState.JUMPING, new Animation<>(
                    1/9f,
                    atlas.findRegions("dragon_flap"),
                    Animation.PlayMode.LOOP
                )), Map.entry(Player.PlayerState.FLYING, new Animation<>(
                    1/6f,
                    atlas.findRegions("dragon_fly"),
                    Animation.PlayMode.LOOP
                )), Map.entry(Player.PlayerState.GLIDING, new Animation<>(
                    1/3f,
                    atlas.findRegions("dragon_glide"),
                    Animation.PlayMode.LOOP
                )), Map.entry(Player.PlayerState.DIVING, new Animation<>(
                    1/3f,
                    atlas.findRegions("dragon_dive"),
                    Animation.PlayMode.LOOP
                )), Map.entry(Player.PlayerState.DIVESOAR, new Animation<>(
                    1/3f,
                    atlas.findRegions("dragon_divesoar"),
                    Animation.PlayMode.LOOP
                )), Map.entry(Player.PlayerState.DASH, new Animation<>(
                    1/6f,
                    atlas.findRegions("dragon_dash"),
                    Animation.PlayMode.LOOP
                )), Map.entry(Player.PlayerState.DASHDIVE, new Animation<>(
                    1/4f,
                    atlas.findRegions("dragon_dive"),
                    Animation.PlayMode.LOOP
                )), Map.entry(Player.PlayerState.ATTACKFORWARD, new Animation<>(
                    1/4f,
                    atlas.findRegions("dragon_attackforward"),
                    Animation.PlayMode.NORMAL
                )), Map.entry(Player.PlayerState.ATTACKFORWARD2, new Animation<>(
                    1/4f,
                    atlas.findRegions("dragon_attackforward2"),
                    Animation.PlayMode.NORMAL
                )), Map.entry(Player.PlayerState.ATTACKFORWARD3, new Animation<>(
                    1/3f,
                    atlas.findRegions("dragon_attackforward3"),
                    Animation.PlayMode.NORMAL
                )), Map.entry(Player.PlayerState.ATTACKDIVE, new Animation<>(
                    1/3f,
                    atlas.findRegions("dragon_attackforward3"),
                    Animation.PlayMode.NORMAL
                )), Map.entry(Player.PlayerState.ATTACKDASH, new Animation<>(
                    1/3f,
                    atlas.findRegions("dragon_attackforward3"),
                    Animation.PlayMode.NORMAL
                )), Map.entry(Player.PlayerState.ATTACKGLIDE, new Animation<>(
                    1/3f,
                    atlas.findRegions("dragon_attackforward3"),
                    Animation.PlayMode.NORMAL
                )), Map.entry(Player.PlayerState.ATTACKJUMP, new Animation<>(
                    1/3f,
                    atlas.findRegions("dragon_attackforward3"),
                    Animation.PlayMode.NORMAL
                )), Map.entry(Player.PlayerState.ATTACKDOWN, new Animation<>(
                    1/3f,
                    atlas.findRegions("dragon_attackdown"),
                    Animation.PlayMode.NORMAL
                )), Map.entry(Player.PlayerState.ATTACKUP, new Animation<>(
                    1/3f,
                    atlas.findRegions("dragon_attackup"),
                    Animation.PlayMode.NORMAL
                )), Map.entry(Player.PlayerState.DEATH, new Animation<>(
                    1/2f,
                    atlas.findRegions("dragon_jump"),
                    Animation.PlayMode.NORMAL
                ))
            ));
        }
        return playerAnimations.get(key);
    }

    public Map<Enemy.EnemyState, Animation<TextureRegion>> getEnemyAnims(AnimationKeys key) {
        if (!enemyAnimations.containsKey(key)) {
            switch (key) {
                case ENEMY_LIZARD:
                    enemyAnimations.put(key, Map.ofEntries(
                        Map.entry(Enemy.EnemyState.IDLE, new Animation<>(
                            1 / 3f,
                            atlas.findRegions("lizard_idle"),
                            Animation.PlayMode.LOOP
                        )),
                        Map.entry(Enemy.EnemyState.ATTACKING, new Animation<>(
                            1 / 6f,
                            atlas.findRegions("lizard_attack"),
                            Animation.PlayMode.NORMAL
                        ))
                    ));
                    break;
                case ENEMY_BAT:
                    enemyAnimations.put(key, Map.ofEntries(
                        Map.entry(Enemy.EnemyState.IDLE, new Animation<>(
                            1 / 4f,
                            atlas.findRegions("bat_idle"),
                            Animation.PlayMode.LOOP
                        )), Map.entry(Enemy.EnemyState.ATTACKING, new Animation<>(
                            1 / 6f,
                            atlas.findRegions("bat_idle"),
                            Animation.PlayMode.LOOP
                        ))
                    ));
                    break;
                case ENEMY_SPIKYLIZARD:
                    enemyAnimations.put(key, Map.ofEntries(
                        Map.entry(Enemy.EnemyState.IDLE, new Animation<>(
                            1 / 2f,
                            atlas.findRegions("spikylizard_idle"),
                            Animation.PlayMode.LOOP
                        )),
                        Map.entry(Enemy.EnemyState.ATTACKING, new Animation<>(
                            0.7f,
                            atlas.findRegions("spikylizard_attack"),
                            Animation.PlayMode.NORMAL
                        ))
                    ));
                    break;
                case ENEMY_MANTICORE:
                    enemyAnimations.put(key, Map.ofEntries(
                        Map.entry(Enemy.EnemyState.IDLE, new Animation<>(
                            1/3f,
                            atlas.findRegions("manticore_idle"),
                            Animation.PlayMode.LOOP
                        )),
                        Map.entry(Enemy.EnemyState.FLYIDLE, new Animation<>(
                            1/3f,
                            atlas.findRegions("manticore_fly"),
                            Animation.PlayMode.LOOP
                        )),
                        Map.entry(Enemy.EnemyState.CHARGELUNGE, new Animation<>(
                            1/2f,
                            atlas.findRegions("manticore_chargepounce"),
                            Animation.PlayMode.NORMAL
                        )),
                        Map.entry(Enemy.EnemyState.FLYCHARGELUNGE, new Animation<>(
                            1/2f,
                            atlas.findRegions("manticore_flychargepounce"),
                            Animation.PlayMode.NORMAL
                        )),
                        Map.entry(Enemy.EnemyState.LUNGE, new Animation<>(
                            1f,
                            atlas.findRegions("manticore_pounce"),
                            Animation.PlayMode.NORMAL
                        )),
                        Map.entry(Enemy.EnemyState.CHARGESHOOTPROJECTILE, new Animation<>(
                            2f,
                            atlas.findRegions("manticore_tailswipe"),
                            Animation.PlayMode.NORMAL
                        )),
                        Map.entry(Enemy.EnemyState.SHOOTPROJECTILE, new Animation<>(
                            0.5f,
                            atlas.findRegions("manticore_tailswipe"),
                            Animation.PlayMode.NORMAL
                        )),
                        Map.entry(Enemy.EnemyState.FLYCHARGESHOOTPROJECTILE, new Animation<>(
                            2f,
                            atlas.findRegions("manticore_flytailswipe"),
                            Animation.PlayMode.NORMAL
                        )),
                        Map.entry(Enemy.EnemyState.FLYSHOOTPROJECTILE, new Animation<>(
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

    public Map<AttackEffect.AttackState, Animation<TextureRegion>> getEffectAnims(AnimationKeys key) {
        if (!effectAnimations.containsKey(key)) {
            switch (key) {
                case EFFECT_FIREBALL:
                    effectAnimations.put(key, Map.ofEntries(
                        Map.entry(AttackEffect.AttackState.IDLE, new Animation<>(
                        1/12f,
                        atlas.findRegions("fireball"),
                        Animation.PlayMode.LOOP)),
                        Map.entry(AttackEffect.AttackState.DESTROYED, new Animation<>(
                            1/12f,
                            atlas.findRegions("fireball_destroy"),
                            Animation.PlayMode.NORMAL))
                    ));
                    break;
                case EFFECT_EXPLOSION:
                    effectAnimations.put(key, Map.ofEntries(
                        Map.entry(AttackEffect.AttackState.IDLE, new Animation<>(
                            1/12f,
                            atlas.findRegions("fireball_destroy"),
                            Animation.PlayMode.NORMAL)),
                        Map.entry(AttackEffect.AttackState.DESTROYED, new Animation<>(
                            1/12f,
                            atlas.findRegions("fireball_destroy"),
                            Animation.PlayMode.NORMAL))
                    ));
                    break;
                case EFFECT_PROJECTILESHOOT:
                    effectAnimations.put(key, Map.ofEntries(
                        Map.entry(AttackEffect.AttackState.IDLE, new Animation<>(
                            1/12f,
                            atlas.findRegions("shooteffect"),
                            Animation.PlayMode.NORMAL
                        ))
                    ));
                    break;
                case EFFECT_CLAWSWIPE:
                    effectAnimations.put(key, Map.ofEntries(
                        Map.entry(AttackEffect.AttackState.IDLE, new Animation<>(
                        1/12f,
                        atlas.findRegions("clawswipe"),
                        Animation.PlayMode.NORMAL))
                    ));
                    break;
                case EFFECT_SLASH:
                    effectAnimations.put(key, Map.ofEntries(
                        Map.entry(AttackEffect.AttackState.IDLE, new Animation<>(
                            1/12f,
                            atlas.findRegions("sweep"),
                            Animation.PlayMode.NORMAL))
                    ));
                    break;
                case EFFECT_ENEMYDEATH:
                    effectAnimations.put(key, Map.ofEntries(
                        Map.entry(AttackEffect.AttackState.IDLE, new Animation<>(
                            1/12f,
                            atlas.findRegions("enemydeath"),
                            Animation.PlayMode.NORMAL))
                    ));
                    break;
            }
        }
        return effectAnimations.get(key);
    }

    public Animation<TextureRegion> getLootAnim(AnimationKeys key) {
        if (!lootAnimations.containsKey(key)) {
            lootAnimations.put(key, new Animation<>(
                1/6f,
                atlas.findRegions("crystalshard"),
                Animation.PlayMode.LOOP
            ));
        }
        return lootAnimations.get(key);
    }

    public enum AnimationKeys {
        PLAYER,
        ENEMY_LIZARD,
        ENEMY_BAT,
        ENEMY_SPIKYLIZARD,
        ENEMY_MANTICORE,
        EFFECT_FIREBALL,
        EFFECT_EXPLOSION,
        EFFECT_CLAWSWIPE,
        EFFECT_SLASH,
        EFFECT_PROJECTILESHOOT,
        EFFECT_ENEMYDEATH,
        LOOT_CRYSTAL
    }
}
