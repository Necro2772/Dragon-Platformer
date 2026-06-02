package io.github.dragonplatformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.dragonplatformer.Entity.*;
import io.github.dragonplatformer.Entity.Effect.AttackEffect.AttackEffect;
import io.github.dragonplatformer.Entity.Actor.*;
import io.github.dragonplatformer.Entity.Actor.Enemy.*;
import io.github.dragonplatformer.Entity.Actor.Player.Player;
import io.github.dragonplatformer.Entity.Effect.Portal.Portal;

import java.util.ArrayList;


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
    private final Array<AttackEffect> effects;
    private final Array<Actor<?>> hitBodies;
    private final Array<Actor<?>> invulBodies;
    private final Array<Actor<?>> counterBodies;
    private final AnimationManager animManager;
    private final ShaderProgram hitShader;
    private final ShaderProgram invulnerableShader;
    private final ShaderProgram counterShader;
//    private float shaderTimer;
    private boolean debug;
    private final ArrayList<CameraArea> cameraAreas;
    private int currentCameraArea;
    private CameraArea cameraInterpolationArea;
    private float cameraInterpolationTime;

    public GameScreen(final Main game, final TiledMap map) {
        this.game = game;
        TextureAtlas atlas = game.manager.get("images/pack.atlas");
        animManager = new AnimationManager(atlas);
        bodies = new Array<>();
        hitBodies = new Array<>();
        invulBodies = new Array<>();
        counterBodies = new Array<>();
        effects = new Array<>();
        world = new World(new Vector2(0, -9.8f), true);
        debug = false;
//        shaderTimer = 0;
        cameraAreas = new ArrayList<>();
        cameraInterpolationArea = null;
        cameraInterpolationTime = 0;
        currentCameraArea = -1;
        if (game.batch == null) { // Debug setup if rendering won't work
            uiStage = null;
            debugRenderer = null;
            this.map = map;
            tiledMapRenderer = null;
            debugInfo = null;
            player = new Player(0, 0, world, this, animManager);
            hitShader = null;
            invulnerableShader = null;
            counterShader = null;
            return;
        }

        hitShader = new ShaderProgram(
            "attribute highp vec4 a_position;\n" +
            "attribute highp vec4 a_color;\n" +
            "attribute highp vec2 a_texCoord0;\n" +
            "uniform mat4 u_projTrans;\n" +
            "varying highp vec4 v_color;\n" +
            "varying highp vec2 v_texCoords;\n" +
            "void main() {\n" +
                "v_color = a_color;\n" +
                "v_texCoords = a_texCoord0;\n" +
                "gl_Position = u_projTrans * a_position;\n" +
            "}",
            "varying highp vec4 v_color;\n" +
            "varying highp vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture;\n" +
            "void main() {\n" +
                "gl_FragColor = vec4(0.0);\n" +
                "highp vec4 color = texture2D(u_texture, v_texCoords);\n" +
                "if (color.a > 0.0) {\n" +
                    "gl_FragColor = vec4(1, 1, 1, 1);\n" +
                "}\n" +
            "}"
        );

        invulnerableShader = new ShaderProgram(
            "attribute highp vec4 a_position;\n" +
                "attribute highp vec4 a_color;\n" +
                "attribute highp vec2 a_texCoord0;\n" +
                "uniform mat4 u_projTrans;\n" +
                "varying highp vec4 v_color;\n" +
                "varying highp vec2 v_texCoords;\n" +
                "void main() {\n" +
                "v_color = a_color;\n" +
                "v_texCoords = a_texCoord0;\n" +
                "gl_Position = u_projTrans * a_position;\n" +
                "}",
            "varying highp vec4 v_color;\n" +
                "varying highp vec2 v_texCoords;\n" +
                "uniform sampler2D u_texture;\n" +
                "void main() {\n" +
                "gl_FragColor = vec4(0.0);\n" +
                "highp vec4 color = texture2D(u_texture, v_texCoords);\n" +
                "if (color.a > 0.0) {\n" +
                "gl_FragColor = vec4(color.r + 0.2, color.g + 0.2, color.b + 0.2, color.a);\n" +
                "}\n" +
                "}"
        );

        counterShader = new ShaderProgram(
            "attribute highp vec4 a_position;\n" +
                "attribute highp vec4 a_color;\n" +
                "attribute highp vec2 a_texCoord0;\n" +
                "uniform mat4 u_projTrans;\n" +
                "varying highp vec4 v_color;\n" +
                "varying highp vec2 v_texCoords;\n" +
                "void main() {\n" +
                "v_color = a_color;\n" +
                "v_texCoords = a_texCoord0;\n" +
                "gl_Position = u_projTrans * a_position;\n" +
                "}",
            "varying highp vec4 v_color;\n" +
                "varying highp vec2 v_texCoords;\n" +
                "uniform sampler2D u_texture;\n" +
                "void main() {\n" +
                "gl_FragColor = vec4(0.0);\n" +
                "highp vec4 color = texture2D(u_texture, v_texCoords);\n" +
                "if (color.a > 0.0) {\n" +
                "gl_FragColor = vec4(color.r - 0.2, color.g + 0.0, color.b + 0.4, color.a);\n" +
                "}\n" +
                "}"
        );
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
                new Portal(x, y, width, height, animManager, world, (String) properties.get("stageexit"), this);
            }
        }
        player = new Player(playerx, playery, world, this, animManager);
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
                    case "wyvern":
                        new Wyvern(posx, posy, world, animManager);
                        break;
                    case "lizard":
                        new Lizard(posx, posy, world, animManager);
                        break;
                    case "bat":
                        new Bat(posx, posy, world, animManager);
                        break;
                    case "spikylizard":
                        new SpikyLizard(posx, posy, world, animManager);
                        break;
                    case "manticore":
                        new Manticore(posx, posy, world, animManager);
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

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        world.getBodies(bodies);
        for (int index = 0; index < bodies.size; index++) {
            if (bodies.get(index).getUserData() instanceof Entity) {
                Entity<?> e = (Entity<?>) bodies.get(index).getUserData();
                e.act(delta);
            }
            for (Fixture fixture : bodies.get(index).getFixtureList()) {
                if (fixture.getUserData() != bodies.get(index).getUserData() && fixture.getUserData() != null) {
                    ((Entity<?>) fixture.getUserData()).act(delta);
                }
            }
        }

        if (cameraInterpolationTime > 0) cameraInterpolationTime -= delta;
        updateCamera();
        draw(delta);
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
        if (currentCameraArea == -1) {
            for (int i = 0; i < cameraAreas.size(); i++) {
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
                    else c.draw(game.batch, delta);
                }
                else e.draw(game.batch, delta);
            }
        }
        game.batch.setShader(hitShader);
        for (Actor<?> actor : hitBodies) {
            if (actor.getHitEffectTimer() > 0.1f) actor.draw(game.batch, delta);
        }
        game.batch.setShader(null);
        game.batch.setColor(Color.BLACK);
        for (Actor<?> actor : hitBodies) {
            if (actor.getHitEffectTimer() <= 0.1f) actor.draw(game.batch, delta);
        }

        game.batch.setShader(counterShader);
        for (Actor<?> actor : counterBodies) {
            actor.draw(game.batch, delta);
        }
//        shaderTimer += delta;
//        shaderTimer %= 0.7f;
        //if (shaderTimer < 0.1f) game.batch.setColor(new Color(0.3f, 0.3f, 0.3f, 0));
        //else
        //game.batch.setColor(new Color(0.6f, 0.6f, 0.6f, 1f));
        game.batch.setShader(invulnerableShader);
        game.batch.setColor(Color.WHITE);
        for (Actor<?> actor : invulBodies) {
            actor.draw(game.batch, delta);
        }
        game.batch.setShader(null);
        for (AttackEffect effect : effects) {
            effect.draw(game.batch, delta);
        }
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

        uiStage.act(delta);
        debugInfo.setText(String.format(
            "%n%nPlayer State: %s%n" +
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
            player.getState(), player.getStateTime(),
            player.stats().getHealth(), player.stats().getCrystals(), player.stats().getGlideCharge(),
            player.stats().getSoarCharge(), player.input().glide, player.stats().isEvading(), player.stats().isIntangible(),
            player.getBody().getLinearVelocity().x, player.getBody().getLinearVelocity().y
            ));
        debugInfo.pack();
        debugInfo.setPosition(10, Gdx.graphics.getHeight() - debugInfo.getPrefHeight());
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
    }

    public void setDebug(boolean debug) {
        if (debug) {
            debugRenderer.setDrawBodies(!debugRenderer.isDrawBodies());
            this.debug = !this.debug;
        }
    }

    public Player getPlayer() {
        return player;
    }
}
