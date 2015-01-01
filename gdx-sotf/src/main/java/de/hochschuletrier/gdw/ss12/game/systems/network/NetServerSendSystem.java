package de.hochschuletrier.gdw.ss12.game.systems.network;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.math.Vector2;
import de.hochschuletrier.gdw.commons.netcode.core.NetConnection;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.components.InputComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.datagrams.CreateEntityDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.DatagramType;
import de.hochschuletrier.gdw.ss12.game.datagrams.PlayerStateDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.WorldSetupDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.WorldStateDatagram;

public class NetServerSendSystem extends EntitySystem {

    public NetServerSendSystem() {
        super(0);
    }

    @Override
    public void update(float deltaTime) {
//        for (Entity entity : players) {
//            PlayerComponent player = ComponentMappers.player.get(entity);
//            PlayerStateDatagram playerState = (PlayerStateDatagram)DatagramType.WORLD_STATE.create();
//            playerState.setup(player);
//            netServer.broadcastUnreliable(playerState);
//        }
//
//        if (stateChanged) {
//            WorldStateDatagram worldState = (WorldStateDatagram)DatagramType.WORLD_STATE.create();
//            worldState.setup();
//            netServer.broadcastUnreliable(worldState);
//            stateChanged = false;
//        }
    }

//    public void sendWorldSetup(NetConnection connection) {
//        WorldSetupDatagram worldSetup = (WorldSetupDatagram)DatagramType.WORLD_SETUP.create();
//        worldSetup.setup(getMapName(), paused, player.getID(), player.getPosition());
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
//    }
}
