package io.github.dragonplatformer.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class EffectManager {
    private final ParticleEffectPool chargeFirePool;
    private final ParticleEffectPool chargeFirePlayerPool;
    private final ParticleEffectPool fireballPool;
    private final ParticleEffectPool fireballMediumPool;
    private final ParticleEffectPool fireballHitPool;
    private final ParticleEffectPool explosionPool;
    private final ParticleEffectPool meleeHitPool;
    private final ParticleEffectPool enemyDeathPool;
    private final ParticleEffectPool glidePool;
    private final ParticleEffectPool flapWindPool;
    private final ParticleEffectPool projectileShootPool;
    private final Array<PooledEffect> effects;

    public EffectManager(TextureAtlas atlas) {
        ParticleEffect chargeFire = new ParticleEffect();
        ParticleEffect chargeFirePlayer = new ParticleEffect();
        ParticleEffect fireball = new ParticleEffect();
        ParticleEffect fireballHit = new ParticleEffect();
        ParticleEffect fireballMedium = new ParticleEffect();
        ParticleEffect explosion = new ParticleEffect();
        ParticleEffect meleeHit = new ParticleEffect();
        ParticleEffect enemyDeath = new ParticleEffect();
        ParticleEffect glide = new ParticleEffect();
        ParticleEffect flapWind = new ParticleEffect();
        ParticleEffect projectileShoot = new ParticleEffect();

        chargeFire.load(Gdx.files.internal("particles/chargefire.p"), atlas);
        chargeFirePlayer.load(Gdx.files.internal("particles/chargefireplayer.p"), atlas);
        fireball.load(Gdx.files.internal("particles/fireballsmall.p"), atlas);
        fireballHit.load(Gdx.files.internal("particles/fireballhit.p"), atlas);
        fireballMedium.load(Gdx.files.internal("particles/fireballmedium.p"), atlas);
        meleeHit.load(Gdx.files.internal("particles/meleehit.p"), atlas);
        enemyDeath.load(Gdx.files.internal("particles/enemydeath.p"), atlas);
        explosion.load(Gdx.files.internal("particles/explosion.p"), atlas);
        glide.load(Gdx.files.internal("particles/glidewind.p"), atlas);
        flapWind.load(Gdx.files.internal("particles/flapwind.p"), atlas);
        projectileShoot.load(Gdx.files.internal("particles/projectileshoot.p"), atlas);

        chargeFire.setEmittersCleanUpBlendFunction(false);
        chargeFirePlayer.setEmittersCleanUpBlendFunction(false);
        fireball.setEmittersCleanUpBlendFunction(false);
        fireballHit.setEmittersCleanUpBlendFunction(false);
        fireballMedium.setEmittersCleanUpBlendFunction(false);
        explosion.setEmittersCleanUpBlendFunction(false);
        meleeHit.setEmittersCleanUpBlendFunction(false);
        enemyDeath.setEmittersCleanUpBlendFunction(false);
        glide.setEmittersCleanUpBlendFunction(false);
        flapWind.setEmittersCleanUpBlendFunction(false);
        projectileShoot.setEmittersCleanUpBlendFunction(false);

        chargeFirePool =  new ParticleEffectPool(chargeFire, 10, 20);
        chargeFirePlayerPool = new ParticleEffectPool(chargeFirePlayer, 1, 2);
        fireballPool = new ParticleEffectPool(fireball, 10, 20);
        fireballHitPool = new ParticleEffectPool(fireballHit, 10, 20);
        fireballMediumPool = new ParticleEffectPool(fireballMedium, 5, 10);
        explosionPool = new ParticleEffectPool(explosion, 5, 10);
        meleeHitPool = new ParticleEffectPool(meleeHit, 10, 20);
        enemyDeathPool = new ParticleEffectPool(enemyDeath, 5, 10);
        glidePool = new ParticleEffectPool(glide, 1, 2);
        flapWindPool = new ParticleEffectPool(flapWind, 2, 10);
        projectileShootPool = new ParticleEffectPool(projectileShoot, 2, 5);
        effects = new Array<>();
    }

    private PooledEffect obtainEffect(ParticleEffectPool pool) {
        PooledEffect effect = pool.obtain();
        effects.add(effect);
        return effect;
    }

    public PooledEffect obtainChargeFire() {
        return obtainEffect(chargeFirePool);
    }

    public PooledEffect obtainChargeFirePlayer() {
        return obtainEffect(chargeFirePlayerPool);
    }

    public PooledEffect obtainFireball() {
        return obtainEffect(fireballPool);
    }

    public PooledEffect obtainFireballHit() {
        return obtainEffect(fireballHitPool);
    }

    public PooledEffect obtainFireballMedium() {
        return obtainEffect(fireballMediumPool);
    }

    public PooledEffect obtainExplosion() {
        return obtainEffect(explosionPool);
    }

    public PooledEffect obtainMeleeHit() {
        return obtainEffect(meleeHitPool);
    }

    public PooledEffect obtainEnemyDeath() {
        return obtainEffect(enemyDeathPool);
    }

    public PooledEffect obtainGlide() {
        return obtainEffect(glidePool);
    }

    public PooledEffect obtainFlapWind() {
        return obtainEffect(flapWindPool);
    }

    public PooledEffect obtainProjectileShoot() {
        return obtainEffect(projectileShootPool);
    }

    public void endEffect(ParticleEffect effect) {
        effects.removeValue((PooledEffect) effect, true);
        ((PooledEffect) effect).free();
    }

    public void draw(SpriteBatch batch, float delta) {
        for (int i = effects.size - 1; i >= 0; i--) {
            PooledEffect effect = effects.get(i);
            effect.draw(batch, delta);
            if (effect.isComplete()) {
                effect.free();
                effects.removeIndex(i);
            }
        }
    }

    public void resetAll() {
        for (int i = effects.size; i >= 0; i--) {
            effects.get(i).free();
        }
        effects.clear();
    }
}
