package de.hochschuletrier.gdw.ss14.game.systems.rendering;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.utils.DrawUtil;
import de.hochschuletrier.gdw.ss14.game.ComponentMappers;
import de.hochschuletrier.gdw.ss14.game.Constants;
import de.hochschuletrier.gdw.ss14.game.Game;
import de.hochschuletrier.gdw.ss14.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss14.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss14.game.components.UseableComponent;

public class RenderInventoryHudSystem extends EntitySystem implements SystemGameInitializer {
    private Game game;
    
    public RenderInventoryHudSystem() {
        super(0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.game = game;
    }

    @Override
    public void update(float deltaTime) {
        Entity localPlayer = game.getLocalPlayer();
        PlayerComponent player = ComponentMappers.player.get(localPlayer);
        if (!player.isDead()) {
            UseableComponent useable = ComponentMappers.useable.get(localPlayer);
            if(useable != null) {
                DrawUtil.draw(useable.texture,
                        Constants.HUD_INVENTORY_OFFSET_X, Constants.HUD_INVENTORY_OFFSET_Y,
                        Constants.HUD_INVENTORY_WIDTH, Constants.HUD_INVENTORY_HEIGHT);
            }
        }
    }
}
