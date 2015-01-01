package de.hochschuletrier.gdw.ss14.game.systems.input;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.ss14.game.ComponentMappers;
import de.hochschuletrier.gdw.ss14.game.Game;
import de.hochschuletrier.gdw.ss14.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss14.game.components.InputComponent;

public class KeyboardInputSystem extends EntitySystem implements SystemGameInitializer {
    private Game game;

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
        InputComponent input = ComponentMappers.input.get(localPlayer);
        float velX = 0, velY = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            velX -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            velX += 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            velY -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            velY += 1;
        }
        input.moveDirection.set(velX, velY).nor();
    }
}
