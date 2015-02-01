package de.hochschuletrier.gdw.ss12.game.datagrams;

import de.hochschuletrier.gdw.ss12.game.data.PlayerSetup;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageIn;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageOut;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageType;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;

/**
 * send from server only
 */
public final class WorldSetupDatagram extends NetDatagram {

    private long netId;
    private String mapName = "";
    private final PlayerSetup[] players = new PlayerSetup[Constants.MAX_PLAYERS];
    private byte numPlayers;

    public WorldSetupDatagram() {
        for (int i = 0; i < players.length; i++) {
            players[i] = new PlayerSetup();
        }
    }

    public static WorldSetupDatagram create(Game game, Entity playerEntity) {
        WorldSetupDatagram datagram = DatagramFactory.create(WorldSetupDatagram.class);
        datagram.netId = playerEntity.getId();
        datagram.mapName = game.getMapName();
        datagram.numPlayers = 0;

        for (Entity entity : game.getPlayerEntities()) {
            PlayerSetup setup = datagram.players[datagram.numPlayers++];
            setup.netId = entity.getId();

            PlayerComponent player = ComponentMappers.player.get(entity);
            setup.team = (byte) player.team.id;
            setup.start.set(player.startPosition);
            setup.name = player.name;
        }
        return datagram;
    }

    public long getNetId() {
        return netId;
    }

    public String getMapName() {
        return mapName;
    }

    public byte getNumPlayers() {
        return numPlayers;
    }

    public PlayerSetup[] getPlayers() {
        return players;
    }

    @Override
    public NetMessageType getMessageType() {
        return NetMessageType.NORMAL;
    }

    @Override
    public void writeToMessage(NetMessageOut message) {
        message.putLong(netId);
        message.putString(mapName);
        message.put(numPlayers);

        for (int i = 0; i < numPlayers; i++) {
            PlayerSetup player = players[i];
            message.putLong(player.netId);
            message.put(player.team);
            message.putFloat(player.start.x);
            message.putFloat(player.start.y);
            message.putString(player.name);
        }
    }

    public @Override
    void readFromMessage(NetMessageIn message) {
        netId = message.getLong();
        mapName = message.getString();
        numPlayers = message.get();

        for (int i = 0; i < numPlayers; i++) {
            PlayerSetup player = players[i];
            player.netId = message.getLong();
            player.team = message.get();
            player.start.x = message.getFloat();
            player.start.y = message.getFloat();
            player.name = message.getString();
        }
    }
}
