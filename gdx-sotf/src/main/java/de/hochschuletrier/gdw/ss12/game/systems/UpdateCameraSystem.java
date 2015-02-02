package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.cameras.orthogonal.LimitedSmoothCamera;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.PositionComponent;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;

public class UpdateCameraSystem extends EntitySystem implements SystemGameInitializer {

    private final LimitedSmoothCamera camera;
    private Game game;
    public boolean forceCameraUpdate;

    public UpdateCameraSystem(LimitedSmoothCamera camera) {
        super(0);
        this.camera = camera;
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager, PooledEngine engine) {
        this.game = game;
    }

    @Override
    public void update(float deltaTime) {
        PositionComponent position = ComponentMappers.position.get(game.getLocalPlayer());
        camera.setDestination(position.x, position.y);
        if(forceCameraUpdate) {
            forceCameraUpdate = false;
            camera.updateForced();
        } else {
            camera.update(deltaTime);
        }
        camera.bind();
    }

    public void forceCameraUpdate() {
        forceCameraUpdate = true;
    }
}
