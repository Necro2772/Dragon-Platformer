package io.github.dragonplatformer;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import io.github.dragonplatformer.Entity.Player;

public class GameInputProcessor implements InputProcessor {
    Player player;
    public GameInputProcessor(Player player) {
        super();
        this.player = player;
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
                player.input.setGlide(true);
                break;
            case Input.Keys.E:
                player.input.setProjectile(true);
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
                player.input.setGlide(false);
                break;
            case Input.Keys.E:
                player.input.setProjectile(false);
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
                player.input.setProjectile(true);
                break;
            case Input.Buttons.LEFT:
                player.meleeAttack();
                break;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        switch (button) {
            case Input.Buttons.RIGHT:
                player.input.setProjectile(false);
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
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
