package de.hochschuletrier.gdw.ss12.game.systems.rendering;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.utils.DrawUtil;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.DropableComponent;

public class RenderDropableHudSystem extends EntitySystem implements SystemGameInitializer {

    private Game game;

    @Override
    public void initGame(Game game, AssetManagerX assetManager, PooledEngine engine) {
        this.game = game;
    }

    @Override
    public void update(float deltaTime) {
        Entity localPlayer = game.getLocalPlayer();
        PlayerComponent player = ComponentMappers.player.get(localPlayer);
        if (!player.isDead()) {
            DropableComponent dropable = ComponentMappers.dropable.get(localPlayer);
            if (dropable != null) {
                DrawUtil.draw(dropable.texture,
                        Constants.HUD_INVENTORY_OFFSET_X, Gdx.graphics.getHeight() - (Constants.HUD_INVENTORY_OFFSET_Y + Constants.HUD_INVENTORY_HEIGHT),
                        Constants.HUD_INVENTORY_WIDTH, Constants.HUD_INVENTORY_HEIGHT);
            }
        }
    }
}
