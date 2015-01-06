package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.utils.Array;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.jackson.JacksonReader;
import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.data.Powerup;
import de.hochschuletrier.gdw.ss12.game.components.data.Powerup.Modifier;
import de.hochschuletrier.gdw.ss12.game.components.InputComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.data.PlayerEffect;
import de.hochschuletrier.gdw.ss12.game.components.data.Team;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemMapInitializer;
import de.hochschuletrier.gdw.ss12.game.json.PowerupJson;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PowerupSystem extends IteratingSystem implements SystemGameInitializer, SystemMapInitializer {
    private static final Logger logger = LoggerFactory.getLogger(PowerupSystem.class);

    private HashMap<String, PowerupJson> powerupJsonMap;
    private final Random random = new Random();
    private AssetManagerX assetManager;
    private Array<Team> teams;

    public PowerupSystem() {
        super(Family.all(PlayerComponent.class, InputComponent.class).get(), 0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
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
    protected void processEntity(Entity entity, float deltaTime) {
        InputComponent input = ComponentMappers.input.get(entity);
        PlayerComponent player = ComponentMappers.player.get(entity);
        initializePowerups(player, input);
        updatePowerups(player, deltaTime);
        input.speed = calculateSpeed(player);
        tickPowerups(player, deltaTime);
    }

    @Override
    public void update(float deltaTime) {
        for (Team team : teams) {
            if (team.pizzaCount >= 8) {
                team.pizzaCount -= 8;
                
                // Add Powerups to all team members alive
                for (Entity entity : getEntities()) {
                    PlayerComponent player = ComponentMappers.player.get(entity);
                    if(!player.isDead() && player.team == team) {
                        player.newPowerups.add(createPowerup("pizza"));
                        player.hasPizzaPowerup = true;
                    }
                }
//                GameEventManager.fireGameEvent(
//                        GameEventManager.PIZZABUFF_ACTIVATED, 0,
//                        team.getPlayers());
//                GameWorld.stateChanged = true;
            }
        }
        super.update(deltaTime);
    }

    private void initializePowerups(PlayerComponent player, InputComponent input) {
        for (Powerup powerup : player.newPowerups) {
            // Turn on effect and update shape size
            if (powerup.effect != null) {
                ParticleEmitter emitter = player.particleEmitters[powerup.effect.ordinal()];
                if (emitter != null) {
                    setParticleSpawnShapeSize(emitter, player.radius * powerup.effect.shapeScale);
                }
            }
            for (Modifier modifier : powerup.modifiers) {
                switch (modifier.type) {
                    case SLIPPED:
                        // Slip at random direction, but not within 60° of the current movement direction
                        float oldAngle = input.moveDirection.angle();
                        input.moveDirection.set(1, 0).setAngle(oldAngle - 30 + random.nextFloat() * 300);
                        break;
                    case HALUCINATION:
                        break;
                }
            }
            player.powerups.add(powerup);
        }
        player.newPowerups.clear();
    }

    private void updatePowerups(PlayerComponent player, float deltaTime) {
        for (Powerup powerup : player.powerups) {
            if (powerup.effect != null) {
                ParticleEmitter emitter = player.particleEmitters[powerup.effect.ordinal()];
                if (emitter != null) {
                    // Reset duration timer
                    emitter.duration = Integer.MAX_VALUE;
                    emitter.durationTimer = 0;
                }
            }

            for (Modifier modifier : powerup.modifiers) {
                switch (modifier.type) {
                    case SIZE:
                        float addSize = modifier.value * deltaTime;
                        player.radius += addSize;
                        //fixme: check
                        break;
                }
            }
        }

        // Update spawn shape size
        if (Math.abs(player.radius - player.lastSpawnShapeSize) > 5) {
            player.lastSpawnShapeSize = player.radius;

            for (Powerup powerup : player.powerups) {
                if (powerup.effect != null) {
                    ParticleEmitter emitter = player.particleEmitters[powerup.effect.ordinal()];
                    if (emitter != null) {
                        setParticleSpawnShapeSize(emitter, player.radius * powerup.effect.shapeScale);
                    }
                }
            }
        }
    }

    private void setParticleSpawnShapeSize(ParticleEmitter emitter, float size) {
        final ParticleEmitter.ScaledNumericValue spawnWidth = emitter.getSpawnWidth();
        spawnWidth.setHigh(size);
        spawnWidth.setLow(size);
        final ParticleEmitter.ScaledNumericValue spawnHeight = emitter.getSpawnHeight();
        spawnHeight.setHigh(size);
        spawnHeight.setLow(size);
        emitter.start();
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

    private void tickPowerups(PlayerComponent player, float deltaTime) {
        Iterator<Powerup> iterator = player.powerups.iterator();
        while (iterator.hasNext()) {
            Powerup powerup = iterator.next();
            powerup.expiredTime += deltaTime;

            // abgelaufene Powerups entfernen
            if (powerup.expiredTime >= powerup.lifetime) {
                // Turn off effect
                if (powerup.effect != null) {
                    ParticleEmitter emitter = player.particleEmitters[powerup.effect.ordinal()];
                    if (emitter != null) {
                        emitter.duration = 0;
                        emitter.durationTimer = 0;
                    }
                    if(powerup.effect == PlayerEffect.PIZZA) {
                        player.hasPizzaPowerup = false;
                    }
                }
                iterator.remove();
            }
        }
    }

    Powerup createPowerup(String powerupName) {
        PowerupJson powerupJson = powerupJsonMap.get(powerupName);
        Powerup powerup = new Powerup();//fixme: pooling
        powerup.image = assetManager.getTexture(powerupJson.image);
        powerup.isTransferable = powerupJson.isTransferable;
        powerup.effect = powerupJson.effect;
        for (PowerupJson.Modifier modifierJson : powerupJson.modifiers) {
            Modifier modifier = new Modifier();//fixme: pooling
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
}
