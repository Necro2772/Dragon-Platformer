package io.github.dragonplatformer.Entity.Effect.AttackEffect.Projectile;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Actor.Actor;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.AttackEffect;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack.MeleeAttack;
import io.github.dragonplatformer.Entity.Effect.EffectState;
import io.github.dragonplatformer.Entity.EffectManager;
import io.github.dragonplatformer.Entity.MovementType;
import io.github.dragonplatformer.Screen.GameContactListener;

public abstract class Projectile extends AttackEffect {
    private float health;
    private float lifetime;
    private final FixtureDef bodyFixtureDef = new FixtureDef();
    private FixtureDef damageFixtureDef = null;
    private Shape bodyFixtureShape;
    private boolean reflectOnStatic = false;
    private boolean destroyOnStatic = true;
    private float restitution;
    private boolean destroyOnEnemy = true;
    private final boolean isPlayer;
    private final ParticleEffect effect;

    public Projectile(float damage, float knockback, float health, float lifetime, float x, float y,
                      float radius, ParticleEffect effect, AnimationKey animKey, EffectManager effectManager,
                      AnimationManager animManager, boolean isPlayer, World world) {
        this(damage, knockback, health, lifetime, x, y, radius, radius, effect, animKey, effectManager, animManager, isPlayer, world);
        setCollisionAsCircle(radius, new Vector2());
    }

    public Projectile(float damage, float knockback, float health, float lifetime, float x, float y,
                      float width, float height, ParticleEffect effect, AnimationKey animKey, EffectManager effectManager,
                      AnimationManager animManager, boolean isPlayer, World world) {
        super(damage, knockback, x, y, width, height, animKey, effectManager, animManager, world);
        this.health = health;
        this.lifetime = lifetime;
        this.isPlayer = isPlayer;
        bodyFixtureShape = new PolygonShape();
        ((PolygonShape) bodyFixtureShape).setAsBox(getWidth() / 2f, getHeight() / 2f, new Vector2(), 0);

        this.effect = effect;
        this.effect.start();
        this.effect.setPosition(getPosition().x, getPosition().y);
    }

    @Override
    public void init() {
        super.init();
        if (getTargetPos().x < spawnPos().x) {
            setSpriteDirection(-1);
            if (bodyFixtureShape instanceof CircleShape) {
                ((CircleShape) bodyFixtureShape).setPosition(((CircleShape) bodyFixtureShape).getPosition().scl(-1, 1));
            }
        } else {
            setSpriteDirection(1);
        }

        bodyFixtureDef.shape = bodyFixtureShape;
        bodyFixtureDef.filter.categoryBits = GameContactListener.FilterBits.EFFECT.getBit();
        if (isPlayer) {
            bodyFixtureDef.filter.maskBits = (short) (GameContactListener.FilterBits.EFFECT.getBit()
                + GameContactListener.FilterBits.ENEMY.getBit());
            bodyFixtureDef.filter.groupIndex = GameContactListener.FilterGroup.PLAYERATTACK.getBit();
        } else {
            bodyFixtureDef.filter.maskBits = (short) (GameContactListener.FilterBits.EFFECT.getBit()
                + GameContactListener.FilterBits.PLAYER.getBit());
            bodyFixtureDef.filter.groupIndex = GameContactListener.FilterGroup.ENEMYATTACK.getBit();
        }

        if (reflectOnStatic || destroyOnStatic) {
            bodyFixtureDef.filter.maskBits += GameContactListener.FilterBits.STATIC.getBit();
            if (reflectOnStatic) {
                bodyFixtureDef.restitution = restitution;
            }
        }


        bodyFixtureDef.isSensor = destroyOnStatic;
        if (damageFixtureDef != null) {
            Fixture damageFixture = getBody().createFixture(damageFixtureDef);
            damageFixture.setUserData(this);
        }
        Fixture fixture = getBody().createFixture(bodyFixtureDef);
        fixture.setUserData(this);
        bodyFixtureShape.dispose();
    }

    public void setCollisionAsCircle(float radius, Vector2 center) {
        bodyFixtureShape.dispose();
        bodyFixtureShape = new CircleShape();
        bodyFixtureShape.setRadius(radius);
        ((CircleShape) bodyFixtureShape).setPosition(center);
    }

    public void setReflectOnStatic() {
        this.destroyOnStatic = false;
        this.reflectOnStatic = true;
        this.restitution = 1;
    }

    public void setReflectOnStatic(float restitution) {
        this.destroyOnStatic = false;
        this.reflectOnStatic = true;
        this.restitution = restitution;
    }

    public void setPassThroughStatic() {
        this.destroyOnStatic = false;
        this.reflectOnStatic = false;
    }

    public void setPassThroughEnemies() {
        damageFixtureDef = new FixtureDef();
        if (isPlayer) bodyFixtureDef.filter.maskBits -= GameContactListener.FilterBits.ENEMY.getBit();
        else bodyFixtureDef.filter.maskBits -= GameContactListener.FilterBits.PLAYER.getBit();
        FixtureDef damageFixtureDef = new FixtureDef();
        damageFixtureDef.isSensor = true;
        damageFixtureDef.shape = bodyFixtureShape;
        damageFixtureDef.filter.categoryBits = GameContactListener.FilterBits.EFFECT.getBit();
        if (isPlayer) damageFixtureDef.filter.maskBits = GameContactListener.FilterBits.ENEMY.getBit();
        else damageFixtureDef.filter.maskBits = GameContactListener.FilterBits.PLAYER.getBit();
        damageFixtureDef.filter.groupIndex = GameContactListener.FilterGroup.PLAYERATTACK.getBit();
        destroyOnEnemy = false;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        lifetime -= delta;
        effect.setPosition(getPosition().x, getPosition().y);
        if (getState() != EffectState.DESTROYED) {
            if (health <= 0) setState(EffectState.DESTROYED);
            else if (lifetime <= 0) {
                onExpire();
                destroy();
            }
        }
    }

    @Override
    public void destroy() {
        effect.allowCompletion();
        super.destroy();
    }

    public void onExpire() {

    }

    @Override
    public boolean hit(Fixture contactFixture) {
        boolean result = super.hit(contactFixture);
        if (contactFixture.getUserData() instanceof Actor) {
            if (result) this.health = 0;
        } else if (contactFixture.getUserData() instanceof Projectile) {
            Projectile collideProjectile = (Projectile) contactFixture.getUserData();
            float damage = collideProjectile.health;
            collideProjectile.health -= this.health;
            this.health -= damage;
            return true;
        } else if (contactFixture.getUserData() instanceof MeleeAttack) {
            MeleeAttack collideAttack = (MeleeAttack) contactFixture.getUserData();
            if (collideAttack.getReflect()) reflect();
            else this.health -= collideAttack.getDamage() * 1.5f;
            return collideAttack.getDamage() > 0;
        } else {
            if (destroyOnStatic) this.health = 0;
            return true;
        }
        return result;
    }

    public void reflect() {
        applyWeightedImpulse(new Vector2(getBody().getLinearVelocity()).scl(-2));
        if (getMovementType() == MovementType.LINE) {
            Vector2 target = new Vector2(getTargetPos());
            setTargetPos(new Vector2(spawnPos()));
            spawnPos().set(target);
        }

        for (Fixture fixture : getBody().getFixtureList()) {
            Filter filter = fixture.getFilterData();
            if (filter.groupIndex == GameContactListener.FilterGroup.ENEMYATTACK.getBit()) {
                filter.groupIndex = GameContactListener.FilterGroup.PLAYERATTACK.getBit();
            }
            if ((filter.maskBits & GameContactListener.FilterBits.PLAYER.getBit())
                == GameContactListener.FilterBits.PLAYER.getBit()) {
                filter.maskBits -= GameContactListener.FilterBits.PLAYER.getBit();
                filter.maskBits += GameContactListener.FilterBits.ENEMY.getBit();
            }
            fixture.setFilterData(filter);
        }
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getHealth() {
        return health;
    }

    public boolean getIsPlayer() {
        return isPlayer;
    }
}
