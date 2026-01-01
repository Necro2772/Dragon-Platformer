package io.github.dragonplatformer.Entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.GameContactListener;

import java.util.Map;

public class Player extends NPC {
    private final Map<PlayerState, Animation<TextureRegion>> anims;
    private final playerInput input;
    private final playerStats stats;
    private PlayerState state;
    private PlayerState bufferedState;
    private float stateTime;

    public Player(TextureAtlas atlas, float x, float y, float width, float height, World world) {
        super(x, y, width, height, world);
        getBody().getFixtureList().get(0).getFilterData().categoryBits = GameContactListener.FilterBits.PLAYER.getBit();

        MassData md = new MassData();
        md.mass = 2f;
        getBody().setMassData(md);
        stateTime = 0;
        input = new playerInput();
        stats = new playerStats();
        state = PlayerState.IDLE;

        anims = Map.ofEntries(
            Map.entry(Player.PlayerState.IDLE, new Animation<>(
                1/3f,
                atlas.findRegions("dragon_idle"),
                Animation.PlayMode.NORMAL
            )), Map.entry(Player.PlayerState.RUNNING, new Animation<>(
                1/3f,
                atlas.findRegions("dragon_run"),
                Animation.PlayMode.LOOP_PINGPONG
            )), Map.entry(Player.PlayerState.JUMPING, new Animation<>(
                1/5f,
                atlas.findRegions("dragon_flap"),
                Animation.PlayMode.LOOP
            )), Map.entry(Player.PlayerState.FLYING, new Animation<>(
                1/5f,
                atlas.findRegions("dragon_fly"),
                Animation.PlayMode.LOOP
            )), Map.entry(Player.PlayerState.GLIDING, new Animation<>(
                1/3f,
                atlas.findRegions("dragon_glide"),
                Animation.PlayMode.LOOP
            )), Map.entry(Player.PlayerState.DIVING, new Animation<>(
                1/3f,
                atlas.findRegions("dragon_dive"),
                Animation.PlayMode.LOOP
            )), Map.entry(Player.PlayerState.DIVESOAR, new Animation<>(
                1/3f,
                atlas.findRegions("dragon_divesoar"),
                Animation.PlayMode.LOOP
            )), Map.entry(Player.PlayerState.DASH, new Animation<>(
                1/3f,
                atlas.findRegions("dragon_dash"),
                Animation.PlayMode.LOOP
            )), Map.entry(Player.PlayerState.DASHDIVE, new Animation<>(
                1/3f,
                atlas.findRegions("dragon_dive"),
                Animation.PlayMode.LOOP
            ))
        );
    }

    private PlayerState updatePlayerState() {
        PlayerState nextState = PlayerState.IDLE;
        if (getInput().glide && !isGrounded()) {
            if (getInput().downMove) {
                nextState = PlayerState.DIVING;
            } else if (getInput().upMove && getInput().diveTimer > 0) {
                nextState = PlayerState.DIVESOAR;
            } else {
                nextState = PlayerState.GLIDING;
            }
            if (getInput().jump && getInput().numJumps > 0) {
                if (getInput().downMove) {
                    nextState = PlayerState.DASHDIVE;
                } else {
                    nextState = PlayerState.DASH;
                }
                getInput().numJumps--;
                setJump(false);
            }
        } else {
            getInput().diveTimer = 0;
        }
        if (getInput().jump && getInput().numJumps > 0) {
            nextState = PlayerState.JUMPING;
        }
        if (nextState == PlayerState.IDLE) {
            if (isGrounded() && getState() != PlayerState.JUMPING) {
                if (getInput().rightMove || getInput().leftMove) nextState = PlayerState.RUNNING;
            } else {
                nextState = PlayerState.FLYING;
            }
        }
        return nextState;
    }

    @Override
    public void act(float delta) {
        float maxVelocity = 7;
        Vector2 vel = getBody().getLinearVelocity();
        Vector2 pos = getBody().getPosition();
        float speed = 0.8f;

        PlayerState nextState = updatePlayerState();

        if (getInput().glide && !isGrounded()) {
            maxVelocity = 9;
            speed = 0.6f;
            switch (nextState) {
                case DIVING:
                    getInput().diveTimer += delta;
                    if (vel.y > -8) {
                        getBody().applyLinearImpulse(0, -1.3f, pos.x, pos.y, true);
                    }
                    break;
                case DIVESOAR:
                    getInput().diveTimer -= delta * 1.2f;
                    if (getInput().diveTimer > 0.3f) {
                        if (vel.y < 7) {
                            getBody().applyLinearImpulse(0, 4, pos.x, pos.y, true);
                        }
                    } else {
                        getInput().diveTimer = 0;
                    }
                    break;
                case GLIDING:
                    if (getInput().diveTimer > 0) {
                        maxVelocity = 11;
                        if (1 < Math.abs(vel.x) && Math.abs(vel.x) < maxVelocity && (getInput().leftMove || getInput().rightMove)) {
                            getBody().applyLinearImpulse(5 * getDirection(), 0, pos.x, pos.y, true);
                        }
                        getInput().diveTimer -= delta * 0.1f;
                    } else {
                        getInput().diveTimer = 0;
                    }
                    if (vel.y < -1 && Math.abs(vel.x) > 0.7f) {
                        getBody().applyLinearImpulse(0, -vel.y / 8, pos.x, pos.y, true);
                    }
                    break;
                case DASHDIVE:
                    getBody().applyLinearImpulse(0, -25 - vel.y, pos.x, pos.y, true);
                    break;
                case DASH:
                    getBody().applyLinearImpulse(getDirection() * 20 - vel.x, 10, pos.x, pos.y, true);
                    break;
            }
        }
        if (getInput().rightMove && vel.x < maxVelocity) {
            getBody().applyLinearImpulse(speed, 0, pos.x, pos.y, true);
        } else if (getInput().leftMove && vel.x > -maxVelocity) {
            getBody().applyLinearImpulse(-speed, 0, pos.x, pos.y, true);
        } else if (!getInput().leftMove && !getInput().rightMove && vel.x != 0) {
            getBody().applyLinearImpulse(-vel.x / 20, 0, pos.x, pos.y, true);
        }
        if (isGrounded() && Math.abs(vel.x) > 1 && !getInput().rightMove && !getInput().leftMove) {
            getBody().applyLinearImpulse(-vel.x / 8, 0, pos.x, pos.y, true);
        }
        if (nextState == PlayerState.JUMPING) {
            getBody().applyLinearImpulse(0, 20 - vel.y * 1.8f, pos.x, pos.y, true);
            getInput().numJumps--;
            setJump(false);
        } else if (getInput().jump) setJump(false);

        switch (nextState) {
            case JUMPING:
                setState(nextState, PlayerState.FLYING);
                break;
            case DASHDIVE:
            case DASH:
                setState(nextState, PlayerState.GLIDING);
                break;
            default:
                setState(nextState);
        }
    }

    @Override
    public void beginContact(Fixture entityFixture, Fixture contactFixture) {
        super.beginContact(entityFixture, contactFixture);
        if (getBody().getFixtureList().indexOf(entityFixture, true) == 1) {
            getInput().numJumps = getStats().getMaxJumps();
        }
    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {
        super.endContact(entityFixture, contactFixture);
    }

    @Override
    public void draw(SpriteBatch batch, float delta) {
        stateTime = stateTime + delta;
        if (bufferedState != null && anims.get(getState()).isAnimationFinished(getStateTime())) {
            state = bufferedState;
            bufferedState = null;
            stateTime = 0;
        }
        TextureRegion frame = anims.get(getState()).getKeyFrame(getStateTime());
        batch.draw(frame,
            this.getBody().getPosition().x - getWidth() / 2f,
            this.getBody().getPosition().y - getHeight() / 2f,
            getWidth() / 2f, getHeight() / 2f, getWidth(), getHeight(), getDirection(), 1, 0);
    }

    public void damage(int attackDamage, Vector2 attackOrigin) {
        getStats().setHealth(getStats().getHealth() - attackDamage);
        if (attackOrigin.x - getBody().getPosition().x < 0) {
            getBody().applyLinearImpulse(new Vector2(10, 10), getBody().getPosition(), true);
        } else {
            getBody().applyLinearImpulse(new Vector2(-10, 10), getBody().getPosition(), true);
        }
    }

    public PlayerState getState() {
        return state;
    }

    public void setState(PlayerState state) {
        if (state != this.getState() && bufferedState == null) {
            this.state = state;
            stateTime = 0;
        }
    }

    public void setState(PlayerState state, PlayerState bufferedState) {
        setState(state);
        this.bufferedState = bufferedState;
        stateTime = 0;
    }

    public float getStateTime() {
        return stateTime;
    }

    public playerInput getInput() {
        return input;
    }

    public playerStats getStats() {
        return stats;
    }

    public void setLeftMove(boolean leftMove) {
        if (getInput().rightMove && leftMove) getInput().rightMove = false;
        getInput().leftMove = leftMove;
        if (leftMove) setDirection(-1);
    }

    public void setRightMove(boolean rightMove) {
        if (getInput().leftMove && rightMove) getInput().leftMove = false;
        getInput().rightMove = rightMove;
        if (rightMove) setDirection(1);
    }

    public void setDownMove(boolean downMove) {
        if (getInput().upMove && downMove) getInput().upMove = false;
        getInput().downMove = downMove;
    }

    public void setUpMove(boolean upMove) {
        if (getInput().downMove && upMove) getInput().downMove = false;
        getInput().upMove = upMove;
    }

    public void setJump(boolean jump) {
        getInput().jump = jump;
    }

    public void setGlide(boolean glide) {
        getInput().glide = glide;
    }

    public enum PlayerState {
        IDLE,
        RUNNING,
        JUMPING,
        FLYING,
        GLIDING,
        DIVING,
        DIVESOAR,
        DASH,
        DASHDIVE,
    }

    public static class playerInput {
        public boolean leftMove;
        public boolean rightMove;
        public boolean downMove;
        public boolean upMove;
        public boolean jump;
        public boolean glide;
        public int numJumps;
        public float diveTimer;

        public playerInput() {
            leftMove = false;
            rightMove = false;
            downMove = false;
            upMove = false;
            jump = false;
            glide = false;
            numJumps = 1;
            diveTimer = 0;
        }
    }

    public static class playerStats {
        private int maxJumps;
        private int maxHealth;
        private int health;

        public playerStats() {
            setMaxJumps(4);
            setMaxHealth(10);
            setHealth(getMaxHealth());
        }

        public int getMaxJumps() {
            return maxJumps;
        }

        public void setMaxJumps(int maxJumps) {
            this.maxJumps = maxJumps;
        }

        public int getMaxHealth() {
            return maxHealth;
        }

        public void setMaxHealth(int maxHealth) {
            this.maxHealth = maxHealth;
        }

        public int getHealth() {
            return health;
        }

        public void setHealth(int health) {
            this.health = health;
        }
    }
}
