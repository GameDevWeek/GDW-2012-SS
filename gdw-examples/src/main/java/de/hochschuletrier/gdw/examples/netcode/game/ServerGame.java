package de.hochschuletrier.gdw.examples.netcode.game;

import de.hochschuletrier.gdw.commons.netcode.core.NetConnection;
import de.hochschuletrier.gdw.commons.netcode.simple.NetDatagramHandler;
import de.hochschuletrier.gdw.commons.netcode.simple.NetServerSimple;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagramPool;
import de.hochschuletrier.gdw.examples.netcode.game.datagrams.DatagramType;
import de.hochschuletrier.gdw.examples.netcode.game.datagrams.DestroyEntityDatagram;
import de.hochschuletrier.gdw.examples.netcode.game.datagrams.MoveIntentDatagram;
import de.hochschuletrier.gdw.examples.netcode.game.datagrams.PlayerDatagram;
import java.util.ArrayList;

/**
 *
 * @author Santo Pfingsten
 */
public class ServerGame extends BaseGame implements NetDatagramHandler, NetServerSimple.Listener {

    private final NetDatagramPool datagramPool = new NetDatagramPool(DatagramType.MAPPER);

    private final ArrayList<Entity> players = new ArrayList();
    private final NetServerSimple netServer = new NetServerSimple(datagramPool);
    private short entityCount = 0;

    private ServerGame() {
        super("NetPack Game Server Example");
        DatagramType.setPool(datagramPool);

        if (!netServer.start(9090, 10)) {
            System.exit(-1);
        }
    }

    public static ServerGame create() {
        final ServerGame instance = new ServerGame();
        instance.netServer.setHandler(instance);
        instance.netServer.setListener(instance);
        return instance;
    }

    @Override
    public void update() {
        netServer.update();

        for (Entity player : players) {
            player.setChanged((System.currentTimeMillis() - player.getLastMessage()) < 50);
            player.move();
            netServer.broadcastReliable(createPlayerDatagram(player.getID(), player.getX(), player.getY()));
        }
    }

    private PlayerDatagram createPlayerDatagram(long id, int x, int y) {
        PlayerDatagram datagram = (PlayerDatagram) DatagramType.PLAYER.create();
        datagram.setID(id);
        datagram.setPosition(x, y);
        return datagram;
    }

    public void handle(MoveIntentDatagram datagram) {
        Entity player = (Entity) datagram.getConnection().getAttachment();
        player.setMoveDirection(datagram.getX(), datagram.getY());
        player.setLastMessage();
    }

    public static void main(String[] argv) {
        ServerGame game = ServerGame.create();
        game.start();
    }

    @Override
    public boolean onConnect(NetConnection connection) {
        Entity entity = new Entity(entityCount++);
        entity.setPosition(10, 10);
        players.add(entity);
        connection.setAttachment(entity);
        return true;
    }

    @Override
    public void onDisconnect(NetConnection connection) {
        Entity entity = (Entity) connection.getAttachment();
        if (entity != null) {
            players.remove(entity);
            entity.destroy();
            DestroyEntityDatagram datagram = (DestroyEntityDatagram)datagramPool.get(DatagramType.DESTROY_ENTITY.toID());
            datagram.setID(entity.getID());
            netServer.broadcastReliable(datagram);
        }
    }
}
