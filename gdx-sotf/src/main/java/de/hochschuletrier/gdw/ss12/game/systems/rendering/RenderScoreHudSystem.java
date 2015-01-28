package de.hochschuletrier.gdw.ss12.game.systems.rendering;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Array;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.utils.DrawUtil;
import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.data.Team;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemMapInitializer;

public class RenderScoreHudSystem extends EntitySystem implements SystemGameInitializer, SystemMapInitializer {

    private BitmapFont font;
    private Array<Team> teams;
    private ImmutableArray<Entity> entities;

    public RenderScoreHudSystem() {
        super(0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        font = assetManager.getFont("quartz_50");
    }

    @Override
    public void initMap(TiledMap map, Array<Team> teams) {
        this.teams = teams;
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {
        entities = null;
    }

    @Override
    public void update(float deltaTime) {
        if (Gdx.input.isKeyPressed(Input.Keys.TAB)) {

            int y = 100;
            for (Team team : teams) {
                if (team.numPlayers > 0) {
                    int kills = 0;
                    for (Entity entity : entities) {
                        PlayerComponent player = ComponentMappers.player.get(entity);
                        if (player.team == team) {
                            kills += player.statistic.kills;
                        }
                    }

                    String output = String.format("%s (%d/%d)\n %d Kills %d Wins", team.name, team.alivePlayers, team.numPlayers, kills, team.getWins());
                    font.setColor(team.color);
                    font.drawMultiLine(DrawUtil.batch, output, 25, y);
                    y += 100;
                }
            }
        }
    }
}
