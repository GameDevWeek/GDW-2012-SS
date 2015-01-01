package de.hochschuletrier.gdw.ss14.game.systems.rendering;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.utils.DrawUtil;
import de.hochschuletrier.gdw.ss14.Main;
import de.hochschuletrier.gdw.ss14.game.CircularProgressRenderer;
import de.hochschuletrier.gdw.ss14.game.ComponentMappers;
import de.hochschuletrier.gdw.ss14.game.Constants;
import de.hochschuletrier.gdw.ss14.game.Game;
import de.hochschuletrier.gdw.ss14.game.components.data.Powerup;
import de.hochschuletrier.gdw.ss14.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss14.game.interfaces.SystemGameInitializer;

public class RenderPowerupHudSystem extends EntitySystem implements SystemGameInitializer {

    private CircularProgressRenderer progressRenderer;
    private Game game;

    public RenderPowerupHudSystem() {
        super(0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.game = game;
        progressRenderer = new CircularProgressRenderer(assetManager.getTexture("powerup_progress"));
    }

    @Override
    public void update(float deltaTime) {
        Main.getInstance().screenCamera.bind();
        int col = 0;
        int row = 0;

        PlayerComponent player = ComponentMappers.player.get(game.getLocalPlayer());
        for (Powerup powerup : player.powerups) {
            float x = Gdx.graphics.getWidth() - Constants.HUD_POWERUPS_ICON_OFFSET_X - (150 - (col * Constants.HUD_POWERUPS_ICON_SIZE));
            float y = Constants.HUD_POWERUPS_ICON_OFFSET_Y + (row * Constants.HUD_POWERUPS_ICON_SIZE);

            DrawUtil.draw(powerup.image, x, y-42);
            
            // Mit einem Dreisatz die groesse des Kreises bestimmen.
            float progress = powerup.expiredTime / (float) powerup.lifetime;
            float angle = 360 * progress;
            progressRenderer.draw(DrawUtil.batch, x, y, angle);

            col++;
            if (col % 3 == 0) {
                col = 0;
                row++;
            }
        }
    }
}
