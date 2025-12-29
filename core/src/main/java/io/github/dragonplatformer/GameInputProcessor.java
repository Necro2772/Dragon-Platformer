package io.github.dragonplatformer;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

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
                player.setLeftMove(true);
                break;
            case Input.Keys.D:
            case Input.Keys.RIGHT:
                player.setRightMove(true);
                break;
            case Input.Keys.SPACE:
                player.setJump(true);
                break;
            case Input.Keys.SHIFT_LEFT:
                player.setGlide(true);
                break;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.A:
            case Input.Keys.LEFT:
                player.setLeftMove(false);
                break;
            case Input.Keys.D:
            case Input.Keys.RIGHT:
                player.setRightMove(false);
                break;
            case Input.Keys.SHIFT_LEFT:
                player.setGlide(false);
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
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
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
