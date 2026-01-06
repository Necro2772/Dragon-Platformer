package io.github.dragonplatformer.Entity.Creature;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.AnimationManager;

public class Bat extends Enemy {
    private float prevPos;
    private float flapForce = 5f;
    private float waitTime;

    public Bat(float x, float y, float width, float height, World world, AnimationManager animManager) {
        super(x, y, width, height, new Vector2(width/2, height/2), world, animManager, AnimationManager.AnimationKeys.ENEMY_BAT,
            new Vector2(15, 20), true);
        getStats().init(2);
        getBody().setGravityScale(0.75f);
        prevPos = 0;
        waitTime = (float) Math.random() * 2 + 1;
        setAggroRange(50);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (getState() == EnemyState.DEATH) return;
        Vector2 pos = getBody().getPosition();
        Vector2 vel = getBody().getLinearVelocity();
        if (getPlayerSighted()) {
            switch (getState()) {
                case IDLE:
                    if (getStateTime() > waitTime) {
                        setState(EnemyState.ATTACKING);
                        waitTime = (float) Math.random() * 2 + 1;
                    }
                    if (pos.y - getPlayerPos().y < 5) flapForce = 8f;
                    if (vel.y < -2) getBody().applyForceToCenter(0, 10, true);
                    break;
                case ATTACKING:
                    flapForce = 0;
                    if (getStateTime() > 0.25f) setState(EnemyState.IDLE);
                    break;
            }
        } else {
            setState(EnemyState.IDLE);
            getBody().applyForceToCenter(-vel.x * 2, 0, true);
        }
        if (anims.get(getState()).getKeyFrameIndex(getStateTime()) == 0 &&
            anims.get(getState()).getKeyFrameIndex(getStateTime() + delta) == 1 ) {
            float flapHorizontal = 0;
            if (prevPos != 0) {
                flapForce += MathUtils.clamp(prevPos - pos.y, -2, 2);
                flapForce = MathUtils.clamp(flapForce, -2, 12);
            }
            StaticRayCast rayCast = new StaticRayCast();
            getBody().getWorld().rayCast(rayCast, pos, new Vector2(0, -5).add(pos));
            if (rayCast.hit) flapForce += 1;
            else {
                getBody().getWorld().rayCast(rayCast, pos, new Vector2(0, 5).add(pos));
                if (rayCast.hit) flapForce -= 1;
            }
            if (getState() != EnemyState.ATTACKING && Math.abs(pos.x - getPlayerPos().x) < 5) {
                if (pos.x > getPlayerPos().x) flapHorizontal = 5;
                else flapHorizontal = -5;
            }
            getBody().applyLinearImpulse(new Vector2(flapHorizontal, flapForce * getBody().getMass()), pos, true);
            prevPos = pos.y;
        }
    }

    @Override
    public void beginState() {
        super.beginState();
        switch (getState()) {
            case ATTACKING:
                Vector2 dir = new Vector2(getPlayerPos());
                dir.sub(getBody().getPosition()).nor().scl(16);
                getBody().applyLinearImpulse(dir.sub(getBody().getLinearVelocity()), getBody().getPosition(), true);
                break;
        }
    }

    private static class StaticRayCast implements RayCastCallback {
        public boolean hit;

        public StaticRayCast(){
            super();
        }

        public void reset() {
            hit = false;
        }

        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            if (fixture.getBody().getType() == BodyDef.BodyType.StaticBody) {
                hit = true;
                return 0;
            }
            return -1;
        }
    }
}
