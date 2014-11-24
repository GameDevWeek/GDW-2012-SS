package de.hochschuletrier.gdw.ss14.game.systems.rendering;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;

public class RenderShadowMapCleanupSystem extends EntitySystem {
    private RenderShadowMapSystem shadowMap;

    public RenderShadowMapCleanupSystem() {
        super(0);
    }

    public RenderShadowMapCleanupSystem(int priority) {
        super(priority);
    }
    
    @Override
	public void addedToEngine(Engine engine) {
        shadowMap = engine.getSystem(RenderShadowMapSystem.class);
    }
    
    @Override
	public void removedFromEngine(Engine engine) {
        shadowMap = null;
    }

    @Override
    public void update(float deltaTime) {
        shadowMap.finish();
    }
}