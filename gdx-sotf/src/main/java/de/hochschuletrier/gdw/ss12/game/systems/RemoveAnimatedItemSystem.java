package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixBodyComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixModifierComponent;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.components.RenderAnimationComponent;

public class RemoveAnimatedItemSystem extends IteratingSystem {

    private Engine engine;

    public RemoveAnimatedItemSystem() {
        super(Family.all(RenderAnimationComponent.class).exclude(PhysixBodyComponent.class, PhysixModifierComponent.class).get(), 0);
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
        RenderAnimationComponent render = ComponentMappers.renderAnimation.get(entity);
        if (render.stateTime > render.animation.animationDuration) {
            engine.removeEntity(entity);
        }
    }
}
