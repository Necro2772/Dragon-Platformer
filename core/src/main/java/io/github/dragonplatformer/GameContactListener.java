package io.github.dragonplatformer;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

public class GameContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        if (contact.getFixtureA().getBody().getUserData() instanceof Player) {
            ((Player) contact.getFixtureA().getBody().getUserData()).beginContact(contact.getFixtureA(), contact.getFixtureB());
        } else if (contact.getFixtureB().getBody().getUserData() instanceof Player) {
            ((Player) contact.getFixtureB().getBody().getUserData()).beginContact(contact.getFixtureB(), contact.getFixtureA());
        }
    }

    @Override
    public void endContact(Contact contact) {
        if (contact.getFixtureA().getBody().getUserData() instanceof Player) {
            ((Player) contact.getFixtureA().getBody().getUserData()).endContact(contact.getFixtureA(), contact.getFixtureB());
        } else if (contact.getFixtureB().getBody().getUserData() instanceof Player) {
            ((Player) contact.getFixtureB().getBody().getUserData()).endContact(contact.getFixtureB(), contact.getFixtureA());
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
