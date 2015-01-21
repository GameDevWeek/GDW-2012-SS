package de.hochschuletrier.gdw.ss12.game.systems.network;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.netcode.simple.NetClientSimple;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.datagrams.PlayerInputDatagram;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;

public class NetClientSendControlSystem extends EntitySystem implements SystemGameInitializer {

    private Game game;
    private NetClientSimple netClient;

    public NetClientSendControlSystem() {
        super(0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.game = game;
    }

    @Override
    public void update(float deltaTime) {
        if (netClient.isRunning()) {
            Entity localPlayer = game.getLocalPlayer();
            netClient.getConnection().sendUnreliable(PlayerInputDatagram.create(localPlayer));
        }
    }
}
