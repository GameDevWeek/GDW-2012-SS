package de.hochschuletrier.gdw.ss14.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import de.hochschuletrier.gdw.commons.devcon.cvar.CVarBool;
import de.hochschuletrier.gdw.commons.gdx.assets.AnimationExtended;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.cameras.orthogonal.LimitedSmoothCamera;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixBodyDef;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixComponentAwareContactListener;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixFixtureDef;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixBodyComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixModifierComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.systems.PhysixDebugRenderSystem;
import de.hochschuletrier.gdw.commons.gdx.physix.systems.PhysixSystem;
import de.hochschuletrier.gdw.commons.gdx.sound.SoundEmitter;
import de.hochschuletrier.gdw.commons.gdx.sound.SoundInstance;
import de.hochschuletrier.gdw.commons.tiled.Layer;
import de.hochschuletrier.gdw.commons.tiled.LayerObject;
import de.hochschuletrier.gdw.commons.tiled.TileInfo;
import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.commons.tiled.utils.RectangleGenerator;
import de.hochschuletrier.gdw.commons.utils.Rectangle;
import de.hochschuletrier.gdw.ss14.Main;
import de.hochschuletrier.gdw.ss14.game.componentdata.PlayerState;
import de.hochschuletrier.gdw.ss14.game.componentdata.Team;
import de.hochschuletrier.gdw.ss14.game.components.BotComponent;
import de.hochschuletrier.gdw.ss14.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss14.game.components.PositionComponent;
import de.hochschuletrier.gdw.ss14.game.components.SoundEmitterComponent;
import de.hochschuletrier.gdw.ss14.game.components.TriggerComponent;
import de.hochschuletrier.gdw.ss14.game.contactlisteners.PlayerContactListener;
import de.hochschuletrier.gdw.ss14.game.contactlisteners.TriggerContactListener;
import de.hochschuletrier.gdw.ss14.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss14.game.interfaces.SystemMapInitializer;
import de.hochschuletrier.gdw.ss14.game.systems.BotSystem;
import de.hochschuletrier.gdw.ss14.game.systems.EntitySpawnSystem;
import de.hochschuletrier.gdw.ss14.game.systems.PowerupSystem;
import de.hochschuletrier.gdw.ss14.game.systems.UpdateLightSystem;
import de.hochschuletrier.gdw.ss14.game.systems.UpdatePlayerSystem;
import de.hochschuletrier.gdw.ss14.game.systems.UpdatePositionSystem;
import de.hochschuletrier.gdw.ss14.game.systems.UpdateSoundEmitterSystem;
import de.hochschuletrier.gdw.ss14.game.systems.input.InputSystem;
import de.hochschuletrier.gdw.ss14.game.systems.input.KeyboardInputSystem;
import de.hochschuletrier.gdw.ss14.game.systems.rendering.RenderPowerupHudSystem;
import de.hochschuletrier.gdw.ss14.game.systems.rendering.RenderItemSystem;
import de.hochschuletrier.gdw.ss14.game.systems.rendering.RenderMapSystem;
import de.hochschuletrier.gdw.ss14.game.systems.rendering.RenderPlayerSystem;
import de.hochschuletrier.gdw.ss14.game.systems.rendering.RenderShadowMapCleanupSystem;
import de.hochschuletrier.gdw.ss14.game.systems.rendering.RenderShadowMapSystem;
import java.util.HashMap;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Game extends InputAdapter {

    private static final Logger logger = LoggerFactory.getLogger(Game.class);
    private final CVarBool physixDebug = new CVarBool("physix_debug", true, 0, "Draw physix debug");
    private final CVarBool botsEnabled = new CVarBool("bots_enable", true, 0, "Enable bots");

    private final PooledEngine engine = new PooledEngine(
            Constants.ENTITY_POOL_INITIAL_SIZE, Constants.ENTITY_POOL_MAX_SIZE,
            Constants.COMPONENT_POOL_INITIAL_SIZE, Constants.COMPONENT_POOL_MAX_SIZE
    );
    ImmutableArray<Entity> botPlayers = engine.getEntitiesFor(Family.all(PlayerComponent.class, BotComponent.class).get());

    private final LimitedSmoothCamera camera = new LimitedSmoothCamera();
    private float totalMapWidth, totalMapHeight;
    private TiledMap map;
    private Entity localPlayer;
    private final Team[] teams = new Team[Constants.TEAM_COLOR_TABLE.length];
    private AssetManagerX assetManager;

    public Game(AssetManagerX assetManager, String mapFile) {
        this.assetManager = assetManager;
        for(int i=0; i<teams.length; i++) {
            teams[i] = new Team(i, "Team " + i, Constants.TEAM_COLOR_TABLE[i]);
            HashMap<PlayerState, AnimationExtended> animations = teams[i].animations;
            animations.put(PlayerState.PAUSED, assetManager.getAnimation("player_team" + i));
            animations.put(PlayerState.ALIVE, assetManager.getAnimation("player_team" + i));
            animations.put(PlayerState.SLIPPING, assetManager.getAnimation("player_team" + i));
            animations.put(PlayerState.DEAD, assetManager.getAnimation("player_wiggle"));
            animations.put(PlayerState.HALUCINATING, assetManager.getAnimation("player_halucinating"));
        }
        Main.getInstance().console.register(physixDebug);
        physixDebug.addListener((CVar) -> engine.getSystem(PhysixDebugRenderSystem.class).setProcessing(physixDebug.get()));
        Main.getInstance().console.register(botsEnabled);
        botsEnabled.addListener((CVar) -> engine.getSystem(BotSystem.class).setProcessing(botsEnabled.get()));
        addSystems(assetManager);
        addContactListeners();
        map = loadMap(mapFile);
        setupPhysixWorld();

        Main.inputMultiplexer.addProcessor(this);
    }

    public void dispose() {
    }

    public LimitedSmoothCamera getCamera() {
        return camera;
    }

    public Entity getLocalPlayer() {
        return localPlayer;
    }

    private void addSystems(AssetManagerX assetManager) {
        engine.addSystem(new KeyboardInputSystem(Constants.PRIORITY_INPUT));
        engine.addSystem(new BotSystem(Constants.PRIORITY_INPUT));
        engine.addSystem(new InputSystem(Constants.PRIORITY_INPUT + 1));
        engine.addSystem(new PhysixSystem(
                Constants.BOX2D_SCALE, Constants.STEP_SIZE,
                Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS,
                Constants.PRIORITY_PHYSIX
        ));
        engine.addSystem(new UpdatePositionSystem(Constants.PRIORITY_PHYSIX + 1));
        engine.addSystem(new UpdateSoundEmitterSystem(Constants.PRIORITY_PHYSIX + 2));
        
        engine.addSystem(new EntitySpawnSystem(Constants.PRIORITY_ENTITIES));
        engine.addSystem(new PowerupSystem(Constants.PRIORITY_ENTITIES+1));
        engine.addSystem(new UpdatePlayerSystem(Constants.PRIORITY_ENTITIES+2));
        engine.addSystem(new UpdateLightSystem(Constants.PRIORITY_ENTITIES+3));
        
        engine.addSystem(new RenderShadowMapSystem(Constants.PRIORITY_ENTITY_RENDER));
        engine.addSystem(new RenderMapSystem(Constants.PRIORITY_ENTITY_RENDER+1));
        engine.addSystem(new RenderItemSystem(Constants.PRIORITY_ENTITY_RENDER+2));
        engine.addSystem(new RenderPlayerSystem(Constants.PRIORITY_ENTITY_RENDER+3));
        engine.addSystem(new RenderShadowMapCleanupSystem(Constants.PRIORITY_ENTITY_RENDER+4));
        engine.addSystem(new PhysixDebugRenderSystem(Constants.PRIORITY_DEBUG_WORLD));
        engine.addSystem(new RenderPowerupHudSystem(Constants.PRIORITY_HUD));
        
        
        ImmutableArray<EntitySystem> systems = engine.getSystems();
        for(int i=0; i<systems.size(); i++) {
            EntitySystem system = systems.get(i);
            if(system instanceof SystemGameInitializer) {
                SystemGameInitializer initializer = (SystemGameInitializer)system;
                initializer.initGame(this, assetManager);
            }
        }
    }

    private void addContactListeners() {
        PhysixSystem physixSystem = engine.getSystem(PhysixSystem.class);
        PhysixComponentAwareContactListener contactListener = new PhysixComponentAwareContactListener();
        physixSystem.getWorld().setContactListener(contactListener);
        contactListener.addListener(TriggerComponent.class, new TriggerContactListener());
        contactListener.addListener(PlayerComponent.class, new PlayerContactListener(engine, this));
    }
    
    private static class TeleporterInfo extends Rectangle {
        public final String destination;
        
        public TeleporterInfo(LayerObject object) {
            x = object.getX();
            y = object.getY();
            width = object.getWidth();
            height = object.getHeight();
            destination = object.getProperty("destination", null);
        }
    }

    private void setupPhysixWorld() {
        PhysixSystem physixSystem = engine.getSystem(PhysixSystem.class);
        // Generate static world
        int tileWidth = map.getTileWidth();
        int tileHeight = map.getTileHeight();
        RectangleGenerator generator = new RectangleGenerator();
        generator.generate(map,
                (Layer layer, TileInfo info) -> info.getBooleanProperty("blocked", false),
                (Rectangle rect) -> addShape(physixSystem, rect, tileWidth, tileHeight));

        // Setup camera
        camera.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        totalMapWidth = map.getWidth() * map.getTileWidth();
        totalMapHeight = map.getHeight() * map.getTileHeight();
        camera.setBounds(0, 0, totalMapWidth, totalMapHeight);
        camera.updateForced();
        Main.getInstance().addScreenListener(camera);
        
        setupTeleporters(physixSystem);
        
        for(EntitySystem system: engine.getSystems()) {
            if(system instanceof SystemMapInitializer) {
                SystemMapInitializer initializer = (SystemMapInitializer)system;
                initializer.initMap(map, teams);
            }
        }
        
        // Setup local player
        localPlayer = acquireBotPlayer("Maximo");
        if(localPlayer == null)
            throw new RuntimeException("No free player available");
    }

    private void setupTeleporters(PhysixSystem physixSystem) {
        HashMap<String, TeleporterInfo> teleporterMap = new HashMap();
        
        for(Layer layer: map.getLayers()) {
            if(layer.isObjectLayer()) {
                for(LayerObject object: layer.getObjects()) {
                    if("teleporter".equalsIgnoreCase(object.getType())) {
                        teleporterMap.put(object.getName(), new TeleporterInfo(object));
                    }
                }
            }
        }
        for(TeleporterInfo info: teleporterMap.values()) {
            TeleporterInfo destination = teleporterMap.get(info.destination);
            if(destination == null) {
                throw new RuntimeException("Teleporter not linked correctly"); // fixme: more graceful
            }
            
            createTeleporter(physixSystem, info, destination);
        }
    }

    private void createTeleporter(PhysixSystem physixSystem, TeleporterInfo start, TeleporterInfo destination) {
        createTrigger(physixSystem, start.x + start.width/2, start.y + start.height/2, start.width, start.height, (Entity entity) -> {
            PlayerComponent player = ComponentMappers.player.get(entity);
            if(player != null && (player.lastTeleport + 500) < System.currentTimeMillis()) {

                PhysixBodyComponent physix = ComponentMappers.physixBody.get(entity);
                Vector2 position = physix.getPosition();
                float x = destination.x;
                float y = position.y + destination.y - start.y;
                
                playGlobalSound("player_teleport", start.x, start.y, false);
                playGlobalSound("player_teleport", destination.x, destination.y, false);
                
                PhysixModifierComponent modifier = ComponentMappers.physixModifier.get(entity);
                if(modifier == null) {
                    modifier = engine.createComponent(PhysixModifierComponent.class);
                    entity.add(modifier);
                }
                modifier.schedule(()-> {physix.setLinearVelocity(0, 0); physix.setPosition(x, y);});
                player.lastTeleport = System.currentTimeMillis();
            }
        });
    }
    
    public SoundInstance playGlobalSound(String name, float x, float y, boolean loop) {
        return SoundEmitter.playGlobal(assetManager.getSound(name), loop, x, y, 0);
    }
    
    public SoundInstance playEntitySound(String name, Entity entity, boolean loop) {
        SoundEmitterComponent soundEmitter = ComponentMappers.soundEmitter.get(entity);
        if(soundEmitter != null) {
            return soundEmitter.emitter.play(assetManager.getSound(name), loop);
        } else {
            PositionComponent position = ComponentMappers.position.get(entity);
            if(position != null) {
                return playGlobalSound(name, position.x, position.y, loop);
            }
        }
        return null;
    }
    
    public Entity acquireBotPlayer(String name) {
        Team bestTeam = teams[0];
        for (int i = 1; i < teams.length; i++) {
            if (!teams[i].isFull() && teams[i].numConnectedPlayers < bestTeam.numConnectedPlayers) {
                bestTeam = teams[i];
            }
        }
        if(bestTeam.isFull())
            return null;

        Entity entity = acquireBotPlayer(bestTeam);
        if(entity != null) {
            PlayerComponent player = ComponentMappers.player.get(entity);
            player.name = name;
            player.team.numConnectedPlayers++;
            entity.remove(BotComponent.class);
        }
        return entity;
    }

    private Entity acquireBotPlayer(Team team) {
        // try alive players first
        for(Entity e: botPlayers) {
            PlayerComponent p = ComponentMappers.player.get(e);
            if (!p.isDead() && p.team == team) {
                return e;
            }
        }

        // now take whatever you can get
        return botPlayers.random();
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

    public TiledMap loadMap(String filename) {
        try {
            return new TiledMap(filename, LayerObject.PolyMode.ABSOLUTE);
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Map konnte nicht geladen werden: " + filename);
        }
    }

    public void update(float delta) {
        PositionComponent position = ComponentMappers.position.get(localPlayer);
        camera.setDestination(position.x, position.y);
        camera.update(delta);
        camera.bind();
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
