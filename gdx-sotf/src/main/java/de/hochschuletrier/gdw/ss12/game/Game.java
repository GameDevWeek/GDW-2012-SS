package de.hochschuletrier.gdw.ss12.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
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
import de.hochschuletrier.gdw.commons.tiled.Layer;
import de.hochschuletrier.gdw.commons.tiled.TileInfo;
import de.hochschuletrier.gdw.commons.tiled.TileSet;
import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.commons.tiled.utils.RectangleGenerator;
import de.hochschuletrier.gdw.commons.utils.Rectangle;
import de.hochschuletrier.gdw.ss12.Main;
import de.hochschuletrier.gdw.ss12.game.data.PlayerState;
import de.hochschuletrier.gdw.ss12.game.data.Team;
import de.hochschuletrier.gdw.ss12.game.components.DropableComponent;
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

public class Game {

    private static final float TELEPORTER_SCALE = 0.4f;

    protected final CustomPooledEngine engine = new CustomPooledEngine();
    private ImmutableArray<Entity> playerEntities = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
    private ImmutableArray<Entity> entitiesToRemove = engine.getEntitiesFor(Family.exclude(PlayerComponent.class, TriggerComponent.class).get());

    protected final LimitedSmoothCamera camera = new LimitedSmoothCamera();
    protected TiledMap map;
    protected Entity localPlayer;
    protected final Array<Team> teams = new Array();
    protected AssetManagerX assetManager;
    private InputForwarder inputForwarder = new InputForwarder();

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
        init();
    }

    private void init() {
        addSystems();
        initSystems();
        inputForwarder.set(engine.getSystem(KeyboardInputSystem.class));
    }

    public void dispose() {
    }

    public LimitedSmoothCamera getCamera() {
        return camera;
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

        engine.addSystem(new EntitySpawnSystem());
        engine.addSystem(new UpdatePlayerEffectsSystem());

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
    }

    private void initSystems() {
        ImmutableArray<EntitySystem> systems = engine.getSystems();
        for (int i = 0; i < systems.size(); i++) {
            EntitySystem system = systems.get(i);
            if (system instanceof SystemGameInitializer) {
                SystemGameInitializer initializer = (SystemGameInitializer) system;
                initializer.initGame(this, assetManager);
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

    private static class TeleporterInfo extends Rectangle {

        public final String destination;
        public final float maxSize;

        public TeleporterInfo(int x, int y, int width, int height, String destination) {
            super(x, y, width, height);
            this.destination = destination;
            this.maxSize = Math.max(width, height) * 0.6f;
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
        float totalMapWidth = map.getWidth() * map.getTileWidth();
        float totalMapHeight = map.getHeight() * map.getTileHeight();
        camera.setBounds(0, 0, totalMapWidth, totalMapHeight);
        Main.getInstance().addScreenListener(camera);

        setupTeleporters(physixSystem);

        for (EntitySystem system : engine.getSystems()) {
            if (system instanceof SystemMapInitializer) {
                SystemMapInitializer initializer = (SystemMapInitializer) system;
                initializer.initMap(map, teams);
            }
        }
    }

    public void setLocalPlayer(Entity entity, String name) {
        localPlayer = entity;
        if (localPlayer == null) {
            throw new RuntimeException("Null player");
        }

        ComponentMappers.player.get(localPlayer).name = name;

        updateCameraForced();
    }

    public void updateCameraForced() {
        engine.getSystem(UpdatePositionSystem.class).update(0);
        PositionComponent position = ComponentMappers.position.get(localPlayer);
        camera.setDestination(position.x, position.y);
        camera.updateForced();
    }

    private void setupTeleporters(PhysixSystem physixSystem) {
        HashMap<String, TeleporterInfo> teleporterMap = new HashMap();

        int tileWidth = map.getTileWidth();
        int tileHeight = map.getTileHeight();
        int mapWidth = map.getWidth();
        int mapHeight = map.getHeight();
        for (Layer layer : map.getLayers()) {
            if (layer.isTileLayer()) {
                TileInfo[][] tiles = layer.getTiles();
                for (int x = 0; x < mapWidth; x++) {
                    for (int y = 0; y < mapHeight; y++) {
                        TileInfo info = tiles[x][y];
                        if (info != null && info.getProperty("teleporter", null) != null) {
                            TileInfo tileInfo = tiles[x][y];
                            TileSet tileSet = map.findTileSet(tileInfo.globalId);
                            assert (tileInfo != null);
                            String name = tileInfo.getProperty("teleporter", null);
                            assert (name != null);

                            int width = tileSet.getTileWidth();
                            int height = tileSet.getTileHeight();
                            int x1 = x * tileWidth;
                            int y1 = y * tileHeight - height + tileHeight;
                            String destination = tileInfo.getProperty("destination", null);
                            assert (destination != null);

                            teleporterMap.put(name, new TeleporterInfo(x1, y1, width, height, destination));
                        }
                    }
                }
            }
        }
        for (TeleporterInfo info : teleporterMap.values()) {
            TeleporterInfo destination = teleporterMap.get(info.destination);
            if (destination == null) {
                throw new RuntimeException("Teleporter not linked correctly"); // fixme: more graceful
            }

            createTeleporter(physixSystem, info, destination);
        }
    }

    private void createTeleporter(PhysixSystem physixSystem, TeleporterInfo start, TeleporterInfo destination) {
        createTrigger(physixSystem, start.x + start.width / 2, start.y + start.height / 2, start.width * TELEPORTER_SCALE, start.height, (Entity entity) -> {
            PlayerComponent player = ComponentMappers.player.get(entity);
            if (player != null && (player.lastTeleport + 500) < System.currentTimeMillis() && player.radius <= start.maxSize) {

                PhysixBodyComponent physix = ComponentMappers.physixBody.get(entity);
                float velX = physix.getLinearVelocity().x;
                Vector2 position = physix.getPosition();
                float x = destination.x + ((velX < 0) ? -player.radius : player.radius);
                float y = position.y + destination.y - start.y;

                playGlobalSound("player_teleport", start.x, start.y, false);
                playGlobalSound("player_teleport", destination.x, destination.y, false);

                PhysixModifierComponent modifier = ComponentMappers.physixModifier.get(entity);
                if (modifier == null) {
                    modifier = engine.createComponent(PhysixModifierComponent.class);
                    entity.add(modifier);
                }
                modifier.schedule(() -> {
                    physix.setLinearVelocity(0, 0);
                    physix.setPosition(x, y);
                    if (entity == localPlayer) {
                        updateCameraForced();
                    }
                });
                player.lastTeleport = System.currentTimeMillis();
            }
        });
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

    private void addShape(PhysixSystem physixSystem, Rectangle rect, int tileWidth, int tileHeight) {
        float width = rect.width * tileWidth;
        float height = rect.height * tileHeight;
        float x = rect.x * tileWidth + width / 2;
        float y = rect.y * tileHeight + height / 2;
        PhysixBodyDef bodyDef = new PhysixBodyDef(BodyDef.BodyType.StaticBody, physixSystem).position(x, y).fixedRotation(false);
        Body body = physixSystem.getWorld().createBody(bodyDef);
        body.createFixture(new PhysixFixtureDef(physixSystem).density(1).friction(0).shapeBox(width, height));
    }

    public void loadMap(String filename) {
        map = assetManager.getTiledMap(filename);
        engine.getSystem(InputSystem.class).setProcessing(false);
        setupPhysixWorld();
    }

    public void start() {
        engine.getSystem(InputSystem.class).setProcessing(false);
    }

    public void go() {
        engine.getSystem(InputSystem.class).setProcessing(true);
    }

    public void onNoticeStart(NoticeType type) {
        switch(type) {
            case GO:
                go();
                engine.getSystem(GameStateSystem.class).setProcessing(true);
                break;
            case ROUND_WON:
            case ROUND_LOST:
            case TEAM_WON:
            case TEAM_LOST:
                engine.getSystem(GameStateSystem.class).setProcessing(false);
                break;
        }
    }

    public void onNoticeEnd(NoticeType type) {
        switch(type) {
            case ROUND_WON:
            case ROUND_LOST:
            case TEAM_WON:
            case TEAM_LOST:
                reset();
                break;
        }
    }

    public void update(float delta) {
        PositionComponent position = ComponentMappers.position.get(localPlayer);
        camera.setDestination(position.x, position.y);
        camera.update(delta);
        camera.bind();
        engine.update(delta);
    }

    public void reset() {
        //fixme: only in GameLocal ?
        for (Entity entity : entitiesToRemove) {
            engine.removeEntity(entity);
        }
        for (Entity entity : playerEntities) {
            PlayerComponent player = ComponentMappers.player.get(entity);
            engine.getSystem(PowerupSystem.class).removePlayerPowerups(entity, player);
            player.radius = Constants.PLAYER_DEFAULT_SIZE;
            player.state = PlayerState.ALIVE;
            player.lastTeleport = 0;
            player.killer = null;
            ComponentMappers.light.get(entity).radius = player.radius;
            ComponentMappers.position.get(entity).ignorePhysix = false;
            entity.remove(DropableComponent.class);

            PhysixModifierComponent modifyComponent = engine.createComponent(PhysixModifierComponent.class);
            entity.add(modifyComponent);
            modifyComponent.schedule(() -> {
                PhysixBodyComponent bodyComponent = ComponentMappers.physixBody.get(entity);
                bodyComponent.setPosition(player.startPosition);
                bodyComponent.setLinearVelocity(0, 0);
                bodyComponent.setActive(true);
            });
        }
        start();
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
