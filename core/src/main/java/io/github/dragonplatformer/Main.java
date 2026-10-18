package io.github.dragonplatformer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.github.dragonplatformer.Entity.Actor.Player.PlayerStats;
import io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.BreathUpgrade.FireBreath;
import io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.BreathUpgrade.IceBreath;
import io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.EvadeUpgrade.DefaultEvade;
import io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.EvadeUpgrade.FireEvade;
import io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.EvadeUpgrade.IceEvade;
import io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.MeleeUpgrade.DefaultMelee;
import io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.MeleeUpgrade.EarthMelee;
import io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.MeleeUpgrade.FireMelee;
import io.github.dragonplatformer.Entity.Actor.Player.PlayerUpgrade.MeleeUpgrade.IceMelee;
import io.github.dragonplatformer.Screen.GameScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    public SpriteBatch batch;
    public BitmapFont font;
    public ExtendViewport viewport;
    public AssetManager manager;
    public LoadScreen currentLevel;
    public CharacterStatsScreen statsScreen;
    public GameScreen currentScreen;
    public PlayerStats playerStats;

    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new ExtendViewport(42, 24, 60, 30, new OrthographicCamera());
        font = new BitmapFont();
        font.setUseIntegerPositions(true);
        font.getData().setScale(27f / Gdx.graphics.getHeight() * 15);
        manager = new AssetManager();

        playerStats = new PlayerStats(20, 20, 0);

        playerStats.upgrade(new DefaultMelee(1));
        playerStats.upgrade(new FireBreath(1));
        playerStats.upgrade(new DefaultEvade(1));

        currentLevel = new LoadScreen(this, "tiledmaps/hub.tmx");
        this.setScreen(currentLevel);
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
        currentLevel = new LoadScreen(this, "tiledmaps/" + newLevel + ".tmx");
        this.setScreen(currentLevel);
    }

    public void returnToHub() {
        playerStats.setHealth(playerStats.getMaxHealth());
        playerStats.addCrystals(-playerStats.getCrystals());
        loadNewLevel("hub");
    }

    public void openStats(GameScreen currentScreen) {
        if (statsScreen == null) statsScreen = new CharacterStatsScreen(this);
        this.currentScreen = currentScreen;
        this.setScreen(statsScreen);
    }

    public void closeStats() {
        this.setScreen(currentScreen);
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
