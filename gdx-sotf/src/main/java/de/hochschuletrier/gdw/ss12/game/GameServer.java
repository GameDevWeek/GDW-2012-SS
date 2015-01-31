package de.hochschuletrier.gdw.ss12.game;

import com.badlogic.ashley.core.Entity;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.netcode.core.NetConnection;
import de.hochschuletrier.gdw.commons.netcode.simple.NetServerSimple;
import de.hochschuletrier.gdw.ss12.game.data.NoticeType;
import de.hochschuletrier.gdw.ss12.game.data.Team;
import de.hochschuletrier.gdw.ss12.game.datagrams.NoticeDatagram;
import de.hochschuletrier.gdw.ss12.game.datagrams.PlayerNameDatagram;
import de.hochschuletrier.gdw.ss12.game.systems.network.NetServerSendSystem;
import de.hochschuletrier.gdw.ss12.game.systems.network.NetServerUpdateSystem;
import java.util.ArrayList;
import java.util.List;

public class GameServer extends GameLocal {

    private final NetServerSimple netServer;
    private final List<NetConnection> broadcastList = new ArrayList();

    public GameServer(AssetManagerX assetManager, NetServerSimple netServer) {
        super(assetManager);

        this.netServer = netServer;
    }

    @Override
    public void dispose() {
        super.dispose();
        
        netServer.disconnect();
    }

    @Override
    protected void addSystems() {
        super.addSystems();

        // Remember to set priorities in CustomPooledEngine when creating new system classes
        engine.addSystem(new NetServerUpdateSystem(netServer));
        engine.addSystem(new NetServerSendSystem(netServer));
    }

    @Override
    protected void onPlayerNameChanged(Entity entity) {
        netServer.broadcastReliable(PlayerNameDatagram.create(entity));
    }

    @Override
    public void scheduleNoticeForAll(NoticeType type, float delay, float timeLeft) {
        super.scheduleNoticeForAll(type, delay, timeLeft);
        netServer.broadcastReliable(NoticeDatagram.create(type, delay, type.displayTime));
    }

    @Override
    public void scheduleNoticeForPlayer(NoticeType type, float delay, float timeLeft, Entity entity) {
        if (localPlayer == entity) {
            super.scheduleNoticeForPlayer(type, delay, timeLeft, entity);
        } else {
            for (NetConnection connection : netServer.getConnections()) {
                if (connection.getAttachment() == entity) {
                    connection.sendReliable(NoticeDatagram.create(type, delay, type.displayTime));
                    break;
                }
            }
        }
    }

    @Override
    public void scheduleNoticeForTeam(NoticeType type, float delay, float timeLeft, Team team) {
        super.scheduleNoticeForTeam(type, delay, timeLeft, team);

        // Send to all players of this team
        List<NetConnection> connections = netServer.getConnections();
        if (!connections.isEmpty()) {
            for (NetConnection connection : connections) {
                Entity entity = (Entity) connection.getAttachment();
                if (ComponentMappers.player.get(entity).team == team) {
                    broadcastList.add(connection);
                }
            }

            netServer.broadcastReliable(NoticeDatagram.create(type, delay, type.displayTime), broadcastList);
            broadcastList.clear();
        }
    }
}
