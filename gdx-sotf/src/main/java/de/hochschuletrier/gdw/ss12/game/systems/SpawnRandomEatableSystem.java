package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.jackson.JacksonReader;
import de.hochschuletrier.gdw.commons.tiled.Layer;
import de.hochschuletrier.gdw.commons.tiled.TileInfo;
import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.data.Team;
import de.hochschuletrier.gdw.ss12.game.components.EatableComponent;
import de.hochschuletrier.gdw.ss12.game.components.PositionComponent;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemMapInitializer;
import de.hochschuletrier.gdw.ss12.game.data.EntityJson;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpawnRandomEatableSystem extends EntitySystem implements SystemGameInitializer, SystemMapInitializer, EntityListener {

    private static final Logger logger = LoggerFactory.getLogger(EntitySystem.class);

    private ImmutableArray<Entity> eatables;
    private Stack<String> nextEatables = new Stack();
    private Stack<String> usedEatables = new Stack();

    private PooledEngine engine;
    private final SpawnPositionPool spawnPositionPool = new SpawnPositionPool(256, 512);
    private final Array<SpawnPosition> spawnPositions = new Array();
    private float timeSincelastItemSpawnTry;
    private final Random random = new Random();

    public SpawnRandomEatableSystem() {
        super(0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        nextEatables.clear();
        usedEatables.clear();

        try {
            HashMap<String, EntityJson> entityJsonMap = JacksonReader.readMap("data/json/entities.json", EntityJson.class);
            for (Map.Entry<String, EntityJson> entry : entityJsonMap.entrySet()) {
                for (int i = 0; i < entry.getValue().frequency; i++) {
                    nextEatables.push(entry.getKey());
                }
            }
            Collections.shuffle(nextEatables);
        } catch (Exception e) {
            logger.error("Error reading entities.json", e);
        }
    }

    @Override
    public void initMap(TiledMap map, Array<Team> teams) {
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
        EntitySpawnSystem spawnSystem = engine.getSystem(EntitySpawnSystem.class);
        spawnSystem.createStaticEntity(eatableName, position.x, position.y, Constants.ITEM_RADIUS, null);
    }

    @Override
    public void addedToEngine(Engine engine) {
        this.engine = (PooledEngine) engine;
        engine.addEntityListener(this);
        eatables = engine.getEntitiesFor(Family.all(EatableComponent.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {
        this.engine = null;
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
