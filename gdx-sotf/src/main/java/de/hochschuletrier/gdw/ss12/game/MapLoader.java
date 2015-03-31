package de.hochschuletrier.gdw.ss12.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Array;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixBodyDef;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixFixtureDef;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixBodyComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixModifierComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.systems.PhysixSystem;
import de.hochschuletrier.gdw.commons.tiled.Layer;
import de.hochschuletrier.gdw.commons.tiled.LayerObject;
import de.hochschuletrier.gdw.commons.tiled.TileInfo;
import de.hochschuletrier.gdw.commons.tiled.TileSet;
import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.commons.tiled.utils.RectangleGenerator;
import de.hochschuletrier.gdw.commons.utils.Rectangle;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.TriggerComponent;
import de.hochschuletrier.gdw.ss12.game.data.Team;
import java.util.HashMap;
import java.util.function.Consumer;

public class MapLoader {
    
    public static void run(Game game, TiledMap map) {
        setupPhysixWorld(game, map);
        if(game instanceof GameLocal) {
            GameLocal gameLocal = (GameLocal)game;
            loadPlayersFromMap(gameLocal, map);
            setupTeleporters(gameLocal, map);
        }
    }

    private static void setupPhysixWorld(Game game, TiledMap map) {
        PhysixSystem physixSystem = game.getEngine().getSystem(PhysixSystem.class);
        int tileWidth = map.getTileWidth();
        int tileHeight = map.getTileHeight();
        RectangleGenerator generator = new RectangleGenerator();
        generator.generate(map,
                (Layer layer, TileInfo info) -> info.getBooleanProperty("blocked", false),
                (Rectangle rect) -> addShape(physixSystem, rect, tileWidth, tileHeight));

    }

    private static void addShape(PhysixSystem physixSystem, Rectangle rect, int tileWidth, int tileHeight) {
        float width = rect.width * tileWidth;
        float height = rect.height * tileHeight;
        float x = rect.x * tileWidth + width / 2;
        float y = rect.y * tileHeight + height / 2;
        PhysixBodyDef bodyDef = new PhysixBodyDef(BodyDef.BodyType.StaticBody, physixSystem).position(x, y).fixedRotation(false);
        Body body = physixSystem.getWorld().createBody(bodyDef);
        body.createFixture(new PhysixFixtureDef(physixSystem).density(1).friction(0).shapeBox(width, height));
    }
    
    private static void loadPlayersFromMap(GameLocal game, TiledMap map) {
        Array<Team> teams = game.getTeams();
        int highestTeamID = -1;
        for (Layer layer : map.getLayers()) {
            if (layer.isObjectLayer()) {
                for (LayerObject obj : layer.getObjects()) {
                    int id = obj.getIntProperty("team", -1);
                    if (id >= 0) {
                        if (id > highestTeamID) {
                            highestTeamID = id;
                        }
                        if (id < 0 || id >= teams.size) {
                            throw new RuntimeException("Map contains bad Team Id: " + id);
                        }

                        game.createPlayer(obj.getX() + obj.getWidth() / 2, obj.getY() + obj.getHeight() / 2, teams.get(id), game.acquireBotName());
                    }
                }
            }
        }
    }
    
    private static final float TELEPORTER_SCALE = 0.4f;
    private static class TeleporterInfo extends Rectangle {

        public final String destination;
        public final float maxSize;

        public TeleporterInfo(int x, int y, int width, int height, String destination) {
            super(x, y, width, height);
            this.destination = destination;
            this.maxSize = Math.max(width, height) * 0.6f;
        }
    }

    private static void setupTeleporters(GameLocal game, TiledMap map) {
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

            createTeleporter(game, info, destination);
        }
    }

    private static void createTeleporter(GameLocal game, TeleporterInfo start, TeleporterInfo destination) {
        createTrigger(game.getEngine(), start.x + start.width / 2, start.y + start.height / 2, start.width * TELEPORTER_SCALE, start.height, (Entity entity) -> {
            PlayerComponent player = ComponentMappers.player.get(entity);
            if (player != null && (player.lastTeleport + 500) < System.currentTimeMillis() && player.radius <= start.maxSize) {

                PhysixBodyComponent physix = ComponentMappers.physixBody.get(entity);
                float velX = physix.getLinearVelocity().x;
                Vector2 position = physix.getPosition();
                float x = destination.x + ((velX < 0) ? -player.radius : player.radius);
                float y = position.y + destination.y - start.y;

                game.playGlobalSound("player_teleport", start.x, start.y, false);
                game.playGlobalSound("player_teleport", destination.x, destination.y, false);

                PhysixModifierComponent modifier = ComponentMappers.physixModifier.get(entity);
                if (modifier == null) {
                    modifier = game.getEngine().createComponent(PhysixModifierComponent.class);
                    entity.add(modifier);
                }
                modifier.schedule(() -> {
                    physix.setLinearVelocity(0, 0);
                    physix.setPosition(x, y);
                    if (entity == game.getLocalPlayer()) {
                        game.updateCameraForced();
                    }
                });
                player.lastTeleport = System.currentTimeMillis();
            }
        });
    }

    private static void createTrigger(PooledEngine engine, float x, float y, float width, float height, Consumer<Entity> consumer) {
        Entity entity = engine.createEntity();
        PhysixModifierComponent modifyComponent = engine.createComponent(PhysixModifierComponent.class);
        entity.add(modifyComponent);

        TriggerComponent triggerComponent = engine.createComponent(TriggerComponent.class);
        triggerComponent.consumer = consumer;
        entity.add(triggerComponent);

        modifyComponent.schedule(() -> {
            PhysixSystem physixSystem = engine.getSystem(PhysixSystem.class);
            PhysixBodyComponent bodyComponent = engine.createComponent(PhysixBodyComponent.class);
            PhysixBodyDef bodyDef = new PhysixBodyDef(BodyDef.BodyType.StaticBody, physixSystem).position(x, y);
            bodyComponent.init(bodyDef, physixSystem, entity);
            PhysixFixtureDef fixtureDef = new PhysixFixtureDef(physixSystem).sensor(true).shapeBox(width, height);
            bodyComponent.createFixture(fixtureDef);
            entity.add(bodyComponent);
        });
        engine.addEntity(entity);
    }
}
