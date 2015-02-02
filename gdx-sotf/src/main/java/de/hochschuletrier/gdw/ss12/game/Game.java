package de.hochschuletrier.gdw.ss12.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import de.hochschuletrier.gdw.commons.devcon.cvar.CVarBool;
import de.hochschuletrier.gdw.commons.gdx.assets.AnimationExtended;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.cameras.orthogonal.LimitedSmoothCamera;
import de.hochschuletrier.gdw.commons.gdx.input.InputForwarder;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixBodyDef;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixFixtureDef;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixBodyComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixModifierComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.systems.PhysixSystem;
import de.hochschuletrier.gdw.commons.gdx.audio.SoundEmitter;
import de.hochschuletrier.gdw.commons.gdx.audio.SoundInstance;
import de.hochschuletrier.gdw.commons.gdx.input.hotkey.Hotkey;
import de.hochschuletrier.gdw.commons.gdx.physix.systems.PhysixDebugRenderSystem;
import de.hochschuletrier.gdw.commons.tiled.Layer;
import de.hochschuletrier.gdw.commons.tiled.TileInfo;
import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.commons.tiled.utils.RectangleGenerator;
import de.hochschuletrier.gdw.commons.utils.Rectangle;
import de.hochschuletrier.gdw.ss12.Main;
import de.hochschuletrier.gdw.ss12.game.data.PlayerState;
import de.hochschuletrier.gdw.ss12.game.data.Team;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.PositionComponent;
import de.hochschuletrier.gdw.ss12.game.components.SoundEmitterComponent;
import de.hochschuletrier.gdw.ss12.game.components.TriggerComponent;
import de.hochschuletrier.gdw.ss12.game.data.NoticeType;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemMapInitializer;
import de.hochschuletrier.gdw.ss12.game.systems.*;
import de.hochschuletrier.gdw.ss12.game.systems.input.*;
import de.hochschuletrier.gdw.ss12.game.systems.rendering.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.function.Consumer;

public abstract class Game {

    protected final CustomPooledEngine engine = new CustomPooledEngine();
    protected ImmutableArray<Entity> playerEntities = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
    protected ImmutableArray<Entity> entitiesToRemove = engine.getEntitiesFor(Family.exclude(PlayerComponent.class, TriggerComponent.class).get());

    protected final LimitedSmoothCamera camera = new LimitedSmoothCamera();
    protected TiledMap map;
    protected Entity localPlayer;
    protected final Array<Team> teams = new Array();
    protected AssetManagerX assetManager;
    private InputForwarder inputForwarder = new InputForwarder();
    private final CVarBool physixDebug = new CVarBool("physix_debug", !Main.IS_RELEASE, 0, "Draw physix debug");
    private final Hotkey togglePhysixDebug = new Hotkey(() -> physixDebug.toggle(false), Input.Keys.F1);

    public Game(AssetManagerX assetManager) {
        this.assetManager = assetManager;

        for (int i = 0; i < Constants.TEAM_COLOR_TABLE.length; i++) {
            final Team team = new Team(i, "Team " + i, Constants.TEAM_COLOR_TABLE[i]);
            HashMap<PlayerState, AnimationExtended> animations = team.animations;
            animations.put(PlayerState.PAUSED, assetManager.getAnimation("player_team" + i));
            animations.put(PlayerState.ALIVE, assetManager.getAnimation("player_team" + i));
            animations.put(PlayerState.DEAD, assetManager.getAnimation("player_wiggle"));
            animations.put(PlayerState.HALUCINATING, assetManager.getAnimation("player_halucinating"));
            teams.add(team);
        }
    }

    protected void init() {
        addSystems();
        initSystems();
        inputForwarder.set(engine.getSystem(KeyboardInputSystem.class));
        Main.getInstance().console.register(physixDebug);
        physixDebug.addListener((CVar) -> engine.getSystem(PhysixDebugRenderSystem.class).setProcessing(physixDebug.get()));
        
        // If this is a build jar file, disable hotkeys
        if (!Main.IS_RELEASE) {
            togglePhysixDebug.register();
        }
    }

    public void dispose() {
        //fixme: call dispose on all systems implementing Disposable
        SoundEmitter.disposeGlobal();
        
        Main.getInstance().console.unregister(physixDebug);
        togglePhysixDebug.unregister();
    }

    public LimitedSmoothCamera getCamera() {
        return camera;
    }

    public ImmutableArray<Entity> getPlayerEntities() {
        return playerEntities;
    }

    public Entity getLocalPlayer() {
        return localPlayer;
    }

    public Array<Team> getTeams() {
        return teams;
    }

    public String getMapName() {
        return map.getFilename();
    }

    protected void addSystems() {
        // Remember to set priorities in CustomPooledEngine when creating new system classes
        engine.addSystem(new KeyboardInputSystem());
        engine.addSystem(new InputSystem());
        engine.addSystem(new UpdateSoundEmitterSystem());

        engine.addSystem(new PhysixSystem(
                Constants.BOX2D_SCALE, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS
        ));
        engine.addSystem(new EntitySpawnSystem());
        engine.addSystem(new UpdatePlayerEffectsSystem());
        engine.addSystem(new UpdateCameraSystem(camera));
        engine.addSystem(new UpdatePositionSystem());

        engine.addSystem(new RenderShadowMapSystem());
        engine.addSystem(new RenderMapSystem());
        engine.addSystem(new RenderParticleEffectSystem());
        engine.addSystem(new RenderItemTextureSystem());
        engine.addSystem(new RenderItemAnimationSystem());
        engine.addSystem(new RenderPlayerSystem());
        engine.addSystem(new RenderShadowMapCleanupSystem());
        engine.addSystem(new RenderMiniMapSystem());
        engine.addSystem(new RenderPowerupHudSystem());
        engine.addSystem(new RenderDropableHudSystem());
        engine.addSystem(new RenderPizzaHudSystem());
        engine.addSystem(new RenderScoreHudSystem());
        engine.addSystem(new RenderNoticeSystem());
        engine.addSystem(new PhysixDebugRenderSystem());
    }

    private void initSystems() {
        ImmutableArray<EntitySystem> systems = engine.getSystems();
        for (int i = 0; i < systems.size(); i++) {
            EntitySystem system = systems.get(i);
            if (system instanceof SystemGameInitializer) {
                SystemGameInitializer initializer = (SystemGameInitializer) system;
                initializer.initGame(this, assetManager, engine);
            }
        }
    }

    public InputProcessor getInputProcessor() {
        return inputForwarder;
    }

    public void scheduleNoticeForAll(NoticeType type, float delay, float timeLeft) {
        engine.getSystem(RenderNoticeSystem.class).schedule(type, delay, timeLeft);
    }

    public void scheduleNoticeForPlayer(NoticeType type, float delay, float timeLeft, Entity entity) {
        if (localPlayer == entity) {
            engine.getSystem(RenderNoticeSystem.class).schedule(type, delay, timeLeft);
        }
    }

    public void scheduleNoticeForTeam(NoticeType type, float delay, float timeLeft, Team team) {
        if (ComponentMappers.player.get(localPlayer).team == team) {
            engine.getSystem(RenderNoticeSystem.class).schedule(type, delay, timeLeft);
        }
    }

    protected void systemMapInit() {
        for (EntitySystem system : engine.getSystems()) {
            if (system instanceof SystemMapInitializer) {
                SystemMapInitializer initializer = (SystemMapInitializer) system;
                initializer.initMap(map, teams);
            }
        }
    }

    public void setLocalPlayer(Entity entity) {
        localPlayer = entity;
        if (localPlayer == null) {
            throw new RuntimeException("Null player");
        }

        updateCameraForced();
    }

    public void setPlayerName(Entity entity, String name) {
        ComponentMappers.player.get(entity).name = name;
    }

    public void updateCameraForced() {
        engine.getSystem(UpdatePositionSystem.class).update(0);
        engine.getSystem(UpdateCameraSystem.class).forceCameraUpdate();
    }

    public SoundInstance playAnouncerSound(String name) {
        return playEntitySound(name, localPlayer, false);
    }

    public SoundInstance playGlobalSound(String name, float x, float y, boolean loop) {
        return SoundEmitter.playGlobal(assetManager.getSound(name), loop, x, y, 0);
    }

    public SoundInstance playEntitySound(String name, Entity entity, boolean loop) {
        SoundEmitterComponent soundEmitter = ComponentMappers.soundEmitter.get(entity);
        if (soundEmitter != null) {
            return soundEmitter.emitter.play(assetManager.getSound(name), loop);
        } else {
            PositionComponent position = ComponentMappers.position.get(entity);
            if (position != null) {
                return playGlobalSound(name, position.x, position.y, loop);
            }
        }
        return null;
    }

    final Comparator<Team> acquireTeamComparator = new Comparator<Team>() {
        @Override
        public int compare(Team a, Team b) {
            return getWeight(a) - getWeight(b);
        }

        private int getWeight(Team team) {
            int weight = team.numPlayers == 0 ? 100 : 0;
            if (team.isFull()) {
                return weight + 50;
            }
            return weight - (team.numPlayers - team.numConnectedPlayers);
        }
    };

    public void loadMap(String filename) {
        map = assetManager.getTiledMap(filename);
        setupPhysixWorld();
        systemMapInit();

        // Setup camera
        camera.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        float totalMapWidth = map.getWidth() * map.getTileWidth();
        float totalMapHeight = map.getHeight() * map.getTileHeight();
        camera.setBounds(0, 0, totalMapWidth, totalMapHeight);
        Main.getInstance().addScreenListener(camera);
    }

    private void setupPhysixWorld() {
        PhysixSystem physixSystem = engine.getSystem(PhysixSystem.class);
        int tileWidth = map.getTileWidth();
        int tileHeight = map.getTileHeight();
        RectangleGenerator generator = new RectangleGenerator();
        generator.generate(map,
                (Layer layer, TileInfo info) -> info.getBooleanProperty("blocked", false),
                (Rectangle rect) -> addShape(physixSystem, rect, tileWidth, tileHeight));

    }

    private void addShape(PhysixSystem physixSystem, Rectangle rect, int tileWidth, int tileHeight) {
        float width = rect.width * tileWidth;
        float height = rect.height * tileHeight;
        float x = rect.x * tileWidth + width / 2;
        float y = rect.y * tileHeight + height / 2;
        PhysixBodyDef bodyDef = new PhysixBodyDef(BodyDef.BodyType.StaticBody, physixSystem).position(x, y).fixedRotation(false);
        Body body = physixSystem.getWorld().createBody(bodyDef);
        body.createFixture(new PhysixFixtureDef(physixSystem).density(1).friction(0).shapeBox(width, height));
    }

    public void start() {
    }

    public void onNoticeStart(NoticeType type) {
    }

    public void onNoticeEnd(NoticeType type) {
    }

    public void update(float delta) {
        engine.update(delta);
    }

    public void createTrigger(PhysixSystem physixSystem, float x, float y, float width, float height, Consumer<Entity> consumer) {
        Entity entity = engine.createEntity();
        PhysixModifierComponent modifyComponent = engine.createComponent(PhysixModifierComponent.class);
        entity.add(modifyComponent);

        TriggerComponent triggerComponent = engine.createComponent(TriggerComponent.class);
        triggerComponent.consumer = consumer;
        entity.add(triggerComponent);

        modifyComponent.schedule(() -> {
            PhysixBodyComponent bodyComponent = engine.createComponent(PhysixBodyComponent.class);
            PhysixBodyDef bodyDef = new PhysixBodyDef(BodyType.StaticBody, physixSystem).position(x, y);
            bodyComponent.init(bodyDef, physixSystem, entity);
            PhysixFixtureDef fixtureDef = new PhysixFixtureDef(physixSystem).sensor(true).shapeBox(width, height);
            bodyComponent.createFixture(fixtureDef);
            entity.add(bodyComponent);
        });
        engine.addEntity(entity);
    }
}
