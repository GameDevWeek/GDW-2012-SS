package de.hochschuletrier.gdw.ss14.states;

import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.state.BaseGameState;
import de.hochschuletrier.gdw.ss14.game.Game;

/**
 * Gameplay state
 * 
 * @author Santo Pfingsten
 */
public class GameplayState extends BaseGameState {

    private final Game game;

    public GameplayState(AssetManagerX assetManager, Game game) {
        this.game = game;
    }

    @Override
    public void update(float delta) {
        game.update(delta);
    }

    @Override
    public void dispose() {
        game.dispose();
    }
}
