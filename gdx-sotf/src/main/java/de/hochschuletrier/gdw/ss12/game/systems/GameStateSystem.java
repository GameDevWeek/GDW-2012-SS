package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.EntitySystem;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;

public class GameStateSystem extends EntitySystem implements SystemGameInitializer {

    private Game game;

    public GameStateSystem() {
        super(0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.game = game;
    }

    @Override
    public void update(float deltaTime) {

        // Gucken, ob ein Team tot ist und wenn, dann der Welt Bescheid
        // geben, dass eine neue Runde beginnen muss
//        int numberAliveTeams = 0;
//        Team aliveTeam = null;
//        Team[] teams = world.getTeams();
//        for (int i = 0; i < teams.length; i++) {
//            if (!teams[i].getAlivePlayers().isEmpty()) {
//                numberAliveTeams++;
//                aliveTeam = teams[i];
//            }
//        }
//        if (numberAliveTeams <= 1) {
//            //RESET
//            if (Constants.START_NEW_ROUND) {
//                if (aliveTeam != null) {
//                    aliveTeam.setWins(aliveTeam.getWins() + 1);
//
//                    // Spiel gewonnen Message
//                    if (aliveTeam.getWins() >= Constants.WIN_LIMIT) {
//                        for (int i = 0; i < GameWorld.getMaxTeams(); i++) {
//                            Team t = world.getTeam(i);
//                            if (t.wins >= Constants.WIN_LIMIT) {
//                                GameEventManager.fireGameEvent(GameEventManager.TEAM_WON, 0, t.getPlayers());
//                            } else {
//                                GameEventManager.fireGameEvent(GameEventManager.TEAM_LOST, 0, t.getPlayers());
//                            }
//                            t.wins = 0;
//                        }
//                    } // Eine Runde gewonnen Message
//                    else {
//                        for (int i = 0; i < GameWorld.getMaxTeams(); i++) {
//                            Team t = world.getTeam(i);
//                            if (!t.getAlivePlayers().isEmpty()) {
//                                GameEventManager.fireGameEvent(GameEventManager.ROUND_WON, 0, t.getPlayers());
//                            } else {
//                                GameEventManager.fireGameEvent(GameEventManager.ROUND_LOST, 0, t.getPlayers());
//                            }
//                        }
//                    }
//                }
//                resetGame();
//            }
//        }
    }
}
