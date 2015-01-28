package de.hochschuletrier.gdw.ss12.game;

import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.netcode.simple.NetClientSimple;
import de.hochschuletrier.gdw.ss12.game.systems.network.NetClientSendInputSystem;
import de.hochschuletrier.gdw.ss12.game.systems.network.NetClientUpdateSystem;

public class GameClient extends Game {

    private final NetClientSimple netClient;

    public GameClient(AssetManagerX assetManager, NetClientSimple netClient) {
        super(assetManager);

        this.netClient = netClient;
    }

    @Override
    protected void addSystems() {
        super.addSystems();

        // Remember to set priorities in CustomPooledEngine when creating new system classes
        engine.addSystem(new NetClientUpdateSystem(netClient));
        engine.addSystem(new NetClientSendInputSystem());
    }
}
