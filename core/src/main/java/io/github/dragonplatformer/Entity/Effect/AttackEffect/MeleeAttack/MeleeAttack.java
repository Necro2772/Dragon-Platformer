package io.github.dragonplatformer.Entity.Effect.AttackEffect.MeleeAttack;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.AnimationKey;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.Actor.Player.Player;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.AttackEffect;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.EnemyDeathVisual;
import io.github.dragonplatformer.Entity.EffectManager;
import io.github.dragonplatformer.GameContactListener;

import java.util.ArrayList;
import java.util.List;

public abstract class MeleeAttack extends AttackEffect {
    private Fixture attackFixture;
    private final boolean isPlayer;
    private final AnimationManager animManager;
    private boolean enabled = true;
    private float spawnDelay = 0;
    private final List<Fixture> bufferedContacts = new ArrayList<>();

    /** Creates a new attack hitbox attached to the given body. Does not create the fixture until init() is called.
     *
     * @param damage damage dealt by the hitbox.
     * @param knockback knockback on anyone hit by the hixbox.
     * @param width width of the sprite in world units.
     * @param height height of the sprite in world units.
     * @param animKey animation key from AnimationManager.
     * @param body body to attach the hitbox fixture to.
     */
    public MeleeAttack(float damage, float knockback, float width, float height, AnimationKey animKey,
                       EffectManager effectManager, AnimationManager animManager, Body body) {
        super(damage, knockback, width, height, animKey, effectManager, animManager, body);
        setDisjointFixture(true);
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
     * @param animKey animation key from AnimationManager.
     * @param isPlayer true if this should hit enemies, false if it should hit the player.
     * @param world Box2D world to create the body in.
     */
    public MeleeAttack(float x, float y, float damage, float knockback, float width, float height, AnimationKey animKey,
                       EffectManager effectManager, AnimationManager animManager, boolean isPlayer, World world) {
        super(damage, knockback, x, y, width, height, animKey, effectManager, animManager, world);
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
        super.init();
        PolygonShape fixtureShape = new PolygonShape();
        fixtureShape.setAsBox(width / 2f, height / 2f, offset, 0);
        init(fixtureShape, offset);
        fixtureShape.dispose();
    }

    /** Initializes the fixture using a circle with the given radius and center.
     *
     * @param radius radius of the fixture in world units.
     * @param offset center of the circle relative to its body in world units.
     */
    public void init(float radius, Vector2 offset) {
        super.init();
        CircleShape attackFixtureShape = new CircleShape();
        attackFixtureShape.setRadius(radius);
        attackFixtureShape.setPosition(offset);
        init(attackFixtureShape, offset);
        attackFixtureShape.dispose();
    }

    /** Initializes the fixture using the given shape. Remember to destroy the shape, it is not needed after use.
     *
     * @param shape fixture shape for the attack hitbox.
     * @param offset center of the rectangle relative to its body in world units.
     */
    private void init(Shape shape, Vector2 offset) {
        super.init();
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
        } else {
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
    public void destroy() {
        if (!isDisjointFixture()) super.destroy();
        else getBody().destroyFixture(this.attackFixture);
    }

    @Override
    public boolean hit(Fixture contactFixture) {
        new EnemyDeathVisual(getBody().getPosition().x + getPositionOffset().x,
            getBody().getPosition().y + getPositionOffset().y, effectManager, animManager, getBody().getWorld());
        if (getBody().getUserData() instanceof Player) {
            Player player = ((Player) getBody().getUserData());
            player.input().incrementMeleeHit();
        }
        return super.hit(contactFixture);
    }

    @Override
    public void setSpawnDelay(float spawnDelay) {
        this.spawnDelay = spawnDelay;
        this.enabled = false;
    }

    @Override
    public void act(float delta) {
        if (this.enabled) super.act(delta);
        else {
            spawnDelay -= delta;
            if (spawnDelay <= 0) {
                this.enabled = true;
                for (Fixture contactFixture : bufferedContacts) {
                    beginContact(attackFixture, contactFixture);
                }
            }
        }
    }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        if (enabled) super.beginContact(entityFixture, contactFixture);
        else bufferedContacts.add(contactFixture);
    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {
        if (enabled) super.endContact(entityFixture, contactFixture);
        else bufferedContacts.remove(contactFixture);
    }

    @Override
    public void draw(SpriteBatch batch, float delta) {
        if (enabled) super.draw(batch, delta);
    }
}
