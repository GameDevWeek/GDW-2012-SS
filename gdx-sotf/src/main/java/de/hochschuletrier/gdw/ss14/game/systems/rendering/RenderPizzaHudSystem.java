package de.hochschuletrier.gdw.ss14.game.systems.rendering;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import de.hochschuletrier.gdw.commons.gdx.assets.AnimationExtended;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.utils.DrawUtil;
import de.hochschuletrier.gdw.ss14.game.ComponentMappers;
import de.hochschuletrier.gdw.ss14.game.Constants;
import de.hochschuletrier.gdw.ss14.game.Game;
import de.hochschuletrier.gdw.ss14.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss14.game.components.PlayerComponent;

public class RenderPizzaHudSystem extends EntitySystem implements SystemGameInitializer {

    private final Texture[] pizza = new Texture[8];
    private AnimationExtended pizzaAnimation;
    private Texture underlay;
    private float stateTime;
    private Game game;
    
    public RenderPizzaHudSystem() {
        super(0);
    }

    public RenderPizzaHudSystem(int priority) {
        super(priority);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.game = game;
    }
    
    public void init(AssetManagerX assetManagerX) {
        underlay = assetManagerX.getTexture("hud_underlay_pizza");

        for (int i = 0; i < 8; i++) {
            pizza[i] = assetManagerX.getTexture("teambuff_pizza_" + (i + 1));
        }

        pizzaAnimation = assetManagerX.getAnimation("teambuff_pizza");
    }

    @Override
    public void update(float deltaTime) {
        Entity localPlayer = game.getLocalPlayer();
        stateTime += deltaTime;
        PlayerComponent player = ComponentMappers.player.get(localPlayer);
        int numSlices = player.team.pizzaCount;
        DrawUtil.draw(underlay, (Gdx.graphics.getWidth() / 2 - pizza[0].getWidth() / 2) - 34, Constants.HUD_PIZZA_OFFSET_Y - 28);
        if (numSlices > 0 && numSlices <= 8) {
            float x = (Gdx.graphics.getWidth() / 2) - (pizza[numSlices - 1].getWidth() / 2);
            float y = Constants.HUD_PIZZA_OFFSET_Y;
            if (numSlices == 8) {
                DrawUtil.batch.draw(pizzaAnimation.getKeyFrame(stateTime), x, y);
            } else {
                DrawUtil.draw(pizza[numSlices - 1], x, y);
            }
        }
    }
}
