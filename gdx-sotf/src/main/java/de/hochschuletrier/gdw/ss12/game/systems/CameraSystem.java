package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.cameras.orthogonal.LimitedSmoothCamera;
import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.ss12.Main;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.PositionComponent;
import de.hochschuletrier.gdw.ss12.game.data.Team;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemMapInitializer;

public class CameraSystem extends EntitySystem implements SystemGameInitializer, SystemMapInitializer {

    private final LimitedSmoothCamera camera = new LimitedSmoothCamera();
    private Game game;
    public boolean forceCameraUpdate;

    public LimitedSmoothCamera getCamera() {
        return camera;
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager, PooledEngine engine) {
        this.game = game;
    }

    @Override
    public void initMap(TiledMap map, Array<Team> teams) {
        camera.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        float totalMapWidth = map.getWidth() * map.getTileWidth();
        float totalMapHeight = map.getHeight() * map.getTileHeight();
        camera.setBounds(0, 0, totalMapWidth, totalMapHeight);
        Main.getInstance().addScreenListener(camera);
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
