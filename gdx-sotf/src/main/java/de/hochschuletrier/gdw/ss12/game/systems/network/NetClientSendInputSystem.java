package de.hochschuletrier.gdw.ss12.game.systems.network;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.netcode.core.NetConnection;
import de.hochschuletrier.gdw.commons.netcode.simple.NetClientSimple;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.InputComponent;
import de.hochschuletrier.gdw.ss12.game.datagrams.DropItemDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.PlayerInputDatagram;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;

public class NetClientSendInputSystem extends EntitySystem implements SystemGameInitializer {

    private Game game;
    private NetClientSimple netClient;

    public NetClientSendInputSystem() {
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
            final NetConnection connection = netClient.getConnection();
            connection.sendUnreliable(PlayerInputDatagram.create(localPlayer));
            InputComponent input = ComponentMappers.input.get(localPlayer);
            if (input.dropItem) {
                connection.sendReliable(DropItemDatagram.create(localPlayer));
                input.dropItem = false;
            }
        }
    }
}
