package de.hochschuletrier.gdw.ss12.game.systems.rendering;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.utils.DrawUtil;
import de.hochschuletrier.gdw.ss12.Main;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.components.data.Team;
import de.hochschuletrier.gdw.ss12.game.components.LightComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.PositionComponent;

public class RenderShadowMapSystem extends EntitySystem implements SystemGameInitializer {

    private ImmutableArray<Entity> lights;
    private Texture alphaMapPlayer;
    private FrameBuffer fbo;

    private ShaderProgram defaultShader;
    private ShaderProgram finalShader;

    public final float ambientIntensity = 1.0f;
    public final Vector3 ambientColor = new Vector3(0.0f, 0.0f, 0.0f);
    private Game game;
    private boolean activeFrame;

    public RenderShadowMapSystem() {
        super(0);
    }

    @Override
    public void addedToEngine(Engine engine) {
        lights = engine.getEntitiesFor(Family.all(LightComponent.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {
        lights = null;
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.game = game;
        String vertexShader = Gdx.files.internal("data/shaders/vertexShader.glsl").readString();
        String defaultPixelShader = Gdx.files.internal("data/shaders/defaultPixelShader.glsl").readString();
        String finalPixelShader = Gdx.files.internal("data/shaders/pixelShader.glsl").readString();

        ShaderProgram.pedantic = false;
        defaultShader = new ShaderProgram(vertexShader, defaultPixelShader);
        finalShader = new ShaderProgram(vertexShader, finalPixelShader);

        finalShader.begin();
        finalShader.setUniformi("u_lightmap", 1);
        finalShader.setUniformf("ambientColor", ambientColor.x, ambientColor.y,
                ambientColor.z, ambientIntensity);
        finalShader.end();

        alphaMapPlayer = assetManager.getTexture("playerAlphaMap");

        Main.getInstance().addScreenListener(this::resize);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void resize(final int width, final int height) {
        fbo = new FrameBuffer(Format.RGBA8888, width, height, false);

        finalShader.begin();
        finalShader.setUniformf("resolution", width, height);
        finalShader.end();
    }

    @Override
    public void update(float deltaTime) {
        Entity localPlayer = game.getLocalPlayer();
        PlayerComponent player = ComponentMappers.player.get(localPlayer);
        Team team = player.team;
        activeFrame = !player.isHalucinating();
        if (activeFrame) {
            DrawUtil.batch.end();
            //draw the light to the FBO
            fbo.begin();
            game.getCamera().bind();
            DrawUtil.batch.setShader(defaultShader);
            DrawUtil.clear();
            DrawUtil.batch.begin();
            for (Entity e : lights) {
                drawLight(e, team);
            }
            if (player.isDead()) {
                PositionComponent position = ComponentMappers.position.get(localPlayer);
                drawLight(position.x, position.y, Constants.PLAYER_DEFAULT_SIGHTDISTANCE);
            }
            DrawUtil.batch.end();
            fbo.end();

            //draw the actual scene
            DrawUtil.clear();
            game.getCamera().bind();
            DrawUtil.batch.setShader(finalShader);
            DrawUtil.batch.begin();
            fbo.getColorBufferTexture().bind(1);
            alphaMapPlayer.bind(0);
        }
    }

    private void drawLight(Entity entity, Team team) {
        LightComponent light = ComponentMappers.light.get(entity);
        if (light.team == team && light.radius != 0) {
            PositionComponent position = ComponentMappers.position.get(entity);
            drawLight(position.x, position.y, light.radius);
        }
    }

    private void drawLight(float x, float y, float lightRadius) {
        DrawUtil.draw(alphaMapPlayer, x + 0.5f - lightRadius, y + 0.5f - lightRadius, lightRadius * 2, lightRadius * 2);
    }

    void finish() {
        if (activeFrame) {
            DrawUtil.batch.end();
            DrawUtil.batch.begin();
            game.getCamera().bind();
            DrawUtil.batch.setShader(defaultShader);
        }
    }
}
