package de.hochschuletrier.gdw.ss12.game;

import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.ss12.game.systems.network.NetClientSendInputSystem;
import de.hochschuletrier.gdw.ss12.game.systems.network.NetClientUpdateSystem;

public class GameClient extends Game {

    public GameClient(AssetManagerX assetManager) {
        super(assetManager);
    }

    @Override
    protected void addSystems() {
        super.addSystems();
        
        // Remember to set priorities in CustomPooledEngine when creating new system classes
        engine.addSystem(new NetClientSendInputSystem());
        engine.addSystem(new NetClientUpdateSystem());
    }
}
