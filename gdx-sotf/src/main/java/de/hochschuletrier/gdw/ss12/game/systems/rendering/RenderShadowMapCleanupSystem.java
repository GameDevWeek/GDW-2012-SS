package de.hochschuletrier.gdw.ss12.game.systems.rendering;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;

public class RenderShadowMapCleanupSystem extends EntitySystem implements SystemGameInitializer {

    private RenderShadowMapSystem shadowMap;

    @Override
    public void initGame(Game game, AssetManagerX assetManager, PooledEngine engine) {
        shadowMap = engine.getSystem(RenderShadowMapSystem.class);
    }

    @Override
    public void update(float deltaTime) {
        shadowMap.finish();
    }
}
