package de.hochschuletrier.gdw.ss12.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import de.hochschuletrier.gdw.commons.devcon.cvar.CVarBool;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.input.hotkey.Hotkey;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixBodyDef;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixComponentAwareContactListener;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixFixtureDef;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixBodyComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixModifierComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.systems.PhysixDebugRenderSystem;
import de.hochschuletrier.gdw.commons.gdx.physix.systems.PhysixSystem;
import de.hochschuletrier.gdw.commons.tiled.Layer;
import de.hochschuletrier.gdw.commons.tiled.TileInfo;
import de.hochschuletrier.gdw.commons.tiled.TileSet;
import de.hochschuletrier.gdw.commons.tiled.utils.RectangleGenerator;
import de.hochschuletrier.gdw.commons.utils.Rectangle;
import de.hochschuletrier.gdw.ss12.Main;
import de.hochschuletrier.gdw.ss12.game.components.BotComponent;
import de.hochschuletrier.gdw.ss12.game.components.DropableComponent;
import de.hochschuletrier.gdw.ss12.game.components.InputComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.TriggerComponent;
import de.hochschuletrier.gdw.ss12.game.contactlisteners.PlayerContactListener;
import de.hochschuletrier.gdw.ss12.game.contactlisteners.TriggerContactListener;
import de.hochschuletrier.gdw.ss12.game.data.NoticeType;
import de.hochschuletrier.gdw.ss12.game.data.PlayerState;
import de.hochschuletrier.gdw.ss12.game.data.Team;
import de.hochschuletrier.gdw.ss12.game.systems.BotSystem;
import de.hochschuletrier.gdw.ss12.game.systems.GameStateSystem;
import de.hochschuletrier.gdw.ss12.game.systems.PowerupSystem;
import de.hochschuletrier.gdw.ss12.game.systems.RemoveAnimatedItemSystem;
import de.hochschuletrier.gdw.ss12.game.systems.SpawnRandomEatableSystem;
import de.hochschuletrier.gdw.ss12.game.systems.UpdateLightSystem;
import de.hochschuletrier.gdw.ss12.game.systems.UpdatePlayerSystem;
import de.hochschuletrier.gdw.ss12.game.systems.UpdatePositionSystem;
import de.hochschuletrier.gdw.ss12.game.systems.input.InputSystem;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class GameLocal extends Game {

    private static final float TELEPORTER_SCALE = 0.4f;

    private final CVarBool physixDebug = new CVarBool("physix_debug", !Main.IS_RELEASE, 0, "Draw physix debug");
    private final CVarBool botsEnabled = new CVarBool("bots_enable", true, 0, "Enable bots");
    private final Hotkey togglePhysixDebug = new Hotkey(() -> physixDebug.toggle(false), Input.Keys.F1);
    private final Hotkey toggleBotsEnabled = new Hotkey(this::toggleBotsEnabled, Input.Keys.F2);
    private final Hotkey resetGame = new Hotkey(this::reset, Input.Keys.F5);

    private final ImmutableArray<Entity> botPlayers = engine.getEntitiesFor(Family.all(PlayerComponent.class, BotComponent.class).get());
    private final ImmutableArray<Entity> nonBotPlayers = engine.getEntitiesFor(Family.all(PlayerComponent.class).exclude(BotComponent.class).get());

    private final LinkedList<String> freeBotNames = new LinkedList();
    private static final String BOT_NAME_PREFIX = "[BOT] ";
    private final String[] botNamesOrdered = {
        "Stan", "Kyle", "Cartman", "Kenny",
        "Butters", "Timmy", "Jimmy", "Token",
        "Wendy", "Bebe", "Nichole", "Stacy",
        "Shelly", "Jessica", "Maria", "Henrietta",
        "Chef", "Garrison", "Prin. Victoria", "Ms. Choksondik",
        "Randy", "Sharon", "Gerald", "Sheila"
    };

    public GameLocal(AssetManagerX assetManager) {
        super(assetManager);
    }

    public void init(String mapName, String playerName) {
        super.init();

        Collections.addAll(freeBotNames, botNamesOrdered);
        Collections.shuffle(freeBotNames);

        Main.getInstance().console.register(physixDebug);
        physixDebug.addListener((CVar) -> engine.getSystem(PhysixDebugRenderSystem.class).setProcessing(physixDebug.get()));
        Main.getInstance().console.register(botsEnabled);
        botsEnabled.addListener((CVar) -> engine.getSystem(BotSystem.class).setProcessing(botsEnabled.get()));

        addContactListeners();

        // If this is a build jar file, disable hotkeys
        if (!Main.IS_RELEASE) {
            togglePhysixDebug.register();
            toggleBotsEnabled.register();
            resetGame.register();
        }

        engine.getSystem(InputSystem.class).setProcessing(false);
        loadMap(mapName);
        setLocalPlayer(acquireBotPlayer());
        setPlayerName(localPlayer, playerName);
    }

    @Override
    public void dispose() {
        super.dispose();

        togglePhysixDebug.unregister();
        toggleBotsEnabled.unregister();
        resetGame.unregister();
    }

    public void reset() {
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

    @Override
    public void start() {
        engine.getSystem(InputSystem.class).setProcessing(false);
        sendStartNotices();
    }

    public void go() {
        engine.getSystem(InputSystem.class).setProcessing(true);
    }

    @Override
    protected void addSystems() {
        super.addSystems();

        // Remember to set priorities in CustomPooledEngine when creating new system classes
        engine.addSystem(new InputSystem());
        engine.addSystem(new BotSystem());
        engine.addSystem(new PhysixSystem(
                Constants.BOX2D_SCALE, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS
        ));
        engine.addSystem(new PowerupSystem());
        engine.addSystem(new UpdatePositionSystem());
        engine.addSystem(new SpawnRandomEatableSystem());
        engine.addSystem(new UpdatePlayerSystem());
        engine.addSystem(new UpdateLightSystem());
        engine.addSystem(new RemoveAnimatedItemSystem());
        engine.addSystem(new GameStateSystem());
        engine.addSystem(new PhysixDebugRenderSystem());
    }

    @Override
    public void updateCameraForced() {
        engine.getSystem(UpdatePositionSystem.class).update(0);
        super.updateCameraForced();
    }

    public void toggleBotsEnabled() {
        for (Entity botPlayer : botPlayers) {
            InputComponent input = ComponentMappers.input.get(botPlayer);
            input.moveDirection.setZero();
            input.dropItem = false;
        }
        botsEnabled.toggle(false);
    }

    protected void onPlayerNameChanged(Entity entity) {
    }

    public String acquireBotName() {
        return BOT_NAME_PREFIX + freeBotNames.pop();
    }

    public void freeBotName(String botName) {
        freeBotNames.add(botName.substring(BOT_NAME_PREFIX.length()));
        Collections.shuffle(freeBotNames);
    }

    public Entity acquireBotPlayer() {
        teams.sort(acquireTeamComparator);
        Team bestTeam = teams.get(0);

        Entity entity = acquireBotPlayer(bestTeam);
        if (entity != null) {
            PlayerComponent player = ComponentMappers.player.get(entity);
            player.team.numConnectedPlayers++;
            freeBotName(player.name);
            player.name = "[Connecting]";
            onPlayerNameChanged(entity);
            entity.remove(BotComponent.class);
        }
        return entity;
    }

    private Entity acquireBotPlayer(Team team) {
        // try alive players first
        for (Entity e : botPlayers) {
            PlayerComponent p = ComponentMappers.player.get(e);
            if (!p.isDead() && p.team == team) {
                return e;
            }
        }

        // now take whatever you can get
        return botPlayers.random();
    }

    public void freeBotPlayer(Entity entity) {
        PlayerComponent player = ComponentMappers.player.get(entity);
        player.team.numConnectedPlayers--;
        player.name = acquireBotName();
        onPlayerNameChanged(entity);
        entity.add(engine.createComponent(BotComponent.class));
    }

    private void addContactListeners() {
        PhysixSystem physixSystem = engine.getSystem(PhysixSystem.class);
        PhysixComponentAwareContactListener contactListener = new PhysixComponentAwareContactListener();
        physixSystem.getWorld().setContactListener(contactListener);
        contactListener.addListener(TriggerComponent.class, new TriggerContactListener());
        contactListener.addListener(PlayerComponent.class, new PlayerContactListener(engine, this));
    }

    public void sendStartNotices() {
        scheduleNoticeForAll(NoticeType.THREE, 0, -1);
        scheduleNoticeForAll(NoticeType.TWO, 1, -1);
        scheduleNoticeForAll(NoticeType.ONE, 2, -1);
        scheduleNoticeForAll(NoticeType.GO, 3, -1);
    }

    @Override
    public void onNoticeStart(NoticeType type) {
        switch (type) {
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

    @Override
    public void onNoticeEnd(NoticeType type) {
        switch (type) {
            case ROUND_WON:
            case ROUND_LOST:
            case TEAM_WON:
            case TEAM_LOST:
                reset();
                break;
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

    private void addShape(PhysixSystem physixSystem, Rectangle rect, int tileWidth, int tileHeight) {
        float width = rect.width * tileWidth;
        float height = rect.height * tileHeight;
        float x = rect.x * tileWidth + width / 2;
        float y = rect.y * tileHeight + height / 2;
        PhysixBodyDef bodyDef = new PhysixBodyDef(BodyDef.BodyType.StaticBody, physixSystem).position(x, y).fixedRotation(false);
        Body body = physixSystem.getWorld().createBody(bodyDef);
        body.createFixture(new PhysixFixtureDef(physixSystem).density(1).friction(0).shapeBox(width, height));
    }

    @Override
    protected void systemMapInit() {
        setupPhysixWorld();
        super.systemMapInit();
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

        setupTeleporters(physixSystem);
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
}
