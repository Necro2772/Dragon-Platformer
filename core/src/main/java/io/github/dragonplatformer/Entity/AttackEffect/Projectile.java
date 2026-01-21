package io.github.dragonplatformer.Entity.AttackEffect;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.Creature.Creature;
import io.github.dragonplatformer.GameContactListener;

import java.util.Map;

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
                      Map<AttackState, Animation<TextureRegion>> anims, boolean isPlayer, World world) {
        super(damage, knockback, x, y, width, height, direction, anims, null, world);
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
        if (this.health <= 0) setState(AttackState.DESTROYED);
        lifetime -= delta;
        if (lifetime <= 0 && getState() != AttackState.DESTROYED) destroy();
    }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        if (contactFixture.getUserData() instanceof Creature) {
            Creature creature = ((Creature) contactFixture.getBody().getUserData());
            creature.damage(getDamage(), getBody().getPosition(), getKnockback());
            if (destroyOnEnemy) this.health = 0;
        } else if (contactFixture.getUserData() instanceof Projectile) {
            Projectile collideProjectile = (Projectile) contactFixture.getUserData();
            float damage = collideProjectile.health;
            collideProjectile.health -= this.health;
            this.health -= damage;
        } else if (contactFixture.getUserData() instanceof MeleeAttack) {
            MeleeAttack collideAttack = (MeleeAttack) contactFixture.getUserData();
            this.health -= collideAttack.getDamage() * 1.5f;
        }else {
            if (destroyOnStatic) this.health = 0;
        }
    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {

    }
}
