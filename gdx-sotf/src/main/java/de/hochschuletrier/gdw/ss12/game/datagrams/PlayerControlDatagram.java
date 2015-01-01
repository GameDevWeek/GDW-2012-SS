package de.hochschuletrier.gdw.ss12.game.datagrams;

import com.badlogic.gdx.math.Vector2;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageIn;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageOut;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageType;

public final class PlayerControlDatagram extends NetDatagram {

    private Vector2 position = new Vector2();
    private float viewingAngle;
    private long id;

//    public void setup(IPlayer player) {
//        id = e.getId(); // fixme: netid ? and: write/read
//        position.set(player.getPosition());
//        viewingAngle = player.getViewingAngle();
//    }

    public Vector2 getPosition() {
        return position;
    }

    public float getViewingAngle() {
        return viewingAngle;
    }

    @Override
    public NetMessageType getMessageType() {
        return NetMessageType.NORMAL;
    }

    @Override
    public void writeToMessage(NetMessageOut message) {
        message.putFloat(position.x);
        message.putFloat(position.y);
        message.putFloat(viewingAngle);
    }

    public @Override
    void readFromMessage(NetMessageIn message) {
        position.x = message.getFloat();
        position.y = message.getFloat();
        viewingAngle = message.getFloat();
    }
}