package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ReflectionPool;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.jackson.JacksonReader;
import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.EatableComponent;
import de.hochschuletrier.gdw.ss12.game.data.Powerup;
import de.hochschuletrier.gdw.ss12.game.data.Powerup.Modifier;
import de.hochschuletrier.gdw.ss12.game.components.InputComponent;
import de.hochschuletrier.gdw.ss12.game.components.ItemTrapComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.data.NoticeType;
import de.hochschuletrier.gdw.ss12.game.data.PlayerEffect;
import de.hochschuletrier.gdw.ss12.game.data.PlayerState;
import de.hochschuletrier.gdw.ss12.game.data.Team;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemMapInitializer;
import de.hochschuletrier.gdw.ss12.game.data.PowerupJson;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PowerupSystem extends IteratingSystem implements SystemGameInitializer, SystemMapInitializer {

    private static final Logger logger = LoggerFactory.getLogger(PowerupSystem.class);

    private final ReflectionPool<Powerup> powerupPool = new ReflectionPool(Powerup.class);
    private final ReflectionPool<Modifier> powerupModifierPool = new ReflectionPool(Modifier.class);
    private HashMap<String, PowerupJson> powerupJsonMap;
    private final Random random = new Random();
    private AssetManagerX assetManager;
    private Array<Team> teams;
    private Game game;

    public PowerupSystem() {
        super(Family.all(PlayerComponent.class, InputComponent.class).get(), 0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.game = game;
        this.assetManager = assetManager;

        try {
            powerupJsonMap = JacksonReader.readMap("data/json/powerups.json", PowerupJson.class);
        } catch (Exception e) {
            logger.error("Error reading powerups.json", e);
        }
    }

    @Override
    public void initMap(TiledMap map, Array<Team> teams) {
        this.teams = teams;
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(Family.all(EatableComponent.class).get(), eatableListener);
        engine.addEntityListener(Family.all(ItemTrapComponent.class).get(), itemTrapListener);
        engine.addEntityListener(Family.all(PlayerComponent.class).get(), playerListener);
    }

    @Override
    public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        engine.removeEntityListener(eatableListener);
        engine.removeEntityListener(itemTrapListener);
        engine.removeEntityListener(playerListener);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        InputComponent input = ComponentMappers.input.get(entity);
        PlayerComponent player = ComponentMappers.player.get(entity);
        if (!player.isDead()) {
            initializePowerups(entity, player, input);
            updatePowerups(player, deltaTime);
        }
        input.speed = calculateSpeed(player);
        removeExpiredPowerups(entity, player, deltaTime);
    }

    @Override
    public void update(float deltaTime) {
        for (Team team : teams) {
            if (team.pizzaCount >= 8) {
                team.pizzaCount -= 8;

                // Add Powerups to all team members alive
                for (Entity entity : getEntities()) {
                    PlayerComponent player = ComponentMappers.player.get(entity);
                    if (player.team == team) {
                        game.scheduleNoticeForPlayer(NoticeType.PIZZABUFF_ACTIVATED, 0, entity);
                        if (!player.isDead()) {
                            player.newPowerups.add(createPowerup("pizza"));
                        }
                    }
                }
            }
        }
        super.update(deltaTime);
    }

    private void initializePowerups(Entity entity, PlayerComponent player, InputComponent input) {
        for (Powerup powerup : player.newPowerups) {
            initializePowerup(powerup, entity, player, input);
        }
        player.newPowerups.clear();
    }

    void initializePowerup(Powerup powerup, Entity entity, PlayerComponent player, InputComponent input) {
        for (Modifier modifier : powerup.modifiers) {
            switch (modifier.type) {
                case SLIPPED:
                    // Avoid slipping twice
                    if (player.isSlipping()) {
                        freePowerup(powerup);
                        return;
                    }

                    // Slip at random direction, but not within 90° of the current movement direction
                    final float newAngle = input.moveDirection.angle() - 45 + random.nextFloat() * 270;
                    input.moveDirection.set(1, 0).setAngle(newAngle);
                    break;
                case HALUCINATION:
                    player.state = PlayerState.HALUCINATING;
                    break;
            }
        }

        // Turn on effect and update shape size
        player.effectBits |= powerup.effect.getBit();
        player.lastSpawnShapeSize = -100; // force shape size update
        player.powerups.add(powerup);
    }

    private void updatePowerups(PlayerComponent player, float deltaTime) {
        for (Powerup powerup : player.powerups) {
            for (Modifier modifier : powerup.modifiers) {
                switch (modifier.type) {
                    case SIZE:
                        float addSize = modifier.value * deltaTime;
                        player.radius += addSize;
                        break;
                }
            }
        }
    }

    private float calculateSpeed(PlayerComponent player) {
        if (player.isDead()) {
            return Constants.PLAYER_DEAD_MOVEMENT_SPEED;
        }
        float speed = Constants.PLAYER_MOVEMENT_SPEED;
        for (Powerup powerup : player.powerups) {
            for (Modifier modifier : powerup.modifiers) {
                switch (modifier.type) {
                    case SLIPPED:
                        if (powerup.expiredTime < (modifier.lifetime * 0.3f)) {
                            return Constants.PLAYER_MOVEMENT_SPEED * 1.3f;
                        }
                        return Constants.PLAYER_MOVEMENT_SPEED / 5f;
                    case SPEED:
                        speed *= modifier.value;
                        break;
                    case SPEED_OVER_TIME:
                        speed += (powerup.expiredTime + 1f) * (modifier.value * Constants.PLAYER_MOVEMENT_SPEED);
                        break;
                }
            }
        }
        if (speed > Constants.PLAYER_MAX_SPEED) {
            speed = Constants.PLAYER_MAX_SPEED;
        }
        return speed;
    }

    private void removeExpiredPowerups(Entity entity, PlayerComponent player, float deltaTime) {
        Iterator<Powerup> iterator = player.powerups.iterator();
        boolean checkPowerups = false;
        while (iterator.hasNext()) {
            Powerup powerup = iterator.next();
            powerup.expiredTime += deltaTime;

            // abgelaufene Powerups entfernen
            if (powerup.expiredTime >= powerup.lifetime) {
                // Turn off effect
                if (powerup.effect != null) {
                    int bit = powerup.effect.getBit();
                    if ((player.effectBits & bit) != 0) {
                        player.effectBits &= ~bit;
                    }

                    switch (powerup.effect) {
                        case HALUCINATION:
                            player.state = PlayerState.ALIVE;
                            checkPowerups = true;
                            break;
                        case PIZZA:
                            game.scheduleNoticeForPlayer(NoticeType.PIZZABUFF_DEACTIVATED, 0, entity);
                            break;
                    }
                }
                iterator.remove();
            }
        }
        if (checkPowerups) {
            for (Powerup powerup : player.powerups) {
                if (powerup.effect == PlayerEffect.HALUCINATION) {
                    player.state = PlayerState.HALUCINATING;
                    break;
                }
            }
        }
    }

    Powerup createPowerup(String powerupName) {
        PowerupJson powerupJson = powerupJsonMap.get(powerupName);
        Powerup powerup = powerupPool.obtain();
        powerup.image = assetManager.getTexture(powerupJson.image);
        powerup.isTransferable = powerupJson.isTransferable;
        powerup.effect = powerupJson.effect;

        for (PowerupJson.Modifier modifierJson : powerupJson.modifiers) {
            Modifier modifier = powerupModifierPool.obtain();
            modifier.type = modifierJson.type;
            modifier.value = modifierJson.value;
            modifier.lifetime = modifierJson.lifetime;
            if (modifier.lifetime > powerup.lifetime) {
                powerup.lifetime = modifier.lifetime;
            }
            powerup.modifiers.add(modifier);
        }
        return powerup;
    }

    private void freePowerup(Powerup powerup) {
        for (Modifier modifier : powerup.modifiers) {
            powerupModifierPool.free(modifier);
        }
        powerupPool.free(powerup);
    }

    private final EntityListener eatableListener = new EntityListener() {

        @Override
        public void entityAdded(Entity entity) {
        }

        @Override
        public void entityRemoved(Entity entity) {
            Powerup powerup = ComponentMappers.eatable.get(entity).powerup;
            if (powerup != null) {
                freePowerup(powerup);
            }
        }
    };

    private final EntityListener itemTrapListener = new EntityListener() {

        @Override
        public void entityAdded(Entity entity) {
        }

        @Override
        public void entityRemoved(Entity entity) {
            Powerup powerup = ComponentMappers.itemTrap.get(entity).powerup;
            if (powerup != null) {
                freePowerup(powerup);
            }
        }
    };

    private final EntityListener playerListener = new EntityListener() {

        @Override
        public void entityAdded(Entity entity) {
        }

        @Override
        public void entityRemoved(Entity entity) {
            removePlayerPowerups(entity, ComponentMappers.player.get(entity));
        }
    };

    public void removePlayerPowerups(Entity entity, PlayerComponent player) {
        player.effectBits = 0;
        freePowerupsList(player.powerups);
        freePowerupsList(player.newPowerups);
    }

    void freePowerupsList(List<Powerup> powerups) {
        for (Powerup powerup : powerups) {
            freePowerup(powerup);
        }
        powerups.clear();
    }
}
