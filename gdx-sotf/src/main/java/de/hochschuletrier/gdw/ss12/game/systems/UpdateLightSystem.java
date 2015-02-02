package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.LightComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;

public class UpdateLightSystem extends IteratingSystem implements SystemGameInitializer {

    private PooledEngine engine;

    public UpdateLightSystem() {
        super(Family.all(LightComponent.class).exclude(PlayerComponent.class).get(), 0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager, PooledEngine engine) {
        this.engine = engine;
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        LightComponent light = ComponentMappers.light.get(entity);

        light.radius -= light.shrinkPixelPerSecond * deltaTime;
        if (light.radius < 60.0f) {
            engine.removeEntity(entity);
        }
    }
}
