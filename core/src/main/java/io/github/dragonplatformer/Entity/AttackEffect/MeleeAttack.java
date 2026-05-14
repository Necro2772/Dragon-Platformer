package io.github.dragonplatformer.Entity.AttackEffect;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.Creature.Creature;
import io.github.dragonplatformer.Entity.Creature.Player.Player;
import io.github.dragonplatformer.GameContactListener;

import java.util.Map;

public abstract class MeleeAttack extends AttackEffect {
    private Fixture attackFixture;
    private final boolean destroyBody;
    private final boolean isPlayer;

    /** Creates a new attack hitbox attached to the given body. Does not create the fixture until init() is called.
     *
     * @param damage damage dealt by the hitbox.
     * @param knockback knockback on anyone hit by the hixbox.
     * @param width width of the sprite in world units.
     * @param height height of the sprite in world units.
     * @param direction 1 if facing right, -1 if left.
     * @param anims animation map from AnimationManager.getEffectAnims().
     * @param body body to attach the hitbox fixture to.
     */
    public MeleeAttack(float damage, float knockback, float width, float height, int direction,
                       Map<AttackState, Animation<TextureRegion>> anims, Body body) {
        super(damage, knockback, width, height, direction, anims, body);
        destroyBody = false;
        this.isPlayer = body.getUserData() instanceof Player;
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
     * @param anims animation map from AnimationManager.getEffectAnims().
     * @param isPlayer true if this should hit enemies, false if it should hit the player.
     * @param world Box2D world to create the body in.
     */
    public MeleeAttack(float x, float y, float damage, float knockback, float width, float height,
                       int direction, Map<AttackState, Animation<TextureRegion>> anims, boolean isPlayer, World world) {
        super(damage, knockback, x, y, width, height, direction, anims, world);
        destroyBody = true;
        this.isPlayer = isPlayer;
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
                + GameContactListener.FilterBits.ENEMY.getBit());
            filter.groupIndex = GameContactListener.FilterGroup.PLAYERATTACK.getBit();
        }
        else {
            filter.maskBits = (short)(GameContactListener.FilterBits.EFFECT.getBit()
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
        getBody().destroyFixture(this.attackFixture);
        if (destroyBody) getBody().getWorld().destroyBody(getBody());
    }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        if (contactFixture.getBody().getUserData() instanceof Creature) {
            Creature creature = ((Creature) contactFixture.getBody().getUserData());
            if (!creature.stats().getHitGroupInvul(getHitGroup())){
                creature.damage(getDamage(), getBody().getPosition(), getKnockback());
                creature.stats().addHitGroupInvul(getHitGroup(), getHitGroupCD());
            }
        }
        if (getBody().getUserData() instanceof Player) {
            Player player = ((Player) getBody().getUserData());
            player.meleeHitEffect();
        }
    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {

    }
}
