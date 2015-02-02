package de.hochschuletrier.gdw.ss12.game.interfaces;

import com.badlogic.ashley.core.PooledEngine;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.ss12.game.Game;

public interface SystemGameInitializer {

    void initGame(Game game, AssetManagerX assetManager, PooledEngine engine);
}
