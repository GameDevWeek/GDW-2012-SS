package de.hochschuletrier.gdw.ss14.game.systems.network;

import com.badlogic.ashley.core.EntitySystem;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.netcode.core.NetConnection;
import de.hochschuletrier.gdw.commons.netcode.simple.NetDatagramHandler;
import de.hochschuletrier.gdw.commons.netcode.simple.NetServerSimple;
import de.hochschuletrier.gdw.ss14.game.Game;
import de.hochschuletrier.gdw.ss14.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss14.game.datagrams.ConnectDatagram;
import de.hochschuletrier.gdw.ss14.game.datagrams.PlayerControlDatagram;

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
//        Entity player = GameWorld.getInstance().getFirstUnconnectedPlayer();
//        if (player == null) {
//            // no free players available
//            connection.shutdown();
//            return false;
//        }
//
//        player.onConnect();
//        connection.setAttachment(player);
        return true;
    }

    @Override
    public void onDisconnect(NetConnection connection) {
//        Entity player = (Entity) connection.getAttachment();
//        if (player != null) {
//            player.onDisconnect();
//        }
    }

    public void handle(ConnectDatagram datagram) {
        NetConnection connection = datagram.getConnection();
//        if (!connection.isAccepted() && connection.isConnected()) {
//            connection.setAccepted(true);
//            Entity player = (Entity) connection.getAttachment();
//            player.setName(datagram.getPlayerName());
//            GameWorld.getInstance().sendWorldSetup(player);
//        }
    }

    // fixme: handle methods for all datagrams
//    public void handle(PlayerControlDatagram datagram) {
//        NetConnection connection = datagram.getConnection();
//        Entity player = (Entity) connection.getAttachment();
//        if (datagram.getID() == player.getID()) {
//            player.getPosition().set(datagram.getPosition());
//            player.setViewingAngle(datagram.getViewingAngle());
//        }
//    }
//
//    public void handle(TeleportDatagram datagram) {
//        NetConnection connection = datagram.getConnection();
//        Entity player = (Entity) connection.getAttachment();
//        if (datagram.getID() == player.getId()) {
//            Sound sound = AssetLoader.getInstance().getSound("player_swallow");
//            GameWorld.playSound(sound, datagram.getFrom());
//            GameWorld.playSound(sound, datagram.getTo());
//
//            send(datagram);
//        }
//    }
//
//    public void handle(NetUseItemDatagram datagram) {
//        NetConnection connection = datagram.getConnection();
//        Entity player = (Entity) connection.getAttachment();
//        player.use(datagram.getID());
//    }
}
