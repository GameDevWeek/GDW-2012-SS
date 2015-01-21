package de.hochschuletrier.gdw.ss12.game.datagrams;

import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageIn;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageOut;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageType;

/**
 * send from client only
 */
public final class ConnectDatagram extends NetDatagram {

    private String playerName;

    public static ConnectDatagram create(String playerName) {
        ConnectDatagram datagram = DatagramFactory.create(ConnectDatagram.class);
        datagram.playerName = playerName;
        return datagram;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public NetMessageType getMessageType() {
        return NetMessageType.NORMAL;
    }

    @Override
    public void writeToMessage(NetMessageOut message) {
        message.putString(playerName);
    }

    public @Override
    void readFromMessage(NetMessageIn message) {
        playerName = message.getString();
    }
}
