package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.systems.IteratingSystem;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixBodyComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixModifierComponent;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.RenderAnimationComponent;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;

public class RemoveAnimatedItemSystem extends IteratingSystem implements SystemGameInitializer {

    private PooledEngine engine;

    public RemoveAnimatedItemSystem() {
        super(Family.all(RenderAnimationComponent.class).exclude(PhysixBodyComponent.class, PhysixModifierComponent.class).get(), 0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager, PooledEngine engine) {
        this.engine = engine;
    }

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        RenderAnimationComponent render = ComponentMappers.renderAnimation.get(entity);
        if (render.stateTime > render.animation.animationDuration) {
            engine.removeEntity(entity);
        }
    }
}
