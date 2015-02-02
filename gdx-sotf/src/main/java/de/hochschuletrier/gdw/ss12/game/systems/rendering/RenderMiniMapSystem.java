package de.hochschuletrier.gdw.ss12.game.systems.rendering;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.cameras.orthogonal.LimitedSmoothCamera;
import de.hochschuletrier.gdw.commons.gdx.cameras.orthogonal.ScreenCamera;
import de.hochschuletrier.gdw.commons.gdx.utils.DrawUtil;
import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.ss12.Main;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.data.Team;
import de.hochschuletrier.gdw.ss12.game.components.LightComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.PositionComponent;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemMapInitializer;

public class RenderMiniMapSystem extends EntitySystem implements SystemGameInitializer, SystemMapInitializer, Disposable {

    private ImmutableArray<Entity> lights;
    private ImmutableArray<Entity> players;
    private FrameBuffer fbo;
    public final ScreenCamera renderCamera = new ScreenCamera();
    private final Color filterColor = new Color(1, 1, 1, 0.7f);

    private Game game;
    private int xOffset;
    private int yOffset;
    private float renderScale;
    private RenderMapSystem renderMapSystem;


    @Override
    public void dispose() {
        if (fbo != null) {
            fbo.dispose();
            fbo = null;
        }
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager, PooledEngine engine) {
        this.game = game;
        renderMapSystem = engine.getSystem(RenderMapSystem.class);
        lights = engine.getEntitiesFor(Family.all(LightComponent.class).get());
        players = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
    }

    @Override
    public void initMap(TiledMap map, Array<Team> teams) {
        int mapWidth = map.getTileWidth() * map.getWidth();
        int mapHeight = map.getTileHeight() * map.getHeight();
        float ratio = mapWidth / (float) mapHeight;
        int mapWidthScaled;
        int mapHeightScaled;

        if (mapWidth > mapHeight) {
            renderScale = Constants.MINIMAP_WIDTH / (float) mapWidth;
            mapWidthScaled = Constants.MINIMAP_WIDTH;
            mapHeightScaled = (int) Math.ceil(Constants.MINIMAP_HEIGHT / ratio);
            xOffset = 0;
            yOffset = (Constants.MINIMAP_HEIGHT - mapHeightScaled) / 2;
        } else {
            renderScale = Constants.MINIMAP_HEIGHT / (float) mapHeight;
            mapHeightScaled = Constants.MINIMAP_HEIGHT;
            mapWidthScaled = (int) Math.ceil(Constants.MINIMAP_WIDTH * ratio);
            xOffset = (Constants.MINIMAP_WIDTH - mapWidthScaled) / 2;
            yOffset = 0;
        }

        if (fbo != null) {
            fbo.dispose();
        }

        fbo = new FrameBuffer(Format.RGB888, mapWidthScaled, mapHeightScaled, false);
        fbo.begin();
        renderCamera.resize(mapWidth, mapHeight);
        renderCamera.bind();
        DrawUtil.clear();
        DrawUtil.batch.begin();
        renderMapSystem.render();
        DrawUtil.batch.end();
        fbo.end();
    }

    @Override
    public void update(float deltaTime) {
        Main.getInstance().screenCamera.bind();
        float x = xOffset + Gdx.graphics.getWidth() - Constants.MINIMAP_OFFSET_X - Constants.MINIMAP_WIDTH;
        float y = yOffset + Constants.MINIMAP_OFFSET_Y;
        DrawUtil.setColor(filterColor);
        DrawUtil.draw(fbo.getColorBufferTexture(), x, y);
        Entity localPlayerEntity = game.getLocalPlayer();
        PlayerComponent localPlayer = ComponentMappers.player.get(localPlayerEntity);
        drawPlayers(x, y, localPlayer);
        drawCamera(x, y, localPlayer);
        DrawUtil.resetColor();
    }

    private void drawPlayers(float mapX, float mapY, PlayerComponent localPlayer) {
        int team = localPlayer.team.id;

        if (!localPlayer.isHalucinating()) {
            for (Entity entity : players) {
                PlayerComponent player = ComponentMappers.player.get(entity);
                if (!player.isDead()) {
                    PositionComponent position = ComponentMappers.position.get(entity);
                    if (player.team.id == team || teamLightsInSight(position, team)) {
                        DrawUtil.setColor(player.team.color);

                        final float x = mapX + (position.x - localPlayer.radius) * renderScale;
                        final float y = mapY + (position.y - localPlayer.radius) * renderScale;
                        DrawUtil.fillRect(x, y, localPlayer.radius * 2 * renderScale, localPlayer.radius * 2 * renderScale);
                    }
                }
            }
        }
    }

    private void drawCamera(float mapX, float mapY, PlayerComponent localPlayer) {
        LimitedSmoothCamera camera = game.getCamera();
        final float x = mapX + camera.getLeftOffset() * renderScale;
        final float y = mapY + camera.getTopOffset() * renderScale;
        DrawUtil.setColor(localPlayer.team.color);
        DrawUtil.drawRect(x, y, Gdx.graphics.getWidth() * renderScale, Gdx.graphics.getHeight() * renderScale);
    }

    private boolean teamLightsInSight(PositionComponent from, int team) {
        for (Entity entity : lights) {
            LightComponent light = ComponentMappers.light.get(entity);
            PositionComponent position = ComponentMappers.position.get(entity);
            if (light.team.id == team && Vector2.dst(from.x, from.y, position.x, position.y) <= light.radius) {
                return true;
            }
        }
        return false;
    }
}
