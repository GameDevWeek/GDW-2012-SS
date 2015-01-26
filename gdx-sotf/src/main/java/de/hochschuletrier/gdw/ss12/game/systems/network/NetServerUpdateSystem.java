package de.hochschuletrier.gdw.ss12.game.systems.network;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.netcode.core.NetConnection;
import de.hochschuletrier.gdw.commons.netcode.simple.NetDatagramHandler;
import de.hochschuletrier.gdw.commons.netcode.simple.NetServerSimple;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.InputComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.datagrams.ConnectDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.DropItemDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.PlayerInputDatagram;

public class NetServerUpdateSystem extends EntitySystem implements NetDatagramHandler, NetServerSimple.Listener, SystemGameInitializer {

    private Game game;

    public NetServerUpdateSystem() {
        super(0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.game = game;
    }

    @Override
    public void update(float deltaTime) {
        // fixme: call update on manager
    }

    @Override
    public boolean onConnect(NetConnection connection) {
        Entity playerEntity = game.acquireBotPlayer();
        if (playerEntity == null) {
            // no free players available
            return false;
        }

        connection.setAttachment(playerEntity);
        return true;
    }

    @Override
    public void onDisconnect(NetConnection connection) {
        Entity playerEntity = (Entity) connection.getAttachment();
        if (playerEntity != null) {
            game.freeBotPlayer(playerEntity);
        }
    }

    public void sendWorldSetup(NetConnection connection) {
//        WorldSetupDatagram worldSetup = WorldSetupDatagram.create(getMapName(), paused, player.getID(), player.getPosition());
//        
//        connection.sendReliable(worldSetup);
//        for (Entity entity : eatables) {
//            CreateEntityDatagram createEntity = (CreateEntityDatagram)DatagramType.CREATE_ENTITY.create();
//            createEntity.setEntity(entity);
//            connection.sendReliable(createEntity);
//        }
//
//        WorldStateDatagram worldState = (WorldStateDatagram)DatagramType.WORLD_STATE.create();
//        worldState.setup();
//        connection.sendReliable(worldState);
    }

    public void handle(ConnectDatagram datagram) {
        NetConnection connection = datagram.getConnection();
        if (connection.isConnected()) {
            Entity playerEntity = (Entity) connection.getAttachment();
            PlayerComponent player = ComponentMappers.player.get(playerEntity);
            player.name = datagram.getPlayerName();
            sendWorldSetup(connection);
            //fixme: send new name to everyone
        }
    }

    public void handle(PlayerInputDatagram datagram) {
        NetConnection connection = datagram.getConnection();
        Entity playerEntity = (Entity) connection.getAttachment();
        ComponentMappers.position.get(playerEntity).set(datagram.getMoveDirection());
    }

    public void handle(DropItemDatagram datagram) {
        NetConnection connection = datagram.getConnection();
        Entity playerEntity = (Entity) connection.getAttachment();
        InputComponent input = ComponentMappers.input.get(playerEntity);
        input.dropItem = true;
    }
}
