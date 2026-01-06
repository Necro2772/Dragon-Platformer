package io.github.dragonplatformer.Entity.Creature;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.github.dragonplatformer.Entity.AnimationManager;
import io.github.dragonplatformer.Entity.AttackEffect.*;
import io.github.dragonplatformer.Entity.Loot.Loot;
import io.github.dragonplatformer.GameContactListener;
import io.github.dragonplatformer.GameScreen;

import java.util.Map;

public class Player extends Creature {
    private final GameScreen screen;
    private final AnimationManager animManager;
    private final Map<PlayerState, Animation<TextureRegion>> anims;
    public final playerInput input;
    private final playerStats stats;
    private PlayerState state;
    private PlayerState bufferedState;
    private float stateTime;

    public Player(float x, float y, float width, float height, World world, GameScreen screen, AnimationManager animManager) {
        super(x, y, width, height, new Vector2(width/2, height/2), world);
        this.animManager = animManager;
        this.screen = screen;
        getBody().getFixtureList().get(0).getFilterData().categoryBits = GameContactListener.FilterBits.PLAYER.getBit();
        getBody().getFixtureList().get(0).setDensity(0.225f);
        getBody().resetMassData();
        getBody().setSleepingAllowed(false);

        FixtureDef itemPickupDef = new FixtureDef();
        itemPickupDef.isSensor = true;
        CircleShape itemPickupShape = new CircleShape();
        itemPickupShape.setRadius(15);
        itemPickupDef.shape = itemPickupShape;
        Fixture itemPickupFixture = getBody().createFixture(itemPickupDef);
        itemPickupFixture.setUserData(this);
        itemPickupFixture.getFilterData().categoryBits = GameContactListener.FilterBits.SENSOR.getBit();
        itemPickupFixture.getFilterData().maskBits = GameContactListener.FilterBits.LOOT.getBit();
        itemPickupShape.dispose();

        stateTime = 0;
        input = new playerInput(this);
        stats = new playerStats();
        setStats(stats);
        state = PlayerState.IDLE;

        anims = animManager.getPlayerAnimations();
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
        if (getInput().getInputDirection() == 1 && vel.x < maxVelocity) {
            getBody().applyLinearImpulse(speed, 0, pos.x, pos.y, true);
        } else if (getInput().getInputDirection() == -1 && vel.x > -maxVelocity) {
            getBody().applyLinearImpulse(-speed, 0, pos.x, pos.y, true);
        } else if (getInput().getInputDirection() == 0 && vel.x != 0) {
            getBody().applyLinearImpulse(-vel.x / 20, 0, pos.x, pos.y, true);
        }
        if (isGrounded() && Math.abs(vel.x) > 1 && getInput().getInputDirection() == 0) {
            getBody().applyLinearImpulse(-vel.x / 8, 0, pos.x, pos.y, true);
        }
        if (getInput().jump) input.setJump(false);
    }

    @Override
    public void act(float delta) {
        if (getStats().getHealth() <= 0) {
            death();
            return;
        }
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
        if (getBody().getFixtureList().indexOf(entityFixture, true) == 1) { // Jump sensor
            getInput().numJumps = getStats().getMaxJumps();
        } else if (getBody().getFixtureList().indexOf(entityFixture, true) == 0) { // Player collision
            if (contactFixture.getUserData() instanceof Enemy) {
                Enemy e = (Enemy) contactFixture.getUserData();
                if (e.getState() != Enemy.EnemyState.DEATH && e.stats.hitTimer <= 0) {
                    damage(1, e.getBody().getPosition());
                }
            } else if (contactFixture.getUserData() instanceof Loot) {
                Loot loot = (Loot) contactFixture.getUserData();
                if (!loot.isLooted()) {
                    loot(loot);
                    loot.setLooted();
                }
            }
        } else if (getBody().getFixtureList().indexOf(entityFixture, true) == 2) { // Item pickup sensor
            Loot item = (Loot) contactFixture.getUserData();
            item.moveToPlayer(getBody().getPosition());
        }
    }

    @Override
    public void endContact(Fixture entityFixture, Fixture contactFixture) {
        super.endContact(entityFixture, contactFixture);
        if (getBody().getFixtureList().indexOf(entityFixture, true) == 2) {
            Loot item = (Loot) contactFixture.getUserData();
            item.stopMove();
        }
    }

    @Override
    public void draw(SpriteBatch batch, float delta) {
        stateTime = stateTime + delta;
        if (anims.get(getState()).isAnimationFinished(getStateTime())) {
            if (getState() == PlayerState.DEATH) {
                death();
                return;
            } else if (bufferedState != null) {
                state = bufferedState;
                bufferedState = null;
                stateTime = 0;
            }
        }
        TextureRegion frame = anims.get(getState()).getKeyFrame(getStateTime());
        batch.draw(frame,
            this.getBody().getPosition().x - getWidth() / 2f,
            this.getBody().getPosition().y - getHeight() / 2f,
            getWidth() / 2f, getHeight() / 2f, getWidth(), getHeight(), getDirection(), 1, 0);
    }

    @Override
    public void damage(int attackDamage, Vector2 attackOrigin) {
        if (super.damage(attackDamage, attackOrigin, 20)) {
            stats.setInvulnerability(1);
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
                new Claw(3, 3, new Vector2(1.5f * getDirection(), 2), getDirection(), animManager, getBody());
                break;
            case ATTACKFORWARD:
                new Claw(3, 3, new Vector2(2 * getDirection(), 0), getDirection(), animManager, getBody());
                break;
            case ATTACKDOWN:
                new Claw(3, 3, new Vector2(1.5f * getDirection(), -2), getDirection(), animManager, getBody());
                break;
        }
    }

    public void loot(Loot item) {
        if (item.type == Loot.LootType.CRYSTAL) {
            stats.addCrystals(item.value);
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

        Projectile fireball = new Fireball(getBody().getPosition().x + getDirection() * 2, getBody().getPosition().y - 0.5f,
            1.5f, 1.5f, getDirection(), animManager, true, getBody().getWorld());
        Vector2 impulse = new Vector2();
        impulse.x = stats.projectileSpeed * getDirection();
        if (input.upMove) impulse.y = stats.projectileSpeed * 0.5f;
        else if (input.downMove) impulse.y = -stats.projectileSpeed * 0.5f;
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
        ATTACKDOWN,
        DEATH
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

        public int getInputDirection() {
            if (!leftMove && !rightMove) return 0;
            return player.getDirection();
        }

        public void setLeftMove(boolean leftMove) {
            this.leftMove = leftMove;
            if (leftMove) player.setDirection(-1);
            else if (rightMove) player.setDirection(1);
        }

        public void setRightMove(boolean rightMove) {
            this.rightMove = rightMove;
            if (rightMove) player.setDirection(1);
            else if (leftMove) player.setDirection(-1);
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

    public static class playerStats extends CreatureStats {
        private int maxJumps;
        private float projectileMaxCD = 0.7f;
        private float projectileCD;
        public float projectileSpeed = 25;
        public float jumpForce = 30;
        private int crystals;

        public playerStats() {
            super(10);
            setMaxJumps(4);
            setProjectileMaxCD(0.7f);
            setProjectileCD(getProjectileMaxCD());
            crystals = 0;
        }

        @Override
        public void updateCooldowns(float delta) {
            super.updateCooldowns(delta);
            if (getProjectileCD() > 0) setProjectileCD(getProjectileCD() - delta);
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

        public void addCrystals(int newCrystals) {
            this.crystals += newCrystals;
        }

        public int getCrystals() {
            return this.crystals;
        }

    }
}
