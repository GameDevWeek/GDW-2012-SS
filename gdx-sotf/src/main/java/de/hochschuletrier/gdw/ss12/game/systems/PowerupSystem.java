package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.data.Powerup;
import de.hochschuletrier.gdw.ss12.game.components.data.Powerup.Modifier;
import de.hochschuletrier.gdw.ss12.game.components.InputComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.json.PowerupJson;
import java.util.Iterator;
import java.util.Random;

public class PowerupSystem extends IteratingSystem implements SystemGameInitializer {

    private final Random random = new Random();
    private AssetManagerX assetManager;

    public PowerupSystem() {
        super(Family.all(PlayerComponent.class, InputComponent.class).get(), 0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.assetManager = assetManager;
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

    private void initializePowerups(PlayerComponent player, InputComponent input) {
        for (Powerup powerup : player.newPowerups) {
            // Rendereffekt
//            if (powerup.effect != null) {
//                player.renderEffects.get(powerup.getEffect()).activate();
//            }
            for(Modifier modifier: powerup.modifiers) {
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
            for(Modifier modifier: powerup.modifiers) {
                switch (modifier.type) {
                    case SIZE:
                        float addSize = modifier.value * deltaTime;
                        player.radius += addSize;
                        //fixme: check
                        break;
                }
            }
        }
    }

    private float calculateSpeed(PlayerComponent player) {
        if(player.isDead()) {
            return Constants.PLAYER_DEAD_MOVEMENT_SPEED;
        }
        float speed = Constants.PLAYER_MOVEMENT_SPEED;
        for (Powerup powerup : player.powerups) {
            for(Modifier modifier: powerup.modifiers) {
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
        while(iterator.hasNext()) {
            Powerup powerup = iterator.next();
            powerup.expiredTime += deltaTime;

            // abgelaufene Powerups entfernen
            if (powerup.expiredTime >= powerup.lifetime) {
//                if (powerup.effect != null) {
//                    player.renderEffects.get(powerup.effect).deactivate();
//                }
                iterator.remove();
            }
        }
    }

    Powerup createPowerup(PowerupJson powerupJson) {
        Powerup powerup = new Powerup();//fixme: pooling
        powerup.image = assetManager.getTexture(powerupJson.image);
        powerup.isTransferable = powerupJson.isTransferable;
        for(PowerupJson.Modifier modifierJson: powerupJson.modifiers) {
            Modifier modifier = new Modifier();//fixme: pooling
            modifier.type = modifierJson.type;
            modifier.value = modifierJson.value;
            modifier.lifetime = modifierJson.lifetime;
            if(modifier.lifetime > powerup.lifetime)
                powerup.lifetime = modifier.lifetime;
            powerup.modifiers.add(modifier);
        }
        return powerup;
    }
}
