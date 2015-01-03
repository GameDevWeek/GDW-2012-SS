package de.hochschuletrier.gdw.ss12.game.systems.rendering;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.utils.DrawUtil;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.components.data.Team;
import java.util.ArrayList;

public class RenderScoreHudSystem extends EntitySystem implements SystemGameInitializer {

    private Texture underlay;
    private BitmapFont font;
    private final Rectangle rect = new Rectangle();
    private Game game;

    public RenderScoreHudSystem() {
        super(0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.game = game;
        underlay = assetManager.getTexture("hud_underlay_score");
        font = assetManager.getFont("quartz_50");
        rect.set(25, 0, 50, 50);
    }

    @Override
    public void update(float deltaTime) {
        if (Gdx.input.isKeyPressed(Input.Keys.TAB)) {
            DrawUtil.draw(underlay, 25 - 17, 100 - 30);
            
            // render scores
            ArrayList<Team> teams = new ArrayList(); //fixme: get world teams
            rect.y = 100;
            for (Team team: teams) {
                int kills = 0;
                //killanzeige könnte nicht funktionieren wegen netzwerk
//                for (IPlayer player : team.getPlayers()) {
//                    kills += player.getStatistic().getKills();
//                }
//                String output = team.teamname + " (" + team.getAlivePlayers().size() + "/" + team.getPlayers().size() + ") \n " + kills + " Kills  " + team.wins + " Wins";
//                BitmapFont.TextBounds bounds = font.getBounds(output);
//                float w = bounds.width;
//                float h = bounds.height;
//                if (team != null) {
//                    font.setColor(team.color);
//                    font.draw(DrawUtil.batch, output, rect.getX(), rect.getY() + (rect.getHeight() - h) / 2);
//                }
//                rect.y += 100;
            }
        }
    }
}
