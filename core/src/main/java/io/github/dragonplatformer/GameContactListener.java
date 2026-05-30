package io.github.dragonplatformer;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import io.github.dragonplatformer.Entity.Entity;

public class GameContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        if (contact.getFixtureA().getUserData() instanceof Entity) {
            ((Entity<?>) contact.getFixtureA().getUserData()).beginContact(contact.getFixtureA(), contact.getFixtureB());
        } else if (contact.getFixtureA().getBody().getUserData() instanceof Entity) {
            ((Entity<?>) contact.getFixtureA().getBody().getUserData()).beginContact(contact.getFixtureA(), contact.getFixtureB());
        }
        if (contact.getFixtureB().getUserData() instanceof Entity) {
            ((Entity<?>) contact.getFixtureB().getUserData()).beginContact(contact.getFixtureB(), contact.getFixtureA());
        } else if (contact.getFixtureB().getBody().getUserData() instanceof Entity) {
            ((Entity<?>) contact.getFixtureB().getBody().getUserData()).beginContact(contact.getFixtureB(), contact.getFixtureA());
        }
    }

    @Override
    public void endContact(Contact contact) {
        if (contact.getFixtureA().getUserData() instanceof Entity) {
            ((Entity<?>) contact.getFixtureA().getUserData()).endContact(contact.getFixtureA(), contact.getFixtureB());
        } else if (contact.getFixtureA().getBody().getUserData() instanceof Entity) {
            ((Entity<?>) contact.getFixtureA().getBody().getUserData()).endContact(contact.getFixtureA(), contact.getFixtureB());
        }
        if (contact.getFixtureB().getUserData() instanceof Entity) {
            ((Entity<?>) contact.getFixtureB().getUserData()).endContact(contact.getFixtureB(), contact.getFixtureA());
        } else if (contact.getFixtureB().getBody().getUserData() instanceof Entity) {
            ((Entity<?>) contact.getFixtureB().getBody().getUserData()).endContact(contact.getFixtureB(), contact.getFixtureA());
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

    public enum FilterGroup {
        LOOT ((short) -3),
        ENEMYATTACK ((short) -2),
        PLAYERATTACK ((short) -1),
        ENEMYDEFAULT ((short) 1);

        final short groupBit;

        FilterGroup(short g) {
            this.groupBit = g;
        }

        public short getBit() {
            return this.groupBit;
        }
    }

    public enum FilterBits {
        STATIC ((short) 1),
        SENSOR ((short) 2),
        PLAYER ((short) 4),
        ENEMY ((short) 8),
        EFFECT ((short) 16),
        LOOT ((short) 32),
        NONE ((short) 64);

        final short categoryBit;

        FilterBits(short v) {
            this.categoryBit = v;
        }

        public short getBit() {
            return this.categoryBit;
        }
    }
}
