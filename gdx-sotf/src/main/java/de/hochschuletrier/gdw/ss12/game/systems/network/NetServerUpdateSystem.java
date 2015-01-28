package de.hochschuletrier.gdw.ss12.game.systems.network;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.netcode.core.NetConnection;
import de.hochschuletrier.gdw.commons.netcode.simple.NetDatagramHandler;
import de.hochschuletrier.gdw.commons.netcode.simple.NetServerSimple;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.GameServer;
import de.hochschuletrier.gdw.ss12.game.components.InputComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.SetupComponent;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.datagrams.ConnectDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.CreateEntityDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.DropItemDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.PlayerInputDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.PlayerNameDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.TeamStatesDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.WorldSetupDatagram;

public class NetServerUpdateSystem extends EntitySystem implements NetDatagramHandler, NetServerSimple.Listener, SystemGameInitializer {

    private final NetServerSimple netServer;
    private GameServer game;
    private ImmutableArray<Entity> entities;
    private ImmutableArray<Entity> players;

    public NetServerUpdateSystem(NetServerSimple netServer) {
        super(0);

        this.netServer = netServer;
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.game = (GameServer) game;
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(SetupComponent.class).get());
        players = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {
        entities = null;
    }

    @Override
    public void update(float deltaTime) {
        netServer.update();
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
        connection.sendReliable(WorldSetupDatagram.create(game, (Entity) connection.getAttachment()));

        for (Entity entity : entities) {
            connection.sendReliable(CreateEntityDatagram.create(entity));
        }

        for (Entity entity : players) {
            connection.sendReliable(PlayerNameDatagram.create(entity));
        }

        connection.sendReliable(TeamStatesDatagram.create(game.getTeams()));
        //fixme: check if any of THREE,TWO,ONE,GO are currently scheduled, if so forward them
    }

    public void handle(ConnectDatagram datagram) {
        NetConnection connection = datagram.getConnection();
        if (connection.isConnected()) {
            Entity playerEntity = (Entity) connection.getAttachment();
            PlayerComponent player = ComponentMappers.player.get(playerEntity);
            player.name = datagram.getPlayerName();
            sendWorldSetup(connection);
            netServer.broadcastReliable(PlayerNameDatagram.create(playerEntity));
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
