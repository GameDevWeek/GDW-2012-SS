package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.data.NoticeType;
import de.hochschuletrier.gdw.ss12.game.components.data.Team;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemMapInitializer;
import de.hochschuletrier.gdw.ss12.game.systems.rendering.RenderNoticeSystem;

public class GameStateSystem extends EntitySystem implements SystemGameInitializer, SystemMapInitializer {

    private Game game;
    private Array<Team> teams;

    private final Family family = Family.all(PlayerComponent.class).get();
    private ImmutableArray<Entity> players;
    private Engine engine;

    public GameStateSystem() {
        super(0);
    }

    @Override
    public void addedToEngine(Engine engine) {
        players = engine.getEntitiesFor(family);
        this.engine = engine;
    }

    @Override
    public void removedFromEngine(Engine engine) {
        players = null;
        this.engine = null;
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.game = game;
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
            RenderNoticeSystem noticeSystem = engine.getSystem(RenderNoticeSystem.class);
            if (aliveTeam == null) {
                // Tie (starved at the same time?)
                for (Team team : teams) {
                    noticeSystem.schedule(NoticeType.ROUND_LOST, 0, team);
                }
            } else {
                aliveTeam.wins++;

                if (aliveTeam.wins < Constants.WIN_LIMIT) {
                    // Round won/lost message
                    for (Team team : teams) {
                        if (aliveTeam == team) {
                            noticeSystem.schedule(NoticeType.ROUND_WON, 0, team);
                        } else {
                            noticeSystem.schedule(NoticeType.ROUND_LOST, 0, team);
                        }
                    }
                } else {
                    // Team win/lost message
                    for (Team team : teams) {
                        if (aliveTeam == team) {
                            noticeSystem.schedule(NoticeType.TEAM_WON, 0, team);
                        } else {
                            noticeSystem.schedule(NoticeType.TEAM_LOST, 0, team);
                        }
                        team.wins = 0;
                    }
                }
            }
            game.reset();
        }
    }
}
