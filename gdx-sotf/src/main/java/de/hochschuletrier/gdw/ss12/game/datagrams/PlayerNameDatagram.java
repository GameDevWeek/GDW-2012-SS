package de.hochschuletrier.gdw.ss12.game.datagrams;

import com.badlogic.ashley.core.Entity;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageIn;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageOut;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageType;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;

/**
 * send from server only
 */
public final class PlayerNameDatagram extends NetDatagram {

    private long netId;
    private String name;

    public static PlayerNameDatagram create(Entity entity) {
        PlayerNameDatagram datagram = DatagramFactory.create(PlayerNameDatagram.class);
        datagram.netId = entity.getId();
        datagram.name = ComponentMappers.player.get(entity).name;
        return datagram;
    }

    public String getName() {
        return name;
    }

    @Override
    public NetMessageType getMessageType() {
        return NetMessageType.NORMAL;
    }

    @Override
    public void writeToMessage(NetMessageOut message) {
        message.putLong(netId);
        message.putString(name);
    }

    public @Override
    void readFromMessage(NetMessageIn message) {
        netId = message.getLong();
        name = message.getString();
    }
}
