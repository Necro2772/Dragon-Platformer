package io.github.dragonplatformer;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener {
    private SpriteBatch batch;
    private ExtendViewport viewport;
    private Texture image;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Player player;
    private float jumpCD;

    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new ExtendViewport(20, 12);
        image = new Texture("libgdx.png");
        world = new World(new Vector2(0, -8), true);
        debugRenderer = new Box2DDebugRenderer();
        player = new Player(new Texture("dragon.png"), 2, 2, world);

        world.setContactListener(new GameContactListener());
        GameInputProcessor gameInputProcessor = new GameInputProcessor(player);
        Gdx.input.setInputProcessor(gameInputProcessor);

        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(new Vector2(0, -10));
        Body groundBody = world.createBody(groundBodyDef);
        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox(40, 10.0f);
        groundBody.createFixture(groundBox, 0.0f);
        groundBox.dispose();

        BodyDef ceilingBodyDef = new BodyDef();
        ceilingBodyDef.position.set(new Vector2(0, 25));
        Body ceilingBody = world.createBody(ceilingBodyDef);
        PolygonShape ceilingBox = new PolygonShape();
        ceilingBox.setAsBox(40, 10.0f);
        ceilingBody.createFixture(ceilingBox, 0.0f);
        ceilingBox.dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
        debugRenderer.render(world, viewport.getCamera().combined);
        world.step(1/60f, 6, 2);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }

    private void input() {
        float maxVelocity = 5f;
        Vector2 vel = player.body.getLinearVelocity();
        Vector2 pos = player.body.getPosition();
        float delta = Gdx.graphics.getDeltaTime();
        jumpCD -= delta;

        if (player.glide) {
            maxVelocity = 7f;
            if (player.rightMove && vel.x < maxVelocity) {
                player.body.applyLinearImpulse(0.6f, 0, pos.x, pos.y, true);
            } else if (player.leftMove && vel.x > -maxVelocity) {
                player.body.applyLinearImpulse(-0.6f, 0, pos.x, pos.y, true);
            } else if (!player.leftMove && !player.rightMove && vel.x != 0) {
                player.body.applyLinearImpulse(-vel.x / 20, 0, pos.x, pos.y, true);
            }
        } else {
            if (player.rightMove && vel.x < maxVelocity) {
                player.body.applyLinearImpulse(0.8f, 0, pos.x, pos.y, true);
            } else if (player.leftMove && vel.x > -maxVelocity) {
                player.body.applyLinearImpulse(-0.8f, 0, pos.x, pos.y, true);
            } else if (!player.leftMove && !player.rightMove && vel.x != 0) {
                player.body.applyLinearImpulse(-vel.x / 20, 0, pos.x, pos.y, true);
            }
            if (player.jump && jumpCD <= 0 && player.numJumps > 0) {
                player.body.applyLinearImpulse(0, 15f - vel.y, pos.x, pos.y, true);
                player.numJumps--;
                player.setJump(false);
                jumpCD = 0.5f;
            } else if (player.jump && (jumpCD > 0.3f || player.numJumps <= 0)) player.setJump(false);
        }
    }

    private void logic() {

    }

    private void draw() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.draw(image, 2, 2, 4, 2);
        player.sprite.setCenter(player.body.getPosition().x, player.body.getPosition().y);
        player.sprite.draw(batch);
        batch.end();
    }
}
