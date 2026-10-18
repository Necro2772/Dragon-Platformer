package io.github.dragonplatformer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class CharacterStatsScreen implements Screen {
    private final Main game;
    private final CharacterStatsInputProcessor inputProcessor;
    private final Stage uiStage;
    private final Table uiTable;
    private final Skin skin;
    private boolean debug = false;

    public CharacterStatsScreen(Main game) {
        this.game = game;
        this.inputProcessor = new CharacterStatsInputProcessor(this);
        Gdx.input.setInputProcessor(inputProcessor);
        uiStage = new Stage(new ScreenViewport());
        uiTable = new Table();
        TextureAtlas atlas = game.manager.get("images/pack.atlas");
        skin = new Skin(atlas);
        uiStage.addActor(uiTable);
        uiTable.setFillParent(true);
        loadUI();
    }

    private void loadUI() {
        BitmapFont defaultFont = new BitmapFont();
        defaultFont.getData().setScale(2);
        skin.add("default", new Label.LabelStyle(defaultFont, Color.WHITE));

        Image background = new Image();
        background.setFillParent(true);
        uiTable.addActor(background);
        Label tabLabel = new Label("Stats", skin);
        tabLabel.setAlignment(Align.center);
        uiTable.pad(10).top();
        uiTable.add(tabLabel).size(Value.percentWidth(0.5f, uiTable), Value.percentHeight(0.1f, uiTable)).center();
    }

    public void closeScreen() {
        game.closeStats();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputProcessor);
        Gdx.input.setCursorCatched(false);
    }

    @Override
    public void render(float delta) {
        uiStage.act(delta);
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        uiStage.getViewport().update(width, height, true);
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

    public void toggleDebug() {
        debug = !debug;
        uiTable.setDebug(debug, true);
    }
}
