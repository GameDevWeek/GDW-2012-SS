package de.hochschuletrier.gdw.examples.netcode.game.datagrams;

import de.hochschuletrier.gdw.commons.netcode.core.NetMessageType;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageIn;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageOut;

/**
 *
 * @author Santo Pfingsten
 */
public class PlayerDatagram extends NetDatagram {

    private int x, y;
    protected long id;

    public PlayerDatagram() {
        super();
    }

    @Override
    public NetMessageType getMessageType() {
        return NetMessageType.NORMAL;
    }

    @Override
    public void writeToMessage(NetMessageOut message) {
        message.putLong(id);
        message.putInt(x);
        message.putInt(y);
    }

    @Override
    public void readFromMessage(NetMessageIn message) {
        id = message.getLong();
        x = message.getInt();
        y = message.getInt();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public long getID() {
        return id;
    }

    public void setID(long id) {
        this.id = id;
    }
}
