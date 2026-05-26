package io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Actor.Actor;
import io.github.dragonplatformer.Entity.Actor.Player.Player;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.AttackEffect;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.EnemyDeathVisual;
import io.github.dragonplatformer.GameContactListener;

public abstract class MeleeAttack extends AttackEffect {
    private Fixture attackFixture;
    private final boolean destroyBody;
    private final boolean isPlayer;
    private final AnimationManager animManager;

    /** Creates a new attack hitbox attached to the given body. Does not create the fixture until init() is called.
     *
     * @param damage damage dealt by the hitbox.
     * @param knockback knockback on anyone hit by the hixbox.
     * @param width width of the sprite in world units.
     * @param height height of the sprite in world units.
     * @param direction 1 if facing right, -1 if left.
     * @param animKey animation key from AnimationManager.
     * @param body body to attach the hitbox fixture to.
     */
    public MeleeAttack(float damage, float knockback, float width, float height, int direction,
                       AnimationKey animKey, AnimationManager animManager, Body body) {
        super(damage, knockback, width, height, direction, animKey, animManager, body);
        destroyBody = false;
        this.isPlayer = body.getUserData() instanceof Player;
        this.animManager = animManager;
    }


    /** Creates a new attack hitbox attached to the given body. Does not create the fixture until init() is called.
     *
     * @param x global x position in world units.
     * @param y global y position in world units.
     * @param damage damage dealt by the hitbox.
     * @param knockback knockback on anyone hit by the hixbox.
     * @param width width of the sprite in world units.
     * @param height height of the sprite in world units.
     * @param direction 1 if facing right, -1 if left.
     * @param animKey animation key from AnimationManager.
     * @param isPlayer true if this should hit enemies, false if it should hit the player.
     * @param world Box2D world to create the body in.
     */
    public MeleeAttack(float x, float y, float damage, float knockback, float width, float height,
                       int direction, AnimationKey animKey,
                       AnimationManager animManager, boolean isPlayer, World world) {
        super(damage, knockback, x, y, width, height, direction, animKey, animManager, world);
        destroyBody = true;
        this.isPlayer = isPlayer;
        this.animManager = animManager;
    }

    /** Initializes the fixture using a rectangle with the given width and height.
     *
     * @param width width in world units.
     * @param height height in world units.
     * @param offset center of the rectangle relative to its body in world units.
     */
    public void init(float width, float height, Vector2 offset) {
        PolygonShape fixtureShape = new PolygonShape();
        fixtureShape.setAsBox(width / 2f, height / 2f, offset, 0);
        init(fixtureShape, offset);
        fixtureShape.dispose();
    }

    /** Initializes the fixture using the given shape. Remember to destroy the shape, it is not needed after use.
     *
     * @param shape fixture shape for the attack hitbox.
     * @param offset center of the rectangle relative to its body in world units.
     */
    public void init(Shape shape, Vector2 offset) {
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.isSensor = true;
        fixtureDef.shape = shape;
        Filter filter = new Filter();
        filter.categoryBits = GameContactListener.FilterBits.EFFECT.getBit();
        if (isPlayer) {
            filter.maskBits = (short)(GameContactListener.FilterBits.EFFECT.getBit()
                + GameContactListener.FilterBits.STATIC.getBit()
                + GameContactListener.FilterBits.ENEMY.getBit());
            filter.groupIndex = GameContactListener.FilterGroup.PLAYERATTACK.getBit();
        }
        else {
            filter.maskBits = (short)(GameContactListener.FilterBits.EFFECT.getBit()
                + GameContactListener.FilterBits.STATIC.getBit()
                + GameContactListener.FilterBits.PLAYER.getBit());
            filter.groupIndex = GameContactListener.FilterGroup.ENEMYATTACK.getBit();
        }
        attackFixture = getBody().createFixture(fixtureDef);
        attackFixture.setFilterData(filter);
        attackFixture.setUserData(this);

        setPositionOffset(offset);
    }

    @Override
    public void onHit() {
        super.onHit();
        new EnemyDeathVisual(getBody().getPosition().x + getPositionOffset().x,
            getBody().getPosition().y + getPositionOffset().y, animManager, getBody().getWorld());
    }

    @Override
    public void destroy() {
        getBody().destroyFixture(this.attackFixture);
        if (destroyBody) super.destroy();
    }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        super.beginContact(entityFixture, contactFixture);
        if (contactFixture.getBody().getUserData() instanceof Actor) {
            Actor<?> actor = ((Actor<?>) contactFixture.getBody().getUserData());
            if (!actor.stats().getHitGroupInvul(getHitGroup())){
                actor.damage(getDamage(), getBody().getPosition(), getKnockback());
                actor.stats().addHitGroupInvul(getHitGroup(), getHitGroupCD());
            }
        }
        if (getBody().getUserData() instanceof Player) {
            Player player = ((Player) getBody().getUserData());
            player.input().incrementMeleeHit();
        }
    }
}
