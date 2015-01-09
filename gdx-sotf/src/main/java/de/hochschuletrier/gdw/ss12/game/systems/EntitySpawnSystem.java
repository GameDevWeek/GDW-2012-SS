package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixBodyDef;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixFixtureDef;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixBodyComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixModifierComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.systems.PhysixSystem;
import de.hochschuletrier.gdw.commons.jackson.JacksonReader;
import de.hochschuletrier.gdw.commons.tiled.Layer;
import de.hochschuletrier.gdw.commons.tiled.LayerObject;
import de.hochschuletrier.gdw.commons.tiled.TileInfo;
import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.data.PlayerState;
import de.hochschuletrier.gdw.ss12.game.components.data.Team;
import de.hochschuletrier.gdw.ss12.game.components.BotComponent;
import de.hochschuletrier.gdw.ss12.game.components.EatableComponent;
import de.hochschuletrier.gdw.ss12.game.components.InputComponent;
import de.hochschuletrier.gdw.ss12.game.components.ItemTrapComponent;
import de.hochschuletrier.gdw.ss12.game.components.LightComponent;
import de.hochschuletrier.gdw.ss12.game.components.PizzaSliceComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.PositionComponent;
import de.hochschuletrier.gdw.ss12.game.components.RenderTextureComponent;
import de.hochschuletrier.gdw.ss12.game.components.DropableComponent;
import de.hochschuletrier.gdw.ss12.game.components.RenderAnimationComponent;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemMapInitializer;
import de.hochschuletrier.gdw.ss12.game.json.EntityJson;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntitySpawnSystem extends EntitySystem implements SystemGameInitializer, SystemMapInitializer, EntityListener {

    private static final Logger logger = LoggerFactory.getLogger(EntitySystem.class);

    private ImmutableArray<Entity> eatables;
    private Stack<String> nextEatables = new Stack();
    private Stack<String> usedEatables = new Stack();

    private PooledEngine engine;
    private PhysixSystem physixSystem;
    private HashMap<String, EntityJson> entityJsonMap;
    private AssetManagerX assetManager;
    private final SpawnPositionPool spawnPositionPool = new SpawnPositionPool(256, 512);
    private final Array<SpawnPosition> spawnPositions = new Array();
    private float timeSincelastItemSpawnTry;
    private final Random random = new Random();
    private final LinkedList<String> freeBotNames = new LinkedList();
    private final String[] botNamesOrdered = {
        "Stan", "Kyle", "Cartman", "Kenny",
        "Butters", "Timmy", "Jimmy", "Token",
        "Wendy", "Bebe", "Nichole", "Stacy",
        "Shelly", "Jessica", "Maria", "Henrietta",
        "Chef", "Garrison", "Prin. Victoria", "Ms. Choksondik",
        "Randy", "Sharon", "Gerald", "Sheila"
    };

    public EntitySpawnSystem() {
        super(0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.assetManager = assetManager;

        nextEatables.clear();
        usedEatables.clear();

        try {
            entityJsonMap = JacksonReader.readMap("data/json/entities.json", EntityJson.class);
            for (Map.Entry<String, EntityJson> entry : entityJsonMap.entrySet()) {
                for (int i = 0; i < entry.getValue().frequency; i++) {
                    nextEatables.push(entry.getKey());
                }
            }
            Collections.shuffle(nextEatables);
        } catch (Exception e) {
            logger.error("Error reading entities.json", e);
        }

        Collections.addAll(freeBotNames, botNamesOrdered);
        Collections.shuffle(freeBotNames);
    }

    @Override
    public void initMap(TiledMap map, Array<Team> teams) {
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

                        createBotPlayer(obj.getX() + obj.getWidth() / 2, obj.getY() + obj.getHeight() / 2, teams.get(id), "[BOT] " + freeBotNames.pop());
                    }
                }
            }
        }

        int width = map.getWidth();
        int height = map.getHeight();

        boolean available[][] = new boolean[width][height];
        for (Layer layer : map.getLayers()) {
            if (layer.isTileLayer()) {
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        TileInfo tile = layer.getTiles()[x][y];
                        if (tile != null && tile.getBooleanProperty("itemspawn", false)) {
                            available[x][y] = true;
                        }
                    }
                }
            }
        }

        // Item spawns hinzufügen wenn ein Tile und dessen umliegenden Tiles nicht genutzt sind.
        int tileWidth = map.getTileWidth();
        int tileHeight = map.getTileHeight();
        int numItemSpawns = 0;
        for (int y = 1; y < height - 1; ++y) {
            for (int x = 1; x < width - 1; ++x) {
                if (available[x - 1][y - 1] && available[x][y - 1] && available[x + 1][y - 1]
                        && available[x - 1][y] && available[x][y] && available[x + 1][y]
                        && available[x - 1][y + 1] && available[x][y + 1] && available[x + 1][y + 1]) {
                    available[x][y] = false;
                    SpawnPosition pos = spawnPositionPool.obtain();
                    pos.set(x * tileWidth, y * tileHeight);
                    spawnPositions.add(pos);
                    numItemSpawns++;
                    x++;
                }
            }
        }

        if (numItemSpawns == 0) {
            throw new RuntimeException("Map contains no space for item spawns");
        }
    }

    @Override
    public void update(float deltaTime) {
        spawnRandomItems(deltaTime);
    }

    private void spawnRandomItems(float deltaTime) {
        // spawn random items
        timeSincelastItemSpawnTry += deltaTime;
        if (timeSincelastItemSpawnTry >= Constants.TIME_TO_NEXT_ITEM_CHANCE && spawnPositions.size > 0) {
            float rand = random.nextFloat();

            // Spawnrate reduziert wenn mehr als 24 Items auf der Map.
            if ((rand <= Constants.CHANCE_FOR_NEW_ITEM)
                    && (eatables.size() < Constants.AT_THIS_NUMBER_REDUCED_ITEM_CHANCE_STARTS)
                    || (rand <= Constants.CHANCE_FOR_NEW_ITEM_REDUCED)) {

                int index = random.nextInt(spawnPositions.size);

                SpawnPosition spawnPos = spawnPositions.removeIndex(index);
                createRandomEatable(spawnPos);
                spawnPositionPool.free(spawnPos);
            }
            timeSincelastItemSpawnTry -= Constants.TIME_TO_NEXT_ITEM_CHANCE;
        }
    }

    private void createRandomEatable(Vector2 position) {
        if (nextEatables.empty()) {
            Stack<String> t = nextEatables;
            nextEatables = usedEatables;
            usedEatables = t;
            Collections.shuffle(nextEatables);
        }
        String eatableName = nextEatables.pop();
        usedEatables.push(eatableName);
        createStaticEntity(eatableName, position.x, position.y, Constants.ITEM_RADIUS, null);
    }

    @Override
    public void addedToEngine(Engine engine) {
        this.engine = (PooledEngine) engine;
        physixSystem = engine.getSystem(PhysixSystem.class);
        engine.addEntityListener(this);
        eatables = engine.getEntitiesFor(Family.all(EatableComponent.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {
        this.engine = null;
        physixSystem = null;
        engine.removeEntityListener(this);
        eatables = null;
    }

    @Override
    public void entityAdded(Entity entity) {
    }

    @Override
    public void entityRemoved(Entity entity) {
        if (ComponentMappers.eatable.has(entity)) {
            PositionComponent position = ComponentMappers.position.get(entity);
            SpawnPosition spawnPos = spawnPositionPool.obtain();
            spawnPos.set(position.x, position.y);
            spawnPositions.add(spawnPos);
        }
    }

    private Entity createBotPlayer(float x, float y, Team team, String name) {
        team.numSlots++;

        Entity entity = engine.createEntity();
        PositionComponent position = engine.createComponent(PositionComponent.class);
        position.x = x;
        position.y = y;
        entity.add(position);
        PhysixModifierComponent modifyComponent = engine.createComponent(PhysixModifierComponent.class);
        entity.add(modifyComponent);

        PlayerComponent player = engine.createComponent(PlayerComponent.class);
        player.radius = Constants.PLAYER_DEFAULT_SIZE;
        player.state = PlayerState.ALIVE;
        player.team = team;
        player.name = name;
        player.startPosition.set(x, y);
        entity.add(player);

        entity.add(engine.createComponent(BotComponent.class));

        InputComponent input = engine.createComponent(InputComponent.class);
        input.speed = Constants.PLAYER_MOVEMENT_SPEED;
        entity.add(input);

        LightComponent light = engine.createComponent(LightComponent.class);
        light.team = team;
        light.radius = Constants.PLAYER_DEFAULT_SIGHTDISTANCE;
        entity.add(light);

        modifyComponent.schedule(() -> {
            PhysixBodyComponent bodyComponent = engine.createComponent(PhysixBodyComponent.class);
            PhysixBodyDef bodyDef = new PhysixBodyDef(BodyDef.BodyType.DynamicBody, physixSystem)
                    .position(player.startPosition).fixedRotation(true);//.linearDamping(20);
            bodyComponent.init(bodyDef, physixSystem, entity);
            PhysixFixtureDef fixtureDef = new PhysixFixtureDef(physixSystem).groupIndex((short) -1)
                    .density(5).friction(0).shapeCircle(player.radius);
            Fixture fixture = bodyComponent.createFixture(fixtureDef);
            fixture.setUserData("body");
            PhysixFixtureDef fixtureDef2 = new PhysixFixtureDef(physixSystem)
                    .sensor(true).shapeCircle(player.radius);
            Fixture fixture2 = bodyComponent.createFixture(fixtureDef2);
            fixture2.setUserData("sensor");
            entity.add(bodyComponent);
        });
        engine.addEntity(entity);

        return entity;
    }

    public Entity createStaticEntity(String name, float x, float y, float radius, Team team) {
        Entity entity = engine.createEntity();
        PositionComponent position = engine.createComponent(PositionComponent.class);
        entity.add(position);
        position.x = x;
        position.y = y;

        EntityJson entityJson = entityJsonMap.get(name);
        assert (entityJson != null);
        for (Map.Entry<String, Map<String, String>> entry : entityJson.components.entrySet()) {
            Map<String, String> config = entry.getValue();
            Component component = createComponentFromConfig(entry.getKey(), config, team);
            if (component != null) {
                entity.add(component);
            }
        }

        PhysixModifierComponent modifyComponent = ComponentMappers.physixModifier.get(entity);
        if(modifyComponent != null) {
            modifyComponent.schedule(() -> {
                PhysixBodyComponent bodyComponent = engine.createComponent(PhysixBodyComponent.class);
                PhysixBodyDef bodyDef = new PhysixBodyDef(BodyDef.BodyType.DynamicBody, physixSystem)
                        .position(x, y).awake(false);
                bodyComponent.init(bodyDef, physixSystem, entity);
                PhysixFixtureDef fixtureDef = new PhysixFixtureDef(physixSystem).sensor(true).shapeCircle(radius);
                bodyComponent.createFixture(fixtureDef);
                entity.add(bodyComponent);
            });
        }

        engine.addEntity(entity);
        return entity;
    }

    private Component createComponentFromConfig(String type, Map<String, String> config, Team team) {
        try {
            switch (type) {
                case "EatableComponent": {
                    EatableComponent component = engine.createComponent(EatableComponent.class);
                    component.energy = Float.parseFloat(config.get("energy"));
                    component.sound = config.get("sound");
                    String powerupName = config.get("powerup");
                    if (powerupName != null) {
                        PowerupSystem powerupSystem = engine.getSystem(PowerupSystem.class);
                        component.powerup = powerupSystem.createPowerup(powerupName);
                    }
                    return component;
                }
                case "RenderTextureComponent": {
                    RenderTextureComponent component = engine.createComponent(RenderTextureComponent.class);
                    component.texture = assetManager.getTexture(config.get("image"));
                    assert(component.texture != null);
                    return component;
                }
                case "RenderAnimationComponent": {
                    RenderAnimationComponent component = engine.createComponent(RenderAnimationComponent.class);
                    component.animation = assetManager.getAnimation(config.get("animation"));
                    assert(component.animation != null);
                    return component;
                }
                case "LightComponent": {
                    LightComponent component = engine.createComponent(LightComponent.class);
                    component.team = team;
                    component.radius = Float.parseFloat(config.get("radius"));
                    component.shrinkPixelPerSecond = Float.parseFloat(config.get("shrinkPixelPerSecond"));
                    return component;
                }
                case "ItemTrapComponent": {
                    ItemTrapComponent component = engine.createComponent(ItemTrapComponent.class);
                    component.team = team;
                    component.sound = config.get("sound");
                    String powerupName = config.get("powerup");
                    if (powerupName != null) {
                        PowerupSystem powerupSystem = engine.getSystem(PowerupSystem.class);
                        component.powerup = powerupSystem.createPowerup(powerupName);
                    }
                    return component;
                }
                case "PizzaSliceComponent":
                    return engine.createComponent(PizzaSliceComponent.class);
                case "DropableComponent": {
                    DropableComponent component = engine.createComponent(DropableComponent.class);
                    component.item = config.get("item");
                    component.sound = config.get("sound");
                    component.texture = assetManager.getTexture(config.get("texture"));
                    return component;
                }
                case "PhysixBodyComponent":
                    return engine.createComponent(PhysixModifierComponent.class);
                default:
                    logger.error("Uknown component {}", type);
            }
        } catch (NumberFormatException e) {
            logger.error("Error creating component with config", e);
        }
        return null;
    }

    private static class SpawnPosition extends Vector2 implements Pool.Poolable {

        @Override
        public void reset() {
            setZero();
        }
    }

    private static class SpawnPositionPool extends Pool<SpawnPosition> {

        public SpawnPositionPool(int initialSize, int maxSize) {
            super(initialSize, maxSize);
        }

        @Override
        protected SpawnPosition newObject() {
            return new SpawnPosition();
        }
    }
}
