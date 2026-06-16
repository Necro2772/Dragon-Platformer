package io.github.dragonplatformer;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.github.dragonplatformer.Entity.Actor.Player.Player;

public class GameInputProcessor implements InputProcessor {
    private final Player player;
    private final GameScreen screen;
    public GameInputProcessor(Player player, GameScreen screen) {
        super();
        this.player = player;
        this.screen = screen;
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.A:
            case Input.Keys.LEFT:
                player.input.setLeftMove(true);
                break;
            case Input.Keys.D:
            case Input.Keys.RIGHT:
                player.input.setRightMove(true);
                break;
            case Input.Keys.S:
            case Input.Keys.DOWN:
                player.input.setDownMove(true);
                break;
            case Input.Keys.W:
            case Input.Keys.UP:
                player.input.setUpMove(true);
                break;
            case Input.Keys.SPACE:
                player.input.setJump(true);
                break;
            case Input.Keys.SHIFT_LEFT:
                player.input.setGuard(true);
                break;
            case Input.Keys.CONTROL_LEFT:
                player.input.setEvade(true);
                break;
            case Input.Keys.E:
                player.input.setUseProjectile(true);
                break;
            case Input.Keys.B:
                screen.setDebug(true);
                break;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.A:
            case Input.Keys.LEFT:
                player.input.setLeftMove(false);
                break;
            case Input.Keys.D:
            case Input.Keys.RIGHT:
                player.input.setRightMove(false);
                break;
            case Input.Keys.S:
            case Input.Keys.DOWN:
                player.input.setDownMove(false);
                break;
            case Input.Keys.W:
            case Input.Keys.UP:
                player.input.setUpMove(false);
                break;
            case Input.Keys.SHIFT_LEFT:
                player.input.setGuard(false);
                break;
            case Input.Keys.CONTROL_LEFT:
                player.input.setEvade(false);
                break;
            case Input.Keys.E:
                player.input.setUseProjectile(false);
                break;
            case Input.Keys.B:
                screen.setDebug(false);
                break;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        switch (button) {
            case Input.Buttons.RIGHT:
                player.input.setUseProjectile(true);
                break;
            case Input.Buttons.LEFT:
                player.input.setUseMelee(true);
                break;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        switch (button) {
            case Input.Buttons.RIGHT:
                player.input.setUseProjectile(false);
                break;
            case Input.Buttons.LEFT:
                player.input.setUseMelee(false);
                break;
        }
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector3 worldCoords = screen.getCamera().unproject(new Vector3(screenX, screenY, 0));
        player.input.setCursor(new Vector2(worldCoords.x, worldCoords.y));
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        Vector3 worldCoords = screen.getCamera().unproject(new Vector3(screenX, screenY, 0));
        player.input.setCursor(new Vector2(worldCoords.x, worldCoords.y));
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
