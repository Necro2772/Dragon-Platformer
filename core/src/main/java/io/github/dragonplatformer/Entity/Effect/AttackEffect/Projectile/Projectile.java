package io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Actor.Actor;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.AttackEffect;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack.MeleeAttack;
import io.github.dragonplatformer.Entity.Effect.EffectState;
import io.github.dragonplatformer.GameContactListener;

public abstract class Projectile extends AttackEffect {
    private float health;
    private float lifetime;
    private final FixtureDef bodyFixtureDef;
    private final Filter bodyFilter;
    private final PolygonShape bodyFixtureShape;
    private boolean destroyOnStatic;
    private boolean destroyOnEnemy;
    private final boolean isPlayer;

    public Projectile(float damage, float knockback, float health, float lifetime,
                      float x, float y, float width, float height, int direction,
                      AnimationKey animKey, AnimationManager animManager,
                      boolean isPlayer, World world) {
        super(damage, knockback, x, y, width, height, direction, animKey, animManager, world);
        this.health = health;
        this.lifetime = lifetime;
        this.destroyOnStatic = true;
        this.destroyOnEnemy = true;
        this.isPlayer = isPlayer;

        bodyFixtureShape = new PolygonShape();
        bodyFixtureShape.setAsBox(width / 2f, height / 2f, new Vector2(), 0);
        bodyFixtureDef = new FixtureDef();
        bodyFixtureDef.shape = bodyFixtureShape;
        bodyFilter = new Filter();
        bodyFilter.categoryBits = GameContactListener.FilterBits.EFFECT.getBit();
        if (isPlayer) {
            bodyFilter.maskBits = (short) (GameContactListener.FilterBits.EFFECT.getBit()
                + GameContactListener.FilterBits.ENEMY.getBit()
                + GameContactListener.FilterBits.STATIC.getBit());
            bodyFilter.groupIndex = GameContactListener.FilterGroup.PLAYERATTACK.getBit();
        } else {
            bodyFilter.maskBits = (short) (GameContactListener.FilterBits.EFFECT.getBit()
                + GameContactListener.FilterBits.PLAYER.getBit()
                + GameContactListener.FilterBits.STATIC.getBit());
            bodyFilter.groupIndex = GameContactListener.FilterGroup.ENEMYATTACK.getBit();
        }
    }

    public void init() {
        bodyFixtureDef.isSensor = destroyOnStatic;
        Fixture fixture = getBody().createFixture(bodyFixtureDef);
        fixture.setFilterData(bodyFilter);
        fixture.setUserData(this);
        bodyFixtureShape.dispose();

        getBody().setGravityScale(0);
    }

    public void setReflectOnStatic() {
        this.destroyOnStatic = false;
    }

    public void setPassThroughEnemies() {
        if (isPlayer) bodyFilter.maskBits -= GameContactListener.FilterBits.ENEMY.getBit();
        else bodyFilter.maskBits -= GameContactListener.FilterBits.PLAYER.getBit();
        FixtureDef damageFixtureDef = new FixtureDef();
        damageFixtureDef.isSensor = true;
        damageFixtureDef.shape = bodyFixtureShape;
        Filter damageFixtureFilter = new Filter();
        damageFixtureFilter.categoryBits = GameContactListener.FilterBits.EFFECT.getBit();
        if (isPlayer) damageFixtureFilter.maskBits = GameContactListener.FilterBits.ENEMY.getBit();
        else damageFixtureFilter.maskBits = GameContactListener.FilterBits.PLAYER.getBit();
        damageFixtureFilter.groupIndex = GameContactListener.FilterGroup.PLAYERATTACK.getBit();
        Fixture damageFixture = getBody().createFixture(damageFixtureDef);
        damageFixture.setFilterData(damageFixtureFilter);
        damageFixture.setUserData(this);
        this.destroyOnEnemy = false;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (this.health <= 0 && getState() != EffectState.DESTROYED) {
            setState(EffectState.DESTROYED);
            onDestroy();
        }
        lifetime -= delta;
        if (lifetime <= 0 && getState() != EffectState.DESTROYED) destroy();
    }

    public void onDestroy() { }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        super.beginContact(entityFixture, contactFixture);
        if (contactFixture.getUserData() instanceof Actor) {
            Actor<?> actor = ((Actor<?>) contactFixture.getBody().getUserData());
            if (!actor.stats().getHitGroupInvul(getHitGroup())) {
                actor.damage(getDamage(), getBody().getPosition(), getKnockback());
                actor.stats().addHitGroupInvul(getHitGroup(), getHitGroupCD());
            }
            if (destroyOnEnemy && !actor.stats().isIntangible()) this.health = 0;
        } else if (contactFixture.getUserData() instanceof Projectile) {
            Projectile collideProjectile = (Projectile) contactFixture.getUserData();
            float damage = collideProjectile.health;
            collideProjectile.health -= this.health;
            this.health -= damage;
        } else if (contactFixture.getUserData() instanceof MeleeAttack) {
            MeleeAttack collideAttack = (MeleeAttack) contactFixture.getUserData();
            this.health -= collideAttack.getDamage() * 1.5f;
        } else {
            if (destroyOnStatic) this.health = 0;
        }
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getHealth() {
        return health;
    }
}
