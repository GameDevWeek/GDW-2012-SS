package de.hochschuletrier.gdw.ss12.game;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import de.hochschuletrier.gdw.commons.gdx.physix.systems.PhysixDebugRenderSystem;
import de.hochschuletrier.gdw.commons.gdx.physix.systems.PhysixSystem;
import de.hochschuletrier.gdw.ss12.game.systems.*;
import de.hochschuletrier.gdw.ss12.game.systems.input.*;
import de.hochschuletrier.gdw.ss12.game.systems.network.*;
import de.hochschuletrier.gdw.ss12.game.systems.rendering.*;
import java.util.HashMap;

/**
 *
 * @author Santo Pfingsten
 */
public class CustomPooledEngine extends PooledEngine {
    
    private static final HashMap<Class, Integer> ORDER = new HashMap();
    
    static {
        // System priorities are set here:
        int priority = 0;
        // Logic
        ORDER.put(KeyboardInputSystem.class, priority++);
        ORDER.put(BotSystem.class, priority++);
        ORDER.put(InputSystem.class, priority++);
        ORDER.put(PhysixSystem.class, priority++);
        ORDER.put(UpdatePositionSystem.class, priority++);
        ORDER.put(UpdateSoundEmitterSystem.class, priority++);
        ORDER.put(EntitySpawnSystem.class, priority++);
        ORDER.put(PowerupSystem.class, priority++);
        ORDER.put(UpdatePlayerSystem.class, priority++);
        ORDER.put(UpdateLightSystem.class, priority++);
        ORDER.put(GameStateSystem.class, priority++);
        
        // Network
        ORDER.put(NetServerSendSystem.class, priority++);
        ORDER.put(NetServerUpdateSystem.class, priority++);
        ORDER.put(NetClientSendControlSystem.class, priority++);
        ORDER.put(NetClientUpdateSystem.class, priority++);
        
        // Rendering
        ORDER.put(RenderShadowMapSystem.class, priority++);
        ORDER.put(RenderMapSystem.class, priority++);
        ORDER.put(RenderItemSystem.class, priority++);
        ORDER.put(RenderPlayerSystem.class, priority++);
        ORDER.put(RenderShadowMapCleanupSystem.class, priority++);
        ORDER.put(PhysixDebugRenderSystem.class, priority++);
        ORDER.put(RenderMiniMapSystem.class, priority++);
        ORDER.put(RenderPowerupHudSystem.class, priority++);
        ORDER.put(RenderPizzaHudSystem.class, priority++);
        ORDER.put(RenderNoticeSystem.class, priority++);
    }

    public CustomPooledEngine() {
        super(Constants.ENTITY_POOL_INITIAL_SIZE, Constants.ENTITY_POOL_MAX_SIZE,
            Constants.COMPONENT_POOL_INITIAL_SIZE, Constants.COMPONENT_POOL_MAX_SIZE);
    }

    @Override
    public void addSystem(EntitySystem system) {
        system.priority = ORDER.get(system.getClass());
        super.addSystem(system);
    }
    
}
