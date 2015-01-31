package de.hochschuletrier.gdw.ss12.game.datagrams;

import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageIn;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageOut;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageType;
import de.hochschuletrier.gdw.ss12.game.data.Team;

/**
 * send from server only
 */
public final class TeamStateDatagram extends NetDatagram {

    private byte teamId;
    private byte numberTeamWins;
    private byte pizzaCount;

    public static TeamStateDatagram create(Team team) {
        TeamStateDatagram datagram = DatagramFactory.create(TeamStateDatagram.class);
        datagram.teamId = (byte) team.id;
        datagram.numberTeamWins = (byte) team.getWins();
        datagram.pizzaCount = (byte) team.getPizzaCount();
        return datagram;
    }

    public byte getTeamId() {
        return teamId;
    }

    public byte getNumberTeamWins() {
        return numberTeamWins;
    }

    public byte getPizzaCount() {
        return pizzaCount;
    }

    @Override
    public NetMessageType getMessageType() {
        return NetMessageType.NORMAL;
    }

    @Override
    public void writeToMessage(NetMessageOut message) {
        message.put(teamId);
        message.put(numberTeamWins);
        message.put(pizzaCount);
    }

    public @Override
    void readFromMessage(NetMessageIn message) {
        teamId = message.get();
        numberTeamWins = message.get();
        pizzaCount = message.get();
    }
}
