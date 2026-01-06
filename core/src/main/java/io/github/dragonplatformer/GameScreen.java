package io.github.dragonplatformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.dragonplatformer.Entity.*;
import io.github.dragonplatformer.Entity.Creature.Bat;
import io.github.dragonplatformer.Entity.Creature.Lizard;
import io.github.dragonplatformer.Entity.Creature.Player;


public class GameScreen implements Screen {
    private final Main game;

    private final Stage uiStage;
    private final World world;
    private final Box2DDebugRenderer debugRenderer;
    private final Player player;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer tiledMapRenderer;
    private final Label debugInfo;
    private final Array<Body> bodies;
    private final AnimationManager animManager;
    private boolean debug;

    public GameScreen(final Main game, final TiledMap map) {
        this.game = game;
        TextureAtlas atlas = game.manager.get("images/pack.atlas");
        animManager = new AnimationManager(atlas);
        bodies = new Array<>();
        world = new World(new Vector2(0, -9.8f), true);
        debug = false;
        if (game.batch == null) { // Debug setup if rendering won't work
            uiStage = null;
            debugRenderer = null;
            this.map = map;
            tiledMapRenderer = null;
            debugInfo = null;
            player = new Player(0, 0, 3, 3, world, this, animManager);
            return;
        }
        uiStage = new Stage(new ScreenViewport());
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
                new Portal(x, y, width, height, world, (String) properties.get("stageexit"), this);
            }
        }
        player = new Player(playerx, playery, 3, 3, world, this, animManager);
        loadTilemapData();

        Label.LabelStyle style = new Label.LabelStyle();
        style.font = new BitmapFont();
        debugInfo = new Label("Player State: null", style);
        uiStage.addActor(debugInfo);

        world.setContactListener(new GameContactListener());
        GameInputProcessor gameInputProcessor = new GameInputProcessor(player, this);
        Gdx.input.setInputProcessor(gameInputProcessor);

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
                    case "lizard":
                        new Lizard(posx, posy, 2, 2, world, animManager);
                        break;
                    case "bat":
                        new Bat(posx, posy, 1.5f, 1.5f, world, animManager);
                        break;
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
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        world.getBodies(bodies);
        for (int index = 0; index < bodies.size; index++) {
            if (bodies.get(index).getUserData() instanceof Entity) {
                Entity e = (Entity) bodies.get(index).getUserData();
                e.act(delta);
            }
        }
        updateCamera();
        draw();

        debugRenderer.render(world, game.viewport.getCamera().combined);
        world.step(1/60f, 6, 2);
    }

    public void changeLevel(String level) {
        this.pause();
        game.loadNewLevel(level);
    }

    public void gameOver() {
        changeLevel("hub");
    }

    private void updateCamera() {
        game.viewport.getCamera().position.x = MathUtils.clamp(
            player.getBody().getPosition().x,
            game.viewport.getWorldWidth() / 2f,
            map.getProperties().get("width", Integer.class) - game.viewport.getWorldWidth() / 2f
        );
        game.viewport.getCamera().position.y = MathUtils.clamp(
            player.getBody().getPosition().y,
            game.viewport.getWorldHeight() / 2f,
            map.getProperties().get("height", Integer.class) - game.viewport.getWorldHeight() / 2f
        );
    }

    private void draw() {
        tiledMapRenderer.setView((OrthographicCamera) game.viewport.getCamera());
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);

        tiledMapRenderer.render();

        world.getBodies(bodies);
        game.batch.begin();
        for (Body body : bodies) {
            for (Fixture fixture : body.getFixtureList()) {
                if (fixture.getUserData() != body.getUserData() && fixture.getUserData() != null) {
                    ((Entity) fixture.getUserData()).draw(game.batch, Gdx.graphics.getDeltaTime());
                }
            }
            if (body.getUserData() != null) {
                Entity e = (Entity) body.getUserData();
                e.draw(game.batch, Gdx.graphics.getDeltaTime());
            }
        }
        game.batch.end();

        uiStage.act(Gdx.graphics.getDeltaTime());
        debugInfo.setText(String.format(
            "%n%nPlayer State: %s%n" +
                "State Time: %.2f%n" +
                "Jumps: %d%n" +
                "Health: %d%n" +
                "Crystals: %d",
            player.getState(), player.getStateTime(), player.getInput().numJumps,
            player.getStats().getHealth(), player.getStats().getCrystals()
            ));
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (game.batch == null) return;
        game.viewport.update(width, height);
        uiStage.getViewport().update(width, height, true);
        debugInfo.setPosition(10, height - debugInfo.getHeight() - 20);
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
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
        if (debug) debugRenderer.setDrawBodies(!debugRenderer.isDrawBodies());
    }

    public Player getPlayer() {
        return player;
    }
}
