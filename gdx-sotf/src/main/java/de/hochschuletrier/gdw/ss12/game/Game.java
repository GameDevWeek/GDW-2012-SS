package de.hochschuletrier.gdw.ss12.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Array;
import de.hochschuletrier.gdw.commons.devcon.cvar.CVarBool;
import de.hochschuletrier.gdw.commons.gdx.assets.AnimationExtended;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.input.InputForwarder;
import de.hochschuletrier.gdw.commons.gdx.physix.systems.PhysixSystem;
import de.hochschuletrier.gdw.commons.gdx.audio.SoundEmitter;
import de.hochschuletrier.gdw.commons.gdx.audio.SoundInstance;
import de.hochschuletrier.gdw.commons.gdx.entityFactory.EntityFactory;
import de.hochschuletrier.gdw.commons.gdx.input.hotkey.Hotkey;
import de.hochschuletrier.gdw.commons.gdx.physix.systems.PhysixDebugRenderSystem;
import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.ss12.Main;
import de.hochschuletrier.gdw.ss12.game.components.factories.EntityFactoryParam;
import de.hochschuletrier.gdw.ss12.game.data.PlayerState;
import de.hochschuletrier.gdw.ss12.game.data.Team;
import de.hochschuletrier.gdw.ss12.game.components.PositionComponent;
import de.hochschuletrier.gdw.ss12.game.components.SetupComponent;
import de.hochschuletrier.gdw.ss12.game.components.SoundEmitterComponent;
import de.hochschuletrier.gdw.ss12.game.data.NoticeType;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemMapInitializer;
import de.hochschuletrier.gdw.ss12.game.systems.*;
import de.hochschuletrier.gdw.ss12.game.systems.input.*;
import de.hochschuletrier.gdw.ss12.game.systems.rendering.*;
import java.util.Comparator;
import java.util.HashMap;

public abstract class Game {

    protected final CustomPooledEngine engine = new CustomPooledEngine();
    private final EntityFactoryParam factoryParam = new EntityFactoryParam();
    private final EntityFactory<EntityFactoryParam> entityFactory = new EntityFactory("data/json/entities.json", Game.class);

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
        entityFactory.init(engine, assetManager);
        
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

    public Entity getLocalPlayer() {
        return localPlayer;
    }

    public Array<Team> getTeams() {
        return teams;
    }

    public PooledEngine getEngine() {
        return engine;
    }

    public String getMapName() {
        return map.getFilename();
    }

    public InputProcessor getInputProcessor() {
        return inputForwarder;
    }

    protected void addSystems() {
        // Remember to set priorities in CustomPooledEngine when creating new system classes
        engine.addSystem(new KeyboardInputSystem());
        engine.addSystem(new InputSystem());
        engine.addSystem(new UpdateSoundEmitterSystem());

        engine.addSystem(new PhysixSystem(
                Constants.BOX2D_SCALE, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS
        ));
        engine.addSystem(new UpdatePlayerEffectsSystem());
        engine.addSystem(new CameraSystem());
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

    protected void systemMapInit() {
        for (EntitySystem system : engine.getSystems()) {
            if (system instanceof SystemMapInitializer) {
                SystemMapInitializer initializer = (SystemMapInitializer) system;
                initializer.initMap(map, teams);
            }
        }
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
        engine.getSystem(CameraSystem.class).forceCameraUpdate();
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
        MapLoader.run(this, map);
        systemMapInit();
    }

    public void startCountdown() {
        engine.getSystem(InputSystem.class).setProcessing(false);
    }

    public void go() {
        engine.getSystem(InputSystem.class).setProcessing(true);
    }

    public void onNoticeStart(NoticeType type) {
        if(type == NoticeType.GO) {
            go();
        }
    }

    public void onNoticeEnd(NoticeType type) {
    }

    public void update(float delta) {
        engine.update(delta);
    }

    public Entity createEntity(String name, float x, float y, Team team) {
        factoryParam.game = this;
        factoryParam.team = team;
        factoryParam.x = x;
        factoryParam.y = y;
        Entity entity = entityFactory.createEntity(name, factoryParam);

        if (this instanceof GameServer) {
            SetupComponent setup = engine.createComponent(SetupComponent.class);
            setup.team = team;
            setup.name = name;
            entity.add(setup);
        }

        engine.addEntity(entity);
        return entity;
    }

    public Entity createPlayer(float x, float y, Team team, String name) {
        team.numPlayers++;

        Entity entity = createEntity("player", x, y, team);
        ComponentMappers.player.get(entity).name = name;
        return entity;
    }

    public EntityFactory<EntityFactoryParam> getEntityFactory() {
        return entityFactory;
    }
}
