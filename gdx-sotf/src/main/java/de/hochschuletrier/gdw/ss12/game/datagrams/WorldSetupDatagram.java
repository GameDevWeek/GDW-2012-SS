package de.hochschuletrier.gdw.ss12.game.datagrams;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageIn;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageOut;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageType;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.components.PositionComponent;

/**
 * send from server only
 */
public final class WorldSetupDatagram extends NetDatagram {

    private long netId;
    private String mapname = "";
    private boolean paused;
    private final Vector2 startPosition = new Vector2();

    public static WorldSetupDatagram create(String mapname, boolean paused, Entity entity) {
        WorldSetupDatagram datagram = DatagramFactory.create(WorldSetupDatagram.class);
        datagram.netId = entity.getId();
        datagram.mapname = mapname;
        datagram.paused = paused;
        PositionComponent position = ComponentMappers.position.get(entity);
        datagram.startPosition.set(position.x, position.y);
        return datagram;
    }

    public long getNetId() {
        return netId;
    }

    public String getMapname() {
        return mapname;
    }

    public boolean isPaused() {
        return paused;
    }

    public Vector2 getStartPosition() {
        return startPosition;
    }

    @Override
    public NetMessageType getMessageType() {
        return NetMessageType.NORMAL;
    }

    @Override
    public void writeToMessage(NetMessageOut message) {
        message.putLong(netId);
        message.putString(mapname);
        message.putBool(paused);
        message.putFloat(startPosition.x);
        message.putFloat(startPosition.y);
    }

    public @Override
    void readFromMessage(NetMessageIn message) {
        netId = message.getLong();
        mapname = message.getString();
        paused = message.getBool();
        startPosition.x = message.getFloat();
        startPosition.y = message.getFloat();
    }
}
