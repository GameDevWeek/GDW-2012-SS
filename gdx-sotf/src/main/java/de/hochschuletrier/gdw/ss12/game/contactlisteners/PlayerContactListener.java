package de.hochschuletrier.gdw.ss12.game.contactlisteners;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixContact;
import de.hochschuletrier.gdw.commons.gdx.physix.PhysixContactAdapter;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixBodyComponent;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.data.Powerup;
import de.hochschuletrier.gdw.ss12.game.components.EatableComponent;
import de.hochschuletrier.gdw.ss12.game.components.ItemTrapComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.DropableComponent;
import java.util.Iterator;
import java.util.List;

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
            } else {
                ItemTrapComponent otherTrap = ComponentMappers.itemTrap.get(otherEntity);
                if (otherTrap != null && otherTrap.team != myPlayer.team) {
                    stepOnTrap(myPlayer, otherTrap, otherEntity);
                }
            }
        }
    }

    private void eatEatable(PlayerComponent eater, Entity eaterEntity, EatableComponent eatable, Entity eatableEntity) {
        game.playEntitySound(eatable.sound, eaterEntity, false);

        // Wachsen des Spielers
        eater.radius += eatable.energy * Constants.PLAYER_GROW_FACTOR;

        if (ComponentMappers.pizzaSlice.has(eatableEntity)) {
            eater.team.setPizzaCount(eater.team.getPizzaCount() + 1);
        }

        copyDropable(eaterEntity, eatableEntity);

        // Powerup hinzufügen (Effekt wird in PowerupSystem abgehandelt)
        if (eatable.powerup != null) {
            eater.newPowerups.add(eatable.powerup);
            eatable.powerup = null;
        }

        engine.removeEntity(eatableEntity);
    }

    private void stepOnTrap(PlayerComponent victim, ItemTrapComponent trap, Entity trapEntity) {
        // Powerup hinzufügen (Effekt wird in PowerupSystem abgehandelt)
        if (trap.powerup != null) {
            victim.newPowerups.add(trap.powerup);
            trap.powerup = null;
        }

        engine.removeEntity(trapEntity);
    }

    private void eatPlayer(PlayerComponent killer, Entity killerEntity, PlayerComponent victim, Entity victimEntity) {
        // Wachsen des Spielers
        killer.radius += victim.radius * Constants.PLAYER_GROW_FACTOR;

        copyDropable(killerEntity, victimEntity);

        // Powerups hinzufügen (Effekt wird in PowerupSystem abgehandelt)
        if (!victim.powerups.isEmpty()) {
            addPlayerPowerups(victim.powerups, killer);
            addPlayerPowerups(victim.newPowerups, killer);
        }

        victim.killer = killerEntity;
    }

    void addPlayerPowerups(final List<Powerup> powerups, PlayerComponent killer) {
        for (Iterator<Powerup> iterator = powerups.iterator(); iterator.hasNext();) {
            Powerup powerup = iterator.next();
            if (powerup.isTransferable) {
                killer.newPowerups.add(powerup);
                iterator.remove();
            }
        }
    }

    private void copyDropable(Entity eater, Entity eatable) {
        DropableComponent dropable = ComponentMappers.dropable.get(eatable);
        if (dropable != null) {
            DropableComponent destinationDropable = ComponentMappers.dropable.get(eater);
            if (destinationDropable == null) {
                destinationDropable = engine.createComponent(DropableComponent.class);
                eater.add(destinationDropable);
            }
            destinationDropable.item = dropable.item;
            destinationDropable.sound = dropable.sound;
            destinationDropable.texture = dropable.texture;
        }
    }
}
