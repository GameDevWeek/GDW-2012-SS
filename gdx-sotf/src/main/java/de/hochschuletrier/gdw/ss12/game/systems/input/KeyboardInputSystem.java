package de.hochschuletrier.gdw.ss12.game.systems.input;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.components.InputComponent;

public class KeyboardInputSystem extends EntitySystem implements SystemGameInitializer, InputProcessor {

    private Game game;
    private boolean leftDown;
    private boolean rightDown;
    private boolean upDown;
    private boolean downDown;

    public KeyboardInputSystem() {
        super(0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.game = game;
    }

    @Override
    public void update(float deltaTime) {
        Entity localPlayer = game.getLocalPlayer();
        if (!ComponentMappers.player.get(localPlayer).isSlipping()) {
            InputComponent input = ComponentMappers.input.get(localPlayer);
            float velX = 0, velY = 0;
            if (leftDown) {
                velX -= 1;
            }
            if (rightDown) {
                velX += 1;
            }
            if (upDown) {
                velY -= 1;
            }
            if (downDown) {
                velY += 1;
            }
            input.moveDirection.set(velX, velY).nor();
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        keyUpDown(keycode, true);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (!keyUpDown(keycode, false)) {
            if (keycode == Input.Keys.SPACE) {
                Entity localPlayer = game.getLocalPlayer();
                InputComponent input = ComponentMappers.input.get(localPlayer);
                input.dropItem = true;
            }
        }

        return true;
    }

    private boolean keyUpDown(int keycode, final boolean value) {
        switch (keycode) {
            case Input.Keys.LEFT:
                leftDown = value;
                return true;
            case Input.Keys.RIGHT:
                rightDown = value;
                return true;
            case Input.Keys.UP:
                upDown = value;
                return true;
            case Input.Keys.DOWN:
                downDown = value;
                return true;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        return true;
    }
}
