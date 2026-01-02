import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.dragonplatformer.Entity.Player;

public class GameHeadlessApplication extends Game {
    public Player player;
    public World world;
    public AssetManager assets;

    public GameHeadlessApplication() {
        super();
    }

    @Override
    public void create() {
        assets = new AssetManager();
        assets.load("images/pack.atlas", TextureAtlas.class);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {
        if (assets.update()) {
            TextureAtlas atlas = assets.get("images/pack.atlas", TextureAtlas.class);
            world = new World(new Vector2(0, 9.8f), true);
            player = new Player(atlas, 2, 2, 1, 1, world);
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
