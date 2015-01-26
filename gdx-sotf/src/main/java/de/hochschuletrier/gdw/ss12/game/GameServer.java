package de.hochschuletrier.gdw.ss12.game;

import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.ss12.game.systems.network.NetServerSendSystem;
import de.hochschuletrier.gdw.ss12.game.systems.network.NetServerUpdateSystem;

public class GameServer extends GameLocal {

    public GameServer(AssetManagerX assetManager) {
        super(assetManager);
    }

    @Override
    protected void addSystems() {
        super.addSystems();

        // Remember to set priorities in CustomPooledEngine when creating new system classes
        engine.addSystem(new NetServerUpdateSystem());
        engine.addSystem(new NetServerSendSystem());
    }
}
