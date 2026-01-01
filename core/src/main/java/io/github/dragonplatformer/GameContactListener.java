package io.github.dragonplatformer;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import io.github.dragonplatformer.Entity.Entity;

public class GameContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        if (contact.getFixtureA().getBody().getUserData() instanceof Entity) {
            ((Entity) contact.getFixtureA().getBody().getUserData()).beginContact(contact.getFixtureA(), contact.getFixtureB());
        }
        if (contact.getFixtureB().getBody().getUserData() instanceof Entity) {
            ((Entity) contact.getFixtureB().getBody().getUserData()).beginContact(contact.getFixtureB(), contact.getFixtureA());
        }
    }

    @Override
    public void endContact(Contact contact) {
        if (contact.getFixtureA().getBody().getUserData() instanceof Entity) {
            ((Entity) contact.getFixtureA().getBody().getUserData()).endContact(contact.getFixtureA(), contact.getFixtureB());
        }
        if (contact.getFixtureB().getBody().getUserData() instanceof Entity) {
            ((Entity) contact.getFixtureB().getBody().getUserData()).endContact(contact.getFixtureB(), contact.getFixtureA());
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

    public enum FilterBits {
        STATIC ((short) 1),
        SENSOR ((short) 2),
        PLAYER ((short) 4),
        ENEMY ((short) 8),
        EFFECT ((short) 16);

        final short categoryBit;

        FilterBits(short v) {
            this.categoryBit = v;
        }

        public short getBit() {
            return this.categoryBit;
        }
    }
}
