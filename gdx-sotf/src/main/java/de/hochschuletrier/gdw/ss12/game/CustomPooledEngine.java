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
        // Network (receiving)
        ORDER.put(NetServerUpdateSystem.class, priority++); // server
        ORDER.put(NetClientUpdateSystem.class, priority++); // client

        // Input
        ORDER.put(KeyboardInputSystem.class, priority++); // both
        ORDER.put(BotSystem.class, priority++); // server
        ORDER.put(InputSystem.class, priority++); // both
        ORDER.put(NetClientSendInputSystem.class, priority++); // client

        // Logic
        ORDER.put(PhysixSystem.class, priority++); // both
        ORDER.put(UpdatePositionSystem.class, priority++); // both
        ORDER.put(UpdateSoundEmitterSystem.class, priority++); // both
        ORDER.put(EntitySpawnSystem.class, priority++); // both
        ORDER.put(SpawnRandomEatableSystem.class, priority++); // server
        ORDER.put(PowerupSystem.class, priority++); // server
        ORDER.put(UpdatePlayerEffectsSystem.class, priority++); // both
        ORDER.put(UpdatePlayerSystem.class, priority++); // server
        ORDER.put(UpdateLightSystem.class, priority++); // server
        ORDER.put(RemoveAnimatedItemSystem.class, priority++); // server
        ORDER.put(GameStateSystem.class, priority++); // server
        ORDER.put(NetServerSendSystem.class, priority++); // server
        ORDER.put(CameraSystem.class, priority++); // both

        // World rendering (all both)
        ORDER.put(RenderShadowMapSystem.class, priority++);
        ORDER.put(RenderMapSystem.class, priority++);
        ORDER.put(RenderParticleEffectSystem.class, priority++);
        ORDER.put(RenderItemTextureSystem.class, priority++);
        ORDER.put(RenderItemAnimationSystem.class, priority++);
        ORDER.put(RenderPlayerSystem.class, priority++);
        ORDER.put(RenderShadowMapCleanupSystem.class, priority++);
        ORDER.put(PhysixDebugRenderSystem.class, priority++);

        // HUD Rendering (all both)
        ORDER.put(RenderMiniMapSystem.class, priority++);
        ORDER.put(RenderPowerupHudSystem.class, priority++);
        ORDER.put(RenderDropableHudSystem.class, priority++);
        ORDER.put(RenderPizzaHudSystem.class, priority++);
        ORDER.put(RenderScoreHudSystem.class, priority++);
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
