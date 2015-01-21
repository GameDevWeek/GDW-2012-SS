package de.hochschuletrier.gdw.ss12.game.datagrams;

import com.badlogic.ashley.core.Entity;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageIn;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageOut;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageType;

/**
 * send from server only
 */
public final class EntitySoundDatagram extends NetDatagram {

    private String sound;
    private long netId;

    public static EntitySoundDatagram create(Entity entity, String sound) {
        EntitySoundDatagram datagram = DatagramFactory.create(EntitySoundDatagram.class);
        datagram.netId = entity.getId();
        datagram.sound = sound;
        return datagram;
    }

    public String getSound() {
        return sound;
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
        message.putString(sound);
    }

    public @Override
    void readFromMessage(NetMessageIn message) {
        netId = message.getLong();
        sound = message.getString();
    }
}
