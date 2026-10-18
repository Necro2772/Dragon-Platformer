package io.github.dragonplatformer.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;

public class EffectManager {
    private final ParticleEffectPool glideAttackPool;
    private final ParticleEffectPool meleeHitPool;
    private final ParticleEffectPool neutralMeleePool;

    private final ParticleEffectPool chargeFirePool;
    private final ParticleEffectPool chargeFirePlayerPool;
    private final ParticleEffectPool fireballPool;
    private final ParticleEffectPool fireballMediumPool;
    private final ParticleEffectPool fireballHitPool;
    private final ParticleEffectPool explosionPool;
    private final ParticleEffectPool fireMeleePool;

    private final ParticleEffectPool iceSpikePool;
    private final ParticleEffectPool chargeIcePlayerPool;
    private final ParticleEffectPool iceSpikeHitPool;
    private final ParticleEffectPool iceGlidePool;
    private final ParticleEffectPool iceExplosionPool;

    private final ParticleEffectPool earthMeleePool;
    private final ParticleEffectPool earthGlideAttackPool;

    private final ParticleEffectPool windVortexPool;

    private final ParticleEffectPool enemyDeathPool;
    private final ParticleEffectPool glidePool;
    private final ParticleEffectPool flapWindPool;
    private final ParticleEffectPool projectileShootPool;
    private final ParticleEffectPool portalPool;

    private final Array<PooledEffect> effects;

    public EffectManager(TextureAtlas atlas) {
        neutralMeleePool = initPool("meleeneutral.p", atlas, 1, 5);

        chargeFirePool =  initPool("chargefire.p", atlas, 10, 20);
        chargeFirePlayerPool = initPool("chargefireplayer.p", atlas, 1, 2);
        fireballPool = initPool("fireballsmall.p", atlas, 10, 20);
        fireballHitPool = initPool("fireballhit.p", atlas, 10, 20);
        fireballMediumPool = initPool("fireballmedium.p", atlas, 5, 10);
        explosionPool = initPool("explosion.p", atlas, 5, 10);
        fireMeleePool = initPool("firemelee.p", atlas, 1, 5);

        chargeIcePlayerPool = initPool("chargeiceplayer.p", atlas, 1, 3);
        iceSpikePool = initPool("icespike.p", atlas, 5, 10);
        iceSpikeHitPool = initPool("icespikehit.p", atlas, 5, 10);
        iceGlidePool = initPool("iceglideattack.p", atlas, 1, 5);
        iceExplosionPool = initPool("iceexplosion.p", atlas, 5, 10);

        earthMeleePool = initPool("earthmelee.p", atlas, 1, 3);
        earthGlideAttackPool = initPool("earthglideattack.p", atlas, 1, 3);

        windVortexPool = initPool("windvortex.p", atlas, 5, 10);

        glideAttackPool = initPool("glideattack.p", atlas, 1, 3);
        meleeHitPool = initPool("meleehit.p", atlas, 10, 20);
        enemyDeathPool = initPool("enemydeath.p", atlas, 5, 10);
        glidePool = initPool("glidewind.p", atlas, 1, 2);
        flapWindPool = initPool("flapwind.p", atlas, 2, 10);
        projectileShootPool = initPool("projectileshoot.p", atlas, 2, 5);
        portalPool = initPool("portal.p", atlas, 2, 5);
        effects = new Array<>();
    }

    private ParticleEffectPool initPool(String filename, TextureAtlas atlas, int initialCapacity, int maxCapacity) {
        ParticleEffect effect = new ParticleEffect();
        effect.load(Gdx.files.internal("particles/" + filename), atlas);
        effect.setEmittersCleanUpBlendFunction(false);
        return new ParticleEffectPool(effect, initialCapacity, maxCapacity);
    }

    private PooledEffect obtainEffect(ParticleEffectPool pool) {
        PooledEffect effect = pool.obtain();
        effects.add(effect);
        return effect;
    }

    public PooledEffect obtainPortal() {
        return obtainEffect(portalPool);
    }

    public PooledEffect obtainWindVortex() {
        return obtainEffect(windVortexPool);
    }

    public PooledEffect obtainEarthMelee() {
        return obtainEffect(earthMeleePool);
    }

    public PooledEffect obtainEarthGlideAttack() {
        return obtainEffect(earthGlideAttackPool);
    }

    public PooledEffect obtainNeutralMelee() {
        return obtainEffect(neutralMeleePool);
    }

    public PooledEffect obtainFireMelee() {
        return obtainEffect(fireMeleePool);
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

    public PooledEffect obtainChargeIcePlayer() {
        return obtainEffect(chargeIcePlayerPool);
    }

    public PooledEffect obtainIceSpike() {
        return obtainEffect(iceSpikePool);
    }

    public PooledEffect obtainIceSpikeHit() {
        return obtainEffect(iceSpikeHitPool);
    }

    public PooledEffect obtainIceGlide() {
        return obtainEffect(iceGlidePool);
    }

    public PooledEffect obtainIceExplosion() {
        return obtainEffect(iceExplosionPool);
    }

    public PooledEffect obtainGlideAttack() {
        return obtainEffect(glideAttackPool);
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

    public void endAll() {
        for (int i = effects.size; i >= 0; i--) {
            effects.get(i).free();
        }
        effects.clear();
    }

}
