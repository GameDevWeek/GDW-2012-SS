package de.hochschuletrier.gdw.ss12.game.datagrams;

import com.badlogic.gdx.math.Vector2;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageIn;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageOut;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageType;

public final class WorldSoundDatagram extends NetDatagram {

    private final Vector2 position = new Vector2();
    private String sound;

    public void setup(String sound, Vector2 position) {
        this.sound = sound;
        this.position.set(position);
    }

    public String getSound() {
        return sound;
    }

    public Vector2 getPosition() {
        return position;
    }

    @Override
    public NetMessageType getMessageType() {
        return NetMessageType.NORMAL;
    }

    @Override
    public void writeToMessage(NetMessageOut message) {
        message.putFloat(position.x);
        message.putFloat(position.y);
        message.putString(sound);
    }

    public @Override
    void readFromMessage(NetMessageIn message) {
        position.x = message.getFloat();
        position.y = message.getFloat();
        sound = message.getString();
    }
}
