package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.components.LightComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;

public class UpdateLightSystem extends IteratingSystem {

    private Engine engine;

    public UpdateLightSystem() {
        super(Family.all(LightComponent.class).exclude(PlayerComponent.class).get(), 0);
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        this.engine = engine;
    }

    @Override
    public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        this.engine = null;
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
