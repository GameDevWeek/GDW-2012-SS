package de.hochschuletrier.gdw.ss12.game.systems.rendering;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;

public class RenderShadowMapCleanupSystem extends EntitySystem implements SystemGameInitializer {

    private RenderShadowMapSystem shadowMap;
    private Engine engine;

    public RenderShadowMapCleanupSystem() {
        super(0);
    }

    @Override
    public void addedToEngine(Engine engine) {
        this.engine = engine;
    }

    @Override
    public void removedFromEngine(Engine engine) {
        this.engine = null;
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        shadowMap = engine.getSystem(RenderShadowMapSystem.class);
    }

    @Override
    public void update(float deltaTime) {
        shadowMap.finish();
    }
}
