package de.hochschuletrier.gdw.ss12.game.contactlisteners;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixContact;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixContactAdapter;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixBodyComponent;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.data.Powerup;
import de.hochschuletrier.gdw.ss12.game.components.EatableComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.UseableComponent;

public class PlayerContactListener extends PhysixContactAdapter {

    private final PooledEngine engine;
    private final Game game;

    public PlayerContactListener(PooledEngine engine, Game game) {
        this.engine = engine;
        this.game = game;
    }

    @Override
    public void beginContact(PhysixContact contact) {
        PhysixBodyComponent myPhysix = contact.getMyComponent();
        PhysixBodyComponent otherPhysix = contact.getOtherComponent();
        if (myPhysix != null && otherPhysix != null) {
            Entity otherEntity = otherPhysix.getEntity();
            if (!otherEntity.isScheduledForRemoval()) {
                Entity myEntity = myPhysix.getEntity();
                PlayerComponent myPlayer = ComponentMappers.player.get(myEntity);
                if (myPlayer != null) {
                    tryToEat(myEntity, myPlayer, otherEntity);
                }
            }
        }
    }

    private void tryToEat(Entity myEntity, PlayerComponent myPlayer, Entity otherEntity) {
        // See if it's trying to eat a player
        PlayerComponent otherPlayer = ComponentMappers.player.get(otherEntity);
        if (otherPlayer != null) {
            if (myPlayer.canEat(otherPlayer)) {
                eatPlayer(myPlayer, myEntity, otherPlayer, otherEntity);
            } else if (otherPlayer.canEat(myPlayer)) {
                eatPlayer(otherPlayer, otherEntity, myPlayer, myEntity);
            }
        } else {
            // See if it's trying to eat an eatable
            EatableComponent otherEatable = ComponentMappers.eatable.get(otherEntity);
            if (otherEatable != null) {
                eatEatable(myPlayer, myEntity, otherEatable, otherEntity);
            }
        }
    }

    private void eatEatable(PlayerComponent eater, Entity eaterEntity, EatableComponent eatable, Entity eatableEntity) {
        game.playEntitySound(eatable.sound, eaterEntity, false);

        // Wachsen des Spielers
        eater.radius += eatable.energy * Constants.PLAYER_GROW_FACTOR;

        if(ComponentMappers.pizzaSlice.has(eatableEntity)) {
            eater.team.pizzaCount++;
        }
        
        copyUseable(eaterEntity, eaterEntity);
        
        // Powerup hinzufügen (Effekt wird in PowerupSystem abgehandelt)
        if (eatable.powerup != null) {
            eater.powerups.add(eatable.powerup);
        }

        engine.removeEntity(eatableEntity);
    }

    private void eatPlayer(PlayerComponent killer, Entity killerEntity, PlayerComponent victim, Entity victimEntity) {
        game.playEntitySound("player_eat_player", killerEntity, false);

        // Wachsen des Spielers
        killer.radius += victim.radius * Constants.PLAYER_GROW_FACTOR;

        copyUseable(killerEntity, victimEntity);

        // Powerups hinzufügen (Effekt wird in PowerupSystem abgehandelt)
        if (!victim.powerups.isEmpty()) {
            for (Powerup powerup : victim.powerups) {
                if(powerup.isTransferable) {
                    killer.newPowerups.add(powerup);
                }
            }
        }

        victim.killer = killerEntity;
    }
    
    private void copyUseable(Entity eater, Entity eatable) {
        if(!ComponentMappers.useable.has(eater)) {
            UseableComponent useable = ComponentMappers.useable.get(eatable);
            if(useable != null) {
                UseableComponent newUseable = engine.createComponent(UseableComponent.class);
                newUseable.actionType = useable.actionType;
                newUseable.actionValue = useable.actionValue;
                newUseable.sound = useable.sound;
                newUseable.texture = useable.texture;
                eater.add(useable);
            }
        }
    }
}
