package io.github.dragonplatformer;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.ScreenUtils;

public class MainMenu implements Screen {
    public final Main game;
    public String currentLevel;

    public MainMenu(final Main game) {
        this.game = game;

        currentLevel = "tiledmaps/cavern.tmx";

        game.manager.load("images/pack.atlas", TextureAtlas.class);
        game.manager.setLoader(TiledMap.class, new TmxMapLoader());
        game.manager.load(currentLevel, TiledMap.class);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (game.manager.update()) {
            game.setScreen(new GameScreen(game, game.manager.get(currentLevel)));
            this.dispose();
        } else {
            ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
            game.batch.begin();
            game.font.draw(game.batch, "Loading... " + game.manager.getProgress() * 100 + "%", 1, game.viewport.getWorldHeight() - 1);
            game.batch.end();
        }
    }

    @Override
    public void resize(int width, int height) {

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

    }
}
