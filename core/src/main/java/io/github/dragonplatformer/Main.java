package io.github.dragonplatformer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    public SpriteBatch batch;
    public BitmapFont font;
    public ExtendViewport viewport;
    public AssetManager manager;
    public MainMenu menu;

    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new ExtendViewport(42, 24, new OrthographicCamera());
        font = new BitmapFont();
        font.setUseIntegerPositions(true);
        font.getData().setScale(27f / Gdx.graphics.getHeight() * 15);
        manager = new AssetManager();

        menu = new MainMenu(this, "tiledmaps/cave-1.tmx");
        this.setScreen(menu);
    }

    @Override
    public void resize(int width, int height) {
        this.screen.resize(width, height);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void pause() {

    }

    public void loadNewLevel(String newLevel) {
        menu = new MainMenu(this, "tiledmaps/" + newLevel + ".tmx");
        this.setScreen(menu);
    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
