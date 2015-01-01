package de.hochschuletrier.gdw.ss14.game.datagrams;

import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageIn;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageOut;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageType;

public final class WorldStateDatagram extends NetDatagram {

    private String[] playerNames;
    private int[] numberTeamWins;
    private byte[] pizzaCount;

//    public void setup(GameWorld world) {
//        playerNames = new String[GameWorld.getMaxPlayers()];
//        for (int playerId = 0; playerId < world.getPlayers().size(); ++playerId) {
//            IPlayer p = world.getPlayerByID(playerId);
//            playerNames[playerId] = p.getName();
//        }
//
//        numberTeamWins = new int[GameWorld.getMaxTeams()];
//        pizzaCount = new byte[GameWorld.getMaxTeams()];
//        for (int i = 0; i < world.getNumberTeams(); ++i) {
//            ITeam team = world.getTeam(i);
//            numberTeamWins[i] = team.getWins();
//            pizzaCount[i] = (byte) team.getPizzaCount();
//        }
//    }

    public String[] getPlayerNames() {
        return playerNames;
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
        message.putInt(playerNames.length);
        for (String playerName : playerNames) {
            message.putString(playerName);
        }

        message.putInt(numberTeamWins.length);
        for (int team = 0; team < numberTeamWins.length; ++team) {
            message.putInt(numberTeamWins[team]);
        }

        message.putInt(pizzaCount.length);
        for (int team = 0; team < pizzaCount.length; ++team) {
            message.put(pizzaCount[team]);
        }
    }

    public @Override
    void readFromMessage(NetMessageIn message) {
        // read player names
        int maxPlayers = message.getInt();
        playerNames = new String[maxPlayers];
        for (int player = 0; player < maxPlayers; ++player) {
            playerNames[player] = message.getString();
        }

        // read number team wins
        int maxTeamWins = message.getInt();
        numberTeamWins = new int[maxTeamWins];
        for (int team = 0; team < maxTeamWins; ++team) {
            numberTeamWins[team] = message.getInt();
        }

        int maxPizzaCounts = message.getInt();
        pizzaCount = new byte[maxPizzaCounts];
        for (int team = 0; team < maxPizzaCounts; ++team) {
            pizzaCount[team] = message.get();
        }
    }
}
