package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixBodyComponent;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.components.PositionComponent;

public class UpdatePositionSystem extends IteratingSystem {

    public UpdatePositionSystem() {
        super(Family.all(PositionComponent.class, PhysixBodyComponent.class).get(), 0);
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        PhysixBodyComponent physix = ComponentMappers.physixBody.get(entity);
        PositionComponent position = ComponentMappers.position.get(entity);
        if(!position.ignorePhysix) {
            position.x = physix.getX();
            position.y = physix.getY();
        }
    }

}