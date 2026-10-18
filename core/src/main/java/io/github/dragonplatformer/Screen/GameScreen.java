package io.github.dragonplatformer.Screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.dragonplatformer.Entity.*;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.AttackEffect;
import io.github.dragonplatformer.Entity.Actor.*;
import io.github.dragonplatformer.Entity.Actor.Enemy.*;
import io.github.dragonplatformer.Entity.Actor.Player.Player;
import io.github.dragonplatformer.Entity.Effect.Portal.Portal;
import io.github.dragonplatformer.Main;

import java.util.ArrayList;


public class GameScreen implements Screen {
    private final Main game;
    private final GameInputProcessor gameInputProcessor;
    private Stage uiStage;
    private Table uiTable;
    private Label debugInfo;
    private final Skin skin;

    private final World world;
    private final Box2DDebugRenderer debugRenderer;
    private final Player player;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer tiledMapRenderer;
    private final Array<Body> bodies = new Array<>();
    private final Array<AttackEffect> effects = new Array<>();
    private final Array<Actor<?>> hitBodies = new Array<>();
    private final Array<Actor<?>> invulBodies = new Array<>();
    private final Array<Actor<?>> counterBodies = new Array<>();
    private final Array<Actor<?>> slowBodies = new Array<>();
    private final AnimationManager animManager;
    private final EffectManager effectManager;
    private final ShaderProgram hitShader;
    private final ShaderProgram invulnerableShader;
    private final ShaderProgram counterShader;
    private final ShaderProgram slowedShader;
//    private float shaderTimer = 0;
    private boolean debug = false;
    private final ArrayList<CameraArea> cameraAreas = new ArrayList<>();
    private int currentCameraArea = -1;
    private CameraArea cameraInterpolationArea = null;
    private float cameraInterpolationTime = 0;
    private float gameTime = 0;

    // Physics Rendering
    private float timeAccumulator = 0;
    private float timeStep = 1f/60;

    public GameScreen(final Main game, final TiledMap map) {
        this.game = game;
        TextureAtlas atlas = game.manager.get("images/pack.atlas");
        skin = new Skin(atlas);
        animManager = new AnimationManager(atlas);
        effectManager = new EffectManager(atlas);
        world = new World(new Vector2(0, -9.8f), true);
        if (game.batch == null) { // for headless application tests
            this.map = map;
            tiledMapRenderer = null;
            debugRenderer = null;
            gameInputProcessor = null;
            player = new Player(0, 0, game.playerStats, world, this, effectManager, animManager);
            hitShader = null;
            invulnerableShader = null;
            counterShader = null;
            slowedShader = null;
            return;
        }

        hitShader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"),
            Gdx.files.internal("shaders/white.frag")
        );

        invulnerableShader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"),
            Gdx.files.internal("shaders/light.frag")
        );

        counterShader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"),
            Gdx.files.internal("shaders/bluetint.frag")
        );

        slowedShader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"),
            Gdx.files.internal("shaders/slowed.frag")
        );

        uiStage = new Stage(new ExtendViewport(game.viewport.getMinWorldWidth() * 32, game.viewport.getMinWorldHeight() * 32,
            game.viewport.getMaxWorldWidth() * 32, game.viewport.getMaxWorldHeight() * 32));
        debugRenderer = new Box2DDebugRenderer();
        debugRenderer.setDrawBodies(false);

        // Tilemap loading
        this.map = map;
        tiledMapRenderer = new OrthogonalTiledMapRenderer(this.map, 1 / 32f);
        tiledMapRenderer.setView((OrthographicCamera) game.viewport.getCamera());

        float playerx = 1;
        float playery = 1;
        MapObjects flags = this.map.getLayers().get("Flags").getObjects();
        for (int i = 0; i < flags.getCount(); i++) {
            if (flags.get(i).getProperties().containsKey("stageenter")) {
                playerx = (float) flags.get(i).getProperties().get("x") * tiledMapRenderer.getUnitScale();
                playery = (float) flags.get(i).getProperties().get("y") * tiledMapRenderer.getUnitScale();
            }
            if (flags.get(i).getProperties().containsKey("stageexit")) {
                MapProperties properties = flags.get(i).getProperties();
                float width = (Float) properties.get("width") * tiledMapRenderer.getUnitScale();
                float height = (Float) properties.get("height") * tiledMapRenderer.getUnitScale();
                float x = (Float) properties.get("x") * tiledMapRenderer.getUnitScale() + width / 2;
                float y = (Float) properties.get("y") * tiledMapRenderer.getUnitScale() + height / 2;
                new Portal(x, y, width, height, effectManager, animManager, world, (String) properties.get("stageexit"), this);
            }
        }
        player = new Player(playerx, playery, game.playerStats, world, this, effectManager, animManager);

        loadTilemapData();
        loadUI();

        world.setContactListener(new GameContactListener());
        gameInputProcessor = new GameInputProcessor(player, this);
        Gdx.input.setInputProcessor(gameInputProcessor);
        Gdx.input.setCursorCatched(true);

    }

    private void loadTilemapData() {
        if (map.getLayers().get("Enemies") != null) {
            MapObjects enemies = this.map.getLayers().get("Enemies").getObjects();
            for (int i = 0; i < enemies.getCount(); i++) {
                MapObject enemy = enemies.get(i);
                float posx = (float) enemy.getProperties().get("x") * tiledMapRenderer.getUnitScale();
                float posy = (float) enemy.getProperties().get("y") * tiledMapRenderer.getUnitScale();
                String type = (String) enemy.getProperties().get("type");
                switch (type) {
                    case "wyvern":
                        new Wyvern(posx, posy, world, effectManager, animManager);
                        break;
                    case "gargoyle":
                        new Gargoyle(posx, posy, world, effectManager, animManager);
                        break;
                    case "lizard":
                        new Lizard(posx, posy, world, effectManager, animManager);
                        break;
                    case "bat":
                        new Bat(posx, posy, world, effectManager, animManager);
                        break;
                    case "spikylizard":
                        new SpikyLizard(posx, posy, world, effectManager, animManager);
                        break;
                    case "manticore":
                        new Manticore(posx, posy, world, effectManager, animManager);
                }
            }
        }
        MapObjects collisionObjects = this.map.getLayers().get("Collision").getObjects();
        for (int i = 0; i < collisionObjects.getCount(); i++) {
            MapObject object = collisionObjects.get(i);
            BodyDef objBodyDef = new BodyDef();
            objBodyDef.type = BodyDef.BodyType.StaticBody;
            Filter filter = new Filter();
            filter.categoryBits = GameContactListener.FilterBits.STATIC.getBit();
            float scale = tiledMapRenderer.getUnitScale();
            if (object instanceof RectangleMapObject) {
                Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
                objBodyDef.position.set((rectangle.x + rectangle.width / 2) * scale, (rectangle.y + rectangle.height / 2) * scale);
                Body recBody = world.createBody(objBodyDef);
                PolygonShape recBox = new PolygonShape();
                recBox.setAsBox(rectangle.width / 2 * scale, rectangle.height / 2 * scale);
                recBody.createFixture(recBox, 0.0f).setFilterData(filter);
                recBox.dispose();
            } else if (object instanceof PolygonMapObject) {
                Polygon polygon = ((PolygonMapObject) object).getPolygon();
                objBodyDef.position.set(polygon.getX() * scale, polygon.getY() * scale);
                Body polygonBody = world.createBody(objBodyDef);
                PolygonShape polygonShape = new PolygonShape();
                float[] vertices = new float[polygon.getVertexCount() * 2];
                for (int v = 0; v < vertices.length; v++) {
                    vertices[v] = polygon.getVertices()[v] * scale;
                }
                polygonShape.set(vertices);
                polygonBody.createFixture(polygonShape, 0.0f).setFilterData(filter);
                polygonShape.dispose();
            } else if (object instanceof EllipseMapObject) {
                Ellipse ellipse = ((EllipseMapObject) object).getEllipse();
                objBodyDef.position.set((ellipse.x + ellipse.width / 2) * scale, (ellipse.y + ellipse.height / 2) * scale);
                Body circleBody = world.createBody(objBodyDef);
                CircleShape circleShape = new CircleShape();
                circleShape.setRadius(ellipse.width / 2 * scale);
                circleBody.createFixture(circleShape, 0.0f).setFilterData(filter);
                circleShape.dispose();
            }
        }
        if (map.getLayers().get("Camera") != null) {
            MapObjects cameraViews = this.map.getLayers().get("Camera").getObjects();
            for (int i = 0; i < cameraViews.getCount(); i++) {
                MapObject cameraView = cameraViews.get(i);
                cameraAreas.add(new CameraArea(
                    (float) cameraView.getProperties().get("x") * tiledMapRenderer.getUnitScale(),
                    (float) cameraView.getProperties().get("y") * tiledMapRenderer.getUnitScale(),
                    (float) cameraView.getProperties().get("width") * tiledMapRenderer.getUnitScale(),
                    (float) cameraView.getProperties().get("height") * tiledMapRenderer.getUnitScale()
                ));
            }
        }
    }

    public void loadUI() {
        uiTable = new Table();
        uiTable.setFillParent(true);
        uiStage.addActor(uiTable);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        skin.add("default", new BitmapFont());
        labelStyle.font = skin.getFont("default");
        skin.add("default", labelStyle);

        uiTable.top().left().pad(Value.percentWidth(0.01f)).defaults().left();

        Image statusWingFull = new Image(skin.getDrawable("healthbar_wing_full"));
        Image statusWingDamaged = new Image(skin.getDrawable("healthbar_wing_damaged"));
        Image statusWingLow = new Image(skin.getDrawable("healthbar_wing_low"));

        Stack statusWing = new Stack() {
            @Override
            public void act(float delta) {
                super.act(delta);
                float healthPercent = getPlayer().stats().getHealth() / getPlayer().stats().getMaxHealth();
                if (healthPercent > 0.5f) {
                    if (!statusWingFull.isVisible()) statusWingFull.setVisible(true);
                    if (statusWingDamaged.isVisible()) statusWingDamaged.setVisible(false);
                    if (statusWingLow.isVisible()) statusWingLow.setVisible(false);
                } else if (healthPercent > 0.25f) {
                    if (statusWingFull.isVisible()) statusWingFull.setVisible(false);
                    if (!statusWingDamaged.isVisible()) statusWingDamaged.setVisible(true);
                    if (statusWingLow.isVisible()) statusWingLow.setVisible(false);
                } else {
                    if (statusWingFull.isVisible()) statusWingFull.setVisible(false);
                    if (statusWingDamaged.isVisible()) statusWingDamaged.setVisible(false);
                    if (!statusWingLow.isVisible()) statusWingLow.setVisible(true);
                }
            }
        };
        statusWing.add(statusWingLow);
        statusWing.add(statusWingDamaged);
        statusWing.add(statusWingFull);

        Table statusBars = new Table();

        Image healthFrame = new Image(skin.getDrawable("healthbar_fire_frame"));
        Image healthFill = new Image(skin.getDrawable("healthbar_fire_fill"));
        ClippingProgressBar healthBar = new ClippingProgressBar(
            getPlayer().stats().getHealth(), getPlayer().stats().maxHealth, healthFrame, healthFill
        ) {
            @Override
            public void act(float delta) {
                super.act(delta);
                setValue(getPlayer().stats().health);
                setMaxValue(getPlayer().stats().maxHealth);
            }
        };

        Image crystalFrame = new Image(skin.getDrawable("crystalbar_frame"));
        Image crystalFill = new Image(skin.getDrawable("crystalbar_fill"));
        ClippingProgressBar crystalBar = new ClippingProgressBar(
            10, getPlayer().stats().getCrystals(), crystalFrame, crystalFill
        ){
            @Override
            public void act(float delta) {
                super.act(delta);
                setValue(getPlayer().stats().getCrystals());
            }
        };

        statusBars.defaults().left();

        float healthRelWidth = 0.65f;
        statusBars.add(healthBar).size(Value.percentWidth(healthRelWidth, statusBars), Value.percentWidth(
            healthFrame.getHeight() / healthFrame.getWidth() * healthRelWidth, statusBars
        )).expandX();
        statusBars.row();
        float crystalRelWidth = 0.45f;
        statusBars.add(crystalBar).size(Value.percentWidth(crystalRelWidth, statusBars), Value.percentWidth(
            crystalFrame.getHeight() / crystalFrame.getWidth() * crystalRelWidth, statusBars
        ));

        Table statusIndicator = new Table();
        statusIndicator.row().height(Value.percentHeight(1, statusIndicator));
        statusIndicator.add(statusWing).width(Value.percentHeight(1, statusIndicator));
        statusIndicator.add(statusBars).minWidth(Value.percentWidth(0.8f, statusIndicator)).expandX().left();

        uiTable.add(statusIndicator).height(Value.percentWidth(0.1f, uiTable)).width(Value.percentWidth(0.98f, uiTable));
        uiTable.row();
        debugInfo = new Label("Debug: loading info", skin) {
            @Override
            public void act(float delta) {
                super.act(delta);
                debugInfo.setText(String.format(
                    "Game Time: %.2f%n" +
                        "Player State: %s%n" +
                        "State Time: %.2f%n" +
                        "Health: %.1f%n" +
                        "Crystals: %d%n" +
                        "Glide Charge: %.2f%n" +
                        "Soar Charge: %.2f%n" +
                        "Glide: %s%n" +
                        "Evade Window: %s%n" +
                        "Intangible: %s%n" +
                        "VelX: %.2f%n" +
                        "VelY: %.2f%n",
                    gameTime, player.getState(), player.getStateTime(),
                    player.stats().getHealth(), player.stats().getCrystals(), player.stats().getGlideCharge(),
                    player.stats().getSoarCharge(), player.input().glide, player.stats().isEvading(), player.stats().isIntangible(),
                    player.getBody().getLinearVelocity().x, player.getBody().getLinearVelocity().y
                ));
            }

            @Override
            public void draw(Batch batch, float parentAlpha) {
                if (debug) super.draw(batch, parentAlpha);
            }
        };
        uiTable.add(debugInfo);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(gameInputProcessor);
        Gdx.input.setCursorCatched(true);
    }

    public Viewport getViewport() {
        return game.viewport;
    }

    @Override
    public void render(float delta) {
        int cursorRadius = game.viewport.getScreenWidth() / 10;
        int screenCenterX = game.viewport.getScreenWidth() / 2;
        int screenCenterY = game.viewport.getScreenHeight() / 2;
        Gdx.input.setCursorPosition(
            MathUtils.clamp(Gdx.input.getX(), screenCenterX - cursorRadius, screenCenterX + cursorRadius),
            MathUtils.clamp(Gdx.input.getY(), screenCenterY - cursorRadius, screenCenterY + cursorRadius)
        );

        gameTime += delta;
        world.getBodies(bodies);
        for (int index = 0; index < bodies.size; index++) {
            if (bodies.get(index).getUserData() instanceof Entity) {
                Entity<?> e = (Entity<?>) bodies.get(index).getUserData();
                if (!e.isHitStunned()) e.act(delta);
                else e.updateHitStun(delta);
            }
            for (Fixture fixture : bodies.get(index).getFixtureList()) {
                if (fixture.getUserData() != bodies.get(index).getUserData() && fixture.getUserData() != null) {
                    Entity<?> e = ((Entity<?>) fixture.getUserData());
                    if (!e.isHitStunned()) e.act(delta);
                }
            }
        }

        if (cameraInterpolationTime > 0) cameraInterpolationTime -= delta;
        updateCamera();
        uiStage.act(delta);
        draw(delta);
        debugRenderer.render(world, game.viewport.getCamera().combined);
        // TODO: test for different frame rates!
        timeAccumulator += delta;
        while (timeAccumulator > timeStep) {
            world.step(timeStep, 6, 2);
            timeAccumulator -= timeStep;
        }
    }

    public void changeLevel(String level) {
        this.pause();
        game.loadNewLevel(level);
    }

    public void gameOver() {
        game.returnToHub();
    }

    public void openStats() {
        game.openStats(this);
    }

    private void updateCamera() {
        OrthographicCamera camera = (OrthographicCamera) game.viewport.getCamera();
        float viewportWidth = game.viewport.getMinWorldWidth();
        float viewportHeight = game.viewport.getMinWorldHeight();
        float interpolationDuration = 1;
        if (currentCameraArea != -1 &&
            !cameraAreas.get(currentCameraArea).contains(player.getBody().getPosition())) {
            cameraInterpolationArea = new CameraArea(
                camera.position.x - viewportWidth * camera.zoom / 2,
                camera.position.y - viewportHeight * camera.zoom / 2,
                viewportWidth * camera.zoom,
                viewportHeight * camera.zoom
            );
            cameraInterpolationTime = interpolationDuration;
            currentCameraArea = -1;
        }
        int startAreaSearch = currentCameraArea != -1 ? currentCameraArea + 1 : 0;
        for (int i = startAreaSearch; i < cameraAreas.size(); i++) {
            if (cameraAreas.get(i).contains(player.getBody().getPosition())) {
                if (cameraInterpolationArea != null) cameraInterpolationTime = interpolationDuration;
                cameraInterpolationArea = new CameraArea(
                    camera.position.x - viewportWidth * camera.zoom / 2,
                    camera.position.y - viewportHeight * camera.zoom / 2,
                    viewportWidth * camera.zoom,
                    viewportHeight * camera.zoom
                );
                currentCameraArea = i;
                break;
            }
        }
        float maxZoomOut = 1.2f;
        float minX = viewportWidth / 2f;
        float minY = viewportHeight / 2f;
        float maxX = map.getProperties().get("width", Integer.class) - viewportWidth / 2f;
        float maxY = map.getProperties().get("height", Integer.class) - viewportHeight / 2f;
        camera.zoom = 1;
        if (currentCameraArea != -1) {
            CameraArea cameraArea = cameraAreas.get(currentCameraArea);
            camera.zoom = Math.min(maxZoomOut, cameraArea.getZoom(viewportWidth, viewportHeight));
            minX = cameraArea.getPosition().x - cameraArea.getWidth() / 2 + viewportWidth / 2 * camera.zoom;
            minY = cameraArea.getPosition().y - cameraArea.getHeight() / 2 + viewportHeight / 2 * camera.zoom;
            maxX = cameraArea.getPosition().x + cameraArea.getWidth() / 2 - viewportWidth / 2 * camera.zoom;
            maxY = cameraArea.getPosition().y + cameraArea.getHeight() / 2 - viewportHeight / 2 * camera.zoom;
            if (maxX < minX) {
                maxX = (maxX + minX) / 2;
                minX = maxX;
            }
            if (maxY < minY) {
                maxY = (maxY + minY) / 2;
                minY = maxY;
            }
        }
        camera.position.set(
            MathUtils.clamp(player.getBody().getPosition().x, minX, maxX),
            MathUtils.clamp(player.getBody().getPosition().y, minY, maxY),
            0
        );
        if (cameraInterpolationTime > 0) {
            float progress = 1 - cameraInterpolationTime / interpolationDuration;
            camera.position.set(new Vector3(camera.position).scl(progress).add(cameraInterpolationArea.getPosition().scl(1 - progress)));
            camera.zoom = progress * camera.zoom + (1 - progress) * cameraInterpolationArea.getZoom(viewportWidth, viewportHeight);
        }
    }

    private void draw(float delta) {
        tiledMapRenderer.setView((OrthographicCamera) game.viewport.getCamera());
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);

        tiledMapRenderer.render();

        world.getBodies(bodies);
        effects.clear();
        hitBodies.clear();
        invulBodies.clear();
        counterBodies.clear();
        slowBodies.clear();
        game.batch.begin();
        for (Body body : bodies) {
            for (Fixture fixture : body.getFixtureList()) {
                if (fixture.getUserData() != body.getUserData() && fixture.getUserData() != null) {
                    if (fixture.getUserData() instanceof AttackEffect) effects.add((AttackEffect) fixture.getUserData());
                }
            }
            if (body.getUserData() != null) {
                Entity<?> e = (Entity<?>) body.getUserData();
                if (e instanceof AttackEffect) effects.add((AttackEffect) e);
                else if (e instanceof Actor) {
                    Actor<?> c = (Actor<?>) e;
                    if (c instanceof Player && ((Player) e).stats().isEvading()) counterBodies.add(c);
                    else if (c.getHitFlash()) hitBodies.add(c);
                    else if (c.stats().isInvulnerable()) invulBodies.add(c);
                    else if (c.stats().isSlowed()) slowBodies.add(c);
                    else c.draw(game.batch, delta);
                }
                else e.draw(game.batch, delta);
            }
        }
        game.batch.setShader(hitShader);
        for (Actor<?> actor : hitBodies) {
            if (actor.getHitEffectTimer() <= 0.1f) actor.draw(game.batch, delta);
        }
        game.batch.setShader(null);
        game.batch.setColor(Color.BLACK);
        for (Actor<?> actor : hitBodies) {
            if (actor.getHitEffectTimer() > 0.1f) actor.draw(game.batch, delta);
        }
        game.batch.setColor(Color.WHITE);
        game.batch.setShader(slowedShader);
        for (Actor<?> actor : slowBodies) {
            actor.draw(game.batch, delta);
        }

        game.batch.setShader(counterShader);
        for (Actor<?> actor : counterBodies) {
            actor.draw(game.batch, delta);
        }
        game.batch.setShader(invulnerableShader);
        for (Actor<?> actor : invulBodies) {
            actor.draw(game.batch, delta);
        }
        game.batch.setShader(null);
        for (AttackEffect effect : effects) {
            effect.draw(game.batch, delta);
        }
        effectManager.draw(game.batch, delta);
        game.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        game.batch.end();

        if (debug) {
            for (Body body : bodies) {
                for (Fixture fixture : body.getFixtureList()) {
                    if (fixture.getUserData() != body.getUserData() && fixture.getUserData() != null) {
                        ((Entity<?>) fixture.getUserData()).debugDraw(game.viewport.getCamera().combined);
                    }
                }
                if (body.getUserData() != null) {
                    ((Entity<?>) body.getUserData()).debugDraw(game.viewport.getCamera().combined);
                }
            }
        }
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (game.batch == null) return;
        game.viewport.update(width, height);
        uiStage.getViewport().update(width, height, true);
        tiledMapRenderer.setView((OrthographicCamera) game.viewport.getCamera());
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        uiStage.dispose();
        effectManager.endAll();
    }

    public void toggleDebug() {
        debug = !debug;
        debugRenderer.setDrawBodies(debug);
        uiTable.setDebug(debug, true);
    }

    public Player getPlayer() {
        return player;
    }

    public Camera getCamera() {
        return game.viewport.getCamera();
    }
}
