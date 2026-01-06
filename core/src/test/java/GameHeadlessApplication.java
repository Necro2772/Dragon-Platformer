import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.github.dragonplatformer.Entity.Creature.Player;
import io.github.dragonplatformer.GameScreen;
import io.github.dragonplatformer.Main;

public class GameHeadlessApplication extends Main {

    public Player player;
    public World world;
    public GameScreen screen;

    public GameHeadlessApplication() {
        super();
    }

    @Override
    public void create() {
        batch = null;
        viewport = new ExtendViewport(48, 27, new OrthographicCamera());
        font = new BitmapFont();
        font.setUseIntegerPositions(true);
        font.getData().setScale(27f / Gdx.graphics.getHeight() * 15);
        manager = new AssetManager();
        manager.load("images/pack.atlas", TextureAtlas.class);
        manager.setLoader(TiledMap.class, new TmxMapLoader());
        manager.load("tiledmaps/hub.tmx", TiledMap.class);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {
        if (manager.update()) {
            screen = new GameScreen(this, manager.get("tiledmaps/hub.tmx"));
            setScreen(screen);
        }

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }
}
