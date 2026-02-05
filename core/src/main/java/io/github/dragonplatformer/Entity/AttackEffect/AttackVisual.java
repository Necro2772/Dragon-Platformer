package io.github.dragonplatformer.Entity.AttackEffect;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;

import java.util.Map;

public abstract class AttackVisual extends AttackEffect {
    public AttackVisual(float x, float y, float width, float height, int direction, Map<AttackState, Animation<TextureRegion>> anims, World world) {
        super(0, 0, x, y, width, height, direction, anims, world);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (anims.get(getState()).isAnimationFinished(getStateTime() + delta)) destroy();
    }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) { }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) { }
}
