package de.hochschuletrier.gdw.examples.netcode.pingpong.datagrams;

import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageIn;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageOut;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageType;

/**
 *
 * @author Santo Pfingsten
 */
public class ChatDatagram extends NetDatagram {

    private String text;
    private long timestamp;
    protected long id;

    @Override
    public void reset() {
        super.reset();

        text = null;
        timestamp = 0;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public NetMessageType getMessageType() {
        return NetMessageType.NORMAL;
    }

    @Override
    public void writeToMessage(NetMessageOut message) {
        message.putString(text);
        message.putLong(timestamp);
    }

    @Override
    public void readFromMessage(NetMessageIn message) {
        text = message.getString();
        timestamp = message.getLong();
    }

    public long getID() {
        return id;
    }
}
