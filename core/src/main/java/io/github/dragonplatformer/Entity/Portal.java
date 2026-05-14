package io.github.dragonplatformer.Entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.Creature.Player.Player;
import io.github.dragonplatformer.GameContactListener;
import io.github.dragonplatformer.GameScreen;

public class Portal extends Entity {
    public final String destination;
    public final GameScreen screen;

    public Portal(float x, float y, float width, float height, World world, String destination, GameScreen screen) {
        super(x, y, width, height, world);
        getBody().setType(BodyDef.BodyType.StaticBody);
        this.destination = destination;
        this.screen = screen;

        PolygonShape collisionRec = new PolygonShape();
        collisionRec.setAsBox(width / 2f, height / 2f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = collisionRec;
        fixtureDef.isSensor = true;
        Fixture fixture = getBody().createFixture(fixtureDef);
        fixture.getFilterData().categoryBits = GameContactListener.FilterBits.SENSOR.getBit();
        fixture.setUserData(this);
        collisionRec.dispose();
    }

    @Override
    public void act(float delta) {

    }

    @Override
    public void draw(SpriteBatch batch, float delta) {

    }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        if (contactFixture.getBody().getUserData() instanceof Player) {
            screen.changeLevel(destination);
        }
    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {

    }
}
