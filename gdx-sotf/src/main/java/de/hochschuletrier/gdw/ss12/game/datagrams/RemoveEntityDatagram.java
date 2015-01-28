package de.hochschuletrier.gdw.ss12.game.datagrams;

import com.badlogic.ashley.core.Entity;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageIn;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageOut;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageType;

/**
 * send from server only
 */
public class RemoveEntityDatagram extends NetDatagram {

    private long netId;

    public static RemoveEntityDatagram create(Entity entity) {
        RemoveEntityDatagram datagram = DatagramFactory.create(RemoveEntityDatagram.class);
        datagram.netId = entity.getId();
        return datagram;
    }

    public long getNetId() {
        return netId;
    }

    @Override
    public NetMessageType getMessageType() {
        return NetMessageType.NORMAL;
    }

    @Override
    public void writeToMessage(NetMessageOut message) {
        message.putLong(netId);
    }

    public @Override
    void readFromMessage(NetMessageIn message) {
        netId = message.getLong();
    }
}
