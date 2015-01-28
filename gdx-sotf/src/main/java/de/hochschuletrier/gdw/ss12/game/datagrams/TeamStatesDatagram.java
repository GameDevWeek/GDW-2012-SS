package de.hochschuletrier.gdw.ss12.game.datagrams;

import com.badlogic.gdx.utils.Array;
import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageIn;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageOut;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageType;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.data.Team;

/**
 * send from server only
 */
public final class TeamStatesDatagram extends NetDatagram {

    private final int[] numberTeamWins = new int[Constants.MAX_TEAMS];
    private final byte[] pizzaCount = new byte[Constants.MAX_TEAMS];

    public static TeamStatesDatagram create(Array<Team> teams) {
        TeamStatesDatagram datagram = DatagramFactory.create(TeamStatesDatagram.class);
        for (Team team : teams) {
            datagram.numberTeamWins[team.id] = team.getWins();
            datagram.pizzaCount[team.id] = (byte) team.getPizzaCount();
        }
        return datagram;
    }

    public int[] getNumberTeamWins() {
        return numberTeamWins;
    }

    public byte[] getPizzaCount() {
        return pizzaCount;
    }

    @Override
    public NetMessageType getMessageType() {
        return NetMessageType.NORMAL;
    }

    @Override
    public void writeToMessage(NetMessageOut message) {
        for (int team = 0; team < Constants.MAX_TEAMS; ++team) {
            message.putInt(numberTeamWins[team]);
        }

        for (int team = 0; team < Constants.MAX_TEAMS; ++team) {
            message.put(pizzaCount[team]);
        }
    }

    public @Override
    void readFromMessage(NetMessageIn message) {
        for (int team = 0; team < Constants.MAX_TEAMS; ++team) {
            numberTeamWins[team] = message.getInt();
        }

        for (int team = 0; team < Constants.MAX_TEAMS; ++team) {
            pizzaCount[team] = message.get();
        }
    }
}
