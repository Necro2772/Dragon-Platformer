package io.github.dragonplatformer.Entity;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.AttackEffect.AttackEffect;
import io.github.dragonplatformer.Entity.AttackEffect.MeleeAttack;
import io.github.dragonplatformer.Entity.AttackEffect.Projectile;
import io.github.dragonplatformer.GameContactListener;
import io.github.dragonplatformer.GameScreen;

import java.util.Map;

public class Player extends Creature {
    private final GameScreen screen;
    private final Map<PlayerState, Animation<TextureRegion>> anims;
    private final Map<AttackEffect.AttackState, Animation<TextureRegion>> fireballAnims;
    private final Map<AttackEffect.AttackState, Animation<TextureRegion>> meleeAnims;
    public final playerInput input;
    private final playerStats stats;
    private PlayerState state;
    private PlayerState bufferedState;
    private float stateTime;

    public Player(TextureAtlas atlas, float x, float y, float width, float height, World world, GameScreen screen) {
        super(x, y, width, height, world);
        this.screen = screen;
        getBody().getFixtureList().get(0).getFilterData().categoryBits = GameContactListener.FilterBits.PLAYER.getBit();
        getBody().getFixtureList().get(0).setDensity(0.225f);
        getBody().resetMassData();
        stateTime = 0;
        input = new playerInput(this);
        stats = new playerStats();
        state = PlayerState.IDLE;

        anims = Map.ofEntries(
            Map.entry(PlayerState.IDLE, new Animation<>(
                1/3f,
                atlas.findRegions("dragon_idle"),
                Animation.PlayMode.NORMAL
            )), Map.entry(PlayerState.RUNNING, new Animation<>(
                1/3f,
                atlas.findRegions("dragon_run"),
                Animation.PlayMode.LOOP_PINGPONG
            )), Map.entry(PlayerState.JUMPING, new Animation<>(
                1/5f,
                atlas.findRegions("dragon_flap"),
                Animation.PlayMode.LOOP
            )), Map.entry(PlayerState.FLYING, new Animation<>(
                1/5f,
                atlas.findRegions("dragon_fly"),
                Animation.PlayMode.LOOP
            )), Map.entry(PlayerState.GLIDING, new Animation<>(
                1/3f,
                atlas.findRegions("dragon_glide"),
                Animation.PlayMode.LOOP
            )), Map.entry(PlayerState.DIVING, new Animation<>(
                1/3f,
                atlas.findRegions("dragon_dive"),
                Animation.PlayMode.LOOP
            )), Map.entry(PlayerState.DIVESOAR, new Animation<>(
                1/3f,
                atlas.findRegions("dragon_divesoar"),
                Animation.PlayMode.LOOP
            )), Map.entry(PlayerState.DASH, new Animation<>(
                1/3f,
                atlas.findRegions("dragon_dash"),
                Animation.PlayMode.LOOP
            )), Map.entry(PlayerState.DASHDIVE, new Animation<>(
                1/3f,
                atlas.findRegions("dragon_dive"),
                Animation.PlayMode.LOOP
            )), Map.entry(PlayerState.ATTACKFORWARD, new Animation<>(
                1/4f,
                atlas.findRegions("dragon_attackforward"),
                Animation.PlayMode.NORMAL
            )), Map.entry(PlayerState.ATTACKDOWN, new Animation<>(
                1/4f,
                atlas.findRegions("dragon_attackdown"),
                Animation.PlayMode.NORMAL
            )), Map.entry(PlayerState.ATTACKUP, new Animation<>(
                1/4f,
                atlas.findRegions("dragon_attackup"),
                Animation.PlayMode.NORMAL
            ))
        );

        fireballAnims = Map.ofEntries(Map.entry(AttackEffect.AttackState.IDLE, new Animation<>(
                1/3f,
                atlas.findRegions("fireball"),
                Animation.PlayMode.LOOP
            ))
        );

        meleeAnims = Map.ofEntries(Map.entry(AttackEffect.AttackState.IDLE, new Animation<>(
            1/12f,
            atlas.findRegions("clawswipe"),
            Animation.PlayMode.NORMAL
        )));
    }

    private void updatePlayerState() {
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
                input.setJump(false);
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

    private void updatePlayerMovement(float delta) {
        float maxVelocity = 7;
        float glideVelocity = 11;
        float diveGlideVelocity = 15;
        Vector2 vel = getBody().getLinearVelocity();
        Vector2 pos = getBody().getPosition();
        float speed = 0.8f;

        if (getInput().glide && !isGrounded()) {
            maxVelocity = glideVelocity;
            speed = 0.6f;
            switch (state) {
                case DIVING:
                    getInput().diveTimer += delta;
                    if (vel.y > -maxVelocity) {
                        getBody().applyLinearImpulse(0, -2.5f, pos.x, pos.y, true);
                    }
                    break;
                case DIVESOAR:
                    getInput().diveTimer -= delta;
                    if (getInput().diveTimer > 0.3f) {
                        if (vel.y < maxVelocity) {
                            getBody().applyLinearImpulse(0, 4, pos.x, pos.y, true);
                        }
                    } else {
                        getInput().diveTimer = 0;
                    }
                    break;
                case GLIDING:
                    if (getInput().diveTimer > 0) {
                        maxVelocity = diveGlideVelocity;
                        if (1 < Math.abs(vel.x) && Math.abs(vel.x) < maxVelocity && (getInput().leftMove || getInput().rightMove)) {
                            getBody().applyLinearImpulse(5 * getDirection(), 0, pos.x, pos.y, true);
                        }
                        getInput().diveTimer -= delta * 0.05f;
                    } else {
                        getInput().diveTimer = 0;
                    }
                    if (vel.y < -1 && Math.abs(vel.x) > 0.7f) {
                        getBody().applyLinearImpulse(0, -vel.y / 8, pos.x, pos.y, true);
                    }
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
        if (getInput().jump) input.setJump(false);
    }

    @Override
    public void act(float delta) {
        getStats().updateCooldowns(delta);
        updatePlayerState();
        updatePlayerMovement(delta);

        if (input.getProjectile() && getStats().getProjectileCD() < 0) {
            projectileAttack();
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

    @Override
    public void damage(int attackDamage, Vector2 attackOrigin) {
        super.damage(attackDamage, attackOrigin);
        if (attackOrigin.x - getBody().getPosition().x < 0) {
            getBody().applyLinearImpulse(new Vector2(10, 10), getBody().getPosition(), true);
        } else {
            getBody().applyLinearImpulse(new Vector2(-10, 10), getBody().getPosition(), true);
        }
    }

    @Override
    public void death() {
        screen.gameOver();
    }

    private void endState() {
    }

    private void beginState() {
        Vector2 vel = getBody().getLinearVelocity();
        Vector2 pos = getBody().getPosition();
        MeleeAttack attack;
        switch(state) {
            case JUMPING:
                getBody().applyLinearImpulse(0, getStats().jumpForce - vel.y * 1.8f, pos.x, pos.y, true);
                getInput().numJumps--;
                break;
            case DASHDIVE:
                getBody().applyLinearImpulse(0, -25 - vel.y, pos.x, pos.y, true);
                break;
            case DASH:
                getBody().applyLinearImpulse(getDirection() * 30 - vel.x, 10, pos.x, pos.y, true);
                break;
            case ATTACKUP:
                attack = new MeleeAttack(getDirection() * 1.5f, 2, 3, 3, getBody().getWorld(), meleeAnims,
                    (short) (GameContactListener.FilterBits.ENEMY.getBit()
                        + GameContactListener.FilterBits.EFFECT.getBit()),
                    GameContactListener.FilterGroup.PLAYERATTACK.getBit(),
                    getBody());
                attack.setDirection(getDirection());
                attack.setRotation(0);
                break;
            case ATTACKFORWARD:
                attack = new MeleeAttack(getDirection() * 2, 0, 3, 3, getBody().getWorld(), meleeAnims,
                    (short) (GameContactListener.FilterBits.ENEMY.getBit()
                        + GameContactListener.FilterBits.EFFECT.getBit()),
                    GameContactListener.FilterGroup.PLAYERATTACK.getBit(),
                    getBody());
                attack.setDirection(getDirection());
                attack.setRotation(0);
                break;
            case ATTACKDOWN:
                attack = new MeleeAttack(getDirection() * 1.5f, -2, 3, 3, getBody().getWorld(), meleeAnims,
                    (short) (GameContactListener.FilterBits.ENEMY.getBit()
                        + GameContactListener.FilterBits.EFFECT.getBit()),
                    GameContactListener.FilterGroup.PLAYERATTACK.getBit(),
                    getBody());
                attack.setDirection(getDirection());
                attack.setRotation(0);
                break;
        }
    }

    public PlayerState getState() {
        return state;
    }

    public boolean setState(PlayerState state) {
        if (state != this.getState() && bufferedState == null) {
            endState();
            this.state = state;
            stateTime = 0;
            beginState();
            return true;
        }
        return false;
    }

    public void setState(PlayerState state, PlayerState bufferedState) {
        if (setState(state)) {
            this.bufferedState = bufferedState;
            stateTime = 0;
        }
    }

    public float getStateTime() {
        return stateTime;
    }

    public Animation<TextureRegion> getCurrentAnim() {
        return anims.get(getState());
    }

    public playerInput getInput() {
        return input;
    }

    public playerStats getStats() {
        return stats;
    }

    public void meleeAttack() {
        switch (getState()) {
            case ATTACKFORWARD:
            case ATTACKUP:
            case ATTACKDOWN:
                return;
        }
        if (input.downMove) {
            setState(PlayerState.ATTACKDOWN, PlayerState.IDLE);
        } else if (input.upMove) {
            setState(PlayerState.ATTACKUP, PlayerState.IDLE);
        } else {
            setState(PlayerState.ATTACKFORWARD, PlayerState.IDLE);
        }
    }

    public void projectileAttack() {
        getStats().resetProjectileCD();
        float projSpeed = 15f;

        Projectile fireball = new Projectile(getBody().getPosition().x, getBody().getPosition().y,
            1.5f, 1.5f, getBody().getWorld(), fireballAnims, 1,
            (short) (GameContactListener.FilterBits.ENEMY.getBit()
                + GameContactListener.FilterBits.STATIC.getBit()
                + GameContactListener.FilterBits.EFFECT.getBit()),
            GameContactListener.FilterGroup.PLAYERATTACK.getBit());
        Vector2 impulse = new Vector2();
        impulse.x = projSpeed * getDirection();
        if (input.upMove) impulse.y = projSpeed * 0.5f;
        else if (input.downMove) impulse.y = -projSpeed * 0.5f;
        fireball.getBody().applyLinearImpulse(impulse, new Vector2(0, 0), true);
        fireball.setDirection(getDirection());
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
        ATTACKFORWARD,
        ATTACKUP,
        ATTACKDOWN
    }

    public static class playerInput {
        private final Player player;
        public boolean leftMove;
        public boolean rightMove;
        public boolean downMove;
        public boolean upMove;
        public boolean jump;
        public boolean glide;
        public boolean projectile;
        public int numJumps;
        public float diveTimer;

        public playerInput(Player player) {
            this.player = player;
            leftMove = false;
            rightMove = false;
            downMove = false;
            upMove = false;
            jump = false;
            glide = false;
            projectile = false;
            numJumps = 1;
            diveTimer = 0;
        }

        public void setLeftMove(boolean leftMove) {
            if (rightMove && leftMove) rightMove = false;
            this.leftMove = leftMove;
            if (leftMove) player.setDirection(-1);
        }

        public void setRightMove(boolean rightMove) {
            if (leftMove && rightMove) leftMove = false;
            this.rightMove = rightMove;
            if (rightMove) player.setDirection(1);
        }

        public void setDownMove(boolean downMove) {
            if (upMove && downMove) upMove = false;
            this.downMove = downMove;
        }

        public void setUpMove(boolean upMove) {
            if (downMove && upMove) downMove = false;
            this.upMove = upMove;
        }

        public void setJump(boolean jump) {
            this.jump = jump;
        }

        public void setGlide(boolean glide) {
            this.glide = glide;
        }

        public void setProjectile(boolean inputProjectile) {
            this.projectile = inputProjectile;
        }

        public boolean getProjectile() {
            return projectile;
        }
    }

    public class playerStats extends CreatureStats {
        private int maxJumps;
        private float projectileMaxCD;
        private float projectileCD;
        public float jumpForce = 30;

        public playerStats() {
            super(10);
            setMaxJumps(4);
            setProjectileMaxCD(0.3f);
            setProjectileCD(getProjectileMaxCD());
        }

        public void updateCooldowns(float delta) {
            if(getProjectileCD() > 0) setProjectileCD(getProjectileCD() - delta);
        }

        public void resetProjectileCD() {
            setProjectileCD(getProjectileMaxCD());
        }

        public int getMaxJumps() {
            return maxJumps;
        }

        public void setMaxJumps(int maxJumps) {
            this.maxJumps = maxJumps;
        }

        public float getProjectileMaxCD() {
            return projectileMaxCD;
        }

        public void setProjectileMaxCD(float projectileMaxCD) {
            this.projectileMaxCD = projectileMaxCD;
        }

        public float getProjectileCD() {
            return projectileCD;
        }

        public void setProjectileCD(float projectileCD) {
            this.projectileCD = projectileCD;
        }
    }
}
