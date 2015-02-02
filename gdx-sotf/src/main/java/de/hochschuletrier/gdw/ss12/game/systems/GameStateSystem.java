package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.data.NoticeType;
import de.hochschuletrier.gdw.ss12.game.data.Team;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemMapInitializer;

public class GameStateSystem extends EntitySystem implements SystemGameInitializer, SystemMapInitializer {

    private Game game;
    private Array<Team> teams;

    private final Family family = Family.all(PlayerComponent.class).get();
    private ImmutableArray<Entity> players;

    @Override
    public void initGame(Game game, AssetManagerX assetManager, PooledEngine engine) {
        this.game = game;
        players = engine.getEntitiesFor(family);
    }

    @Override
    public void initMap(TiledMap map, Array<Team> teams) {
        this.teams = teams;
    }

    @Override
    public void update(float deltaTime) {
        // Update alive players
        for (Team team : teams) {
            team.alivePlayers = 0;
        }

        for (Entity entity : players) {
            PlayerComponent player = ComponentMappers.player.get(entity);
            if (!player.isDead()) {
                player.team.alivePlayers++;
            }
        }

        // Check how many teams are still alive
        int numberAliveTeams = 0;
        Team aliveTeam = null;
        for (Team team : teams) {
            if (team.alivePlayers > 0) {
                numberAliveTeams++;
                aliveTeam = team;
            }
        }

        if (numberAliveTeams <= 1) {
            if (aliveTeam == null) {
                // Tie (starved at the same time?)
                for (Team team : teams) {
                    game.scheduleNoticeForTeam(NoticeType.ROUND_LOST, 0, -1, team);
                }
            } else {
                aliveTeam.setWins(aliveTeam.getWins() + 1);

                if (aliveTeam.getWins() < Constants.WIN_LIMIT) {
                    // Round won/lost message
                    for (Team team : teams) {
                        if (aliveTeam == team) {
                            game.scheduleNoticeForTeam(NoticeType.ROUND_WON, 0, -1, team);
                        } else {
                            game.scheduleNoticeForTeam(NoticeType.ROUND_LOST, 0, -1, team);
                        }
                    }
                } else {
                    // Team win/lost message
                    for (Team team : teams) {
                        if (aliveTeam == team) {
                            game.scheduleNoticeForTeam(NoticeType.TEAM_WON, 0, -1, team);
                        } else {
                            game.scheduleNoticeForTeam(NoticeType.TEAM_LOST, 0, -1, team);
                        }
                        team.setWins(0);
                    }
                }
            }
        }
    }
}
