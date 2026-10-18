package io.github.dragonplatformer.Entity.Actor;

import java.util.HashMap;
import java.util.Map;

public class ActorStats {
    public float maxHealth;
    public float health;
    public float invulnerability;
    public final Map<Integer, Float> hitMap;
    public boolean intangible;
    private float slowed = 0;
    private float slowEffect = 0.8f;

    public ActorStats() {
        setMaxHealth(1);
        setInvulnerable(0);
        hitMap = new HashMap<>();
        setIntangible(false);
    }

    public boolean getHitGroupInvul(int hitGroup) {
        return hitMap.containsKey(hitGroup);
    }

    public void addHitGroupInvul(int hitGroup, float cooldown) {
        if (hitGroup != -1 && cooldown > 0) hitMap.put(hitGroup, cooldown);
    }

    public void update(float delta) {
        if (isInvulnerable()) setInvulnerable(invulnerability - delta);
        if (isSlowed()) slowed -= delta;
        for (int group : hitMap.keySet()) {
            if (hitMap.get(group) - delta < 0) hitMap.remove(group);
            else hitMap.replace(group, hitMap.get(group) - delta);
        }
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = Math.min(health, getMaxHealth());
    }

    public void setInvulnerable(float time) {
        this.invulnerability = time;
    }

    public boolean isInvulnerable() {
        return this.invulnerability > 0 || isIntangible();
    }

    public void setIntangible(boolean intangible) {
        this.intangible = intangible;
    }

    public boolean isIntangible() {
        return intangible;
    }

    public void slow(float duration) {
        this.slowed = Math.max(this.slowed, duration);
    }

    public boolean isSlowed() {
        return slowed > 0;
    }

    public float getSlowEffect() {
        return slowEffect;
    }

    public void setSlowEffect(float slowEffect) {
        this.slowEffect = slowEffect;
    }
}
