package de.hochschuletrier.gdw.ss14.game.datagrams;

import com.badlogic.gdx.math.Vector2;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageIn;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageOut;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageType;

public final class WorldSetupDatagram extends NetDatagram {

    private String mapname = "";
    private boolean paused;
    private final Vector2 startPosition = new Vector2();
    private long id;

    public void setup(String mapname, boolean paused, int playerId, Vector2 startPosition) {
        id = playerId; // fixme: netid ? and: write/read
        
        this.mapname = mapname;
        this.paused = paused;
        this.startPosition.set(startPosition);
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
        message.putString(mapname);
        message.putBool(paused);
        message.putFloat(startPosition.x);
        message.putFloat(startPosition.y);
    }

    public @Override
    void readFromMessage(NetMessageIn message) {
        mapname = message.getString();
        paused = message.getBool();
        startPosition.x = message.getFloat();
        startPosition.y = message.getFloat();
    }
}
