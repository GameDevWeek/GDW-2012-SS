package de.hochschuletrier.gdw.ss12.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixBodyComponent;
import de.hochschuletrier.gdw.commons.gdx.physix.systems.PhysixSystem;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.components.data.PlayerState;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;

public class UpdatePlayerSystem extends IteratingSystem {

    private PhysixSystem physixSystem;

    public UpdatePlayerSystem() {
        super(Family.all(PlayerComponent.class).get(), 0);
    }
		
	@Override
	public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        physixSystem = engine.getSystem(PhysixSystem.class);
	}

	@Override
	public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        physixSystem = null;
	}

    @Override
    public void processEntity(Entity entity, float deltaTime) {
        PlayerComponent player = ComponentMappers.player.get(entity);
        PhysixBodyComponent physix = ComponentMappers.physixBody.get(entity);
        
        if(player.killer != null) {
            triggerPlayerDeath(entity, player);
            player.killer = null;//fixme: use?
        }
        else if(!player.isDead()) {
            float shrinkPixelThisFrame = Constants.PLAYER_SHRINK_PIXEL_PER_SECOND * deltaTime;
            player.radius -= shrinkPixelThisFrame * (player.radius / Constants.PLAYER_MAX_SIZE);

            // check if player radius got under min_size
            if (player.radius < Constants.PLAYER_MIN_SIZE) {
                triggerPlayerDeath(entity, player);
            } else {
                if(player.radius > Constants.PLAYER_MAX_SIZE) {
                    player.radius = Constants.PLAYER_MAX_SIZE;
                }
                setSightDistance(entity, player.radius);
            }
            
            // Adjust body and sensor size
            float radius = physixSystem.toBox2D(player.radius);
            physix.getFixtureByUserData("body").getShape().setRadius(radius);
            physix.getFixtureByUserData("sensor").getShape().setRadius(radius);
        }
    }
    
    private void triggerPlayerDeath(Entity entity, PlayerComponent player) {
        player.powerups.clear();
        player.state = PlayerState.DEAD;
        player.radius = Constants.PLAYER_DEFAULT_SIZE;
        ComponentMappers.light.get(entity).radius = 0;
        ComponentMappers.position.get(entity).ignorePhysix = true;
        ComponentMappers.physixBody.get(entity).setActive(false);
    }
    
    private void setSightDistance(Entity entity, float radius) {
        float m = (2 * radius) / (float) Constants.PLAYER_DEFAULT_SIZE;
        if (m > 1f) {
            float softM = m - 1f;
            softM /= 5f; // Korrekturfaktor, größere Radien sollen nicht so stark die Sehweite ändern
            m = softM + 1f;
        }
        float distance = Constants.PLAYER_DEFAULT_SIGHTDISTANCE * Math.max(1, m);
        ComponentMappers.light.get(entity).radius = distance;
    }

    public void resetPlayer(PlayerComponent player) {
//        getPosition().set(startPosition);
//        moveSpeed = Constants.PLAYER_MOVEMENT_SPEED;
//        setRadius(Constants.START_SIZE);
//        setAnimState(PlayerState.CHOMPING);

//        for (IPlayerRenderEffect e : player.renderEffects.values()) {
//            e.deactivate();
//        }

//        for (int i = 0; i < inventory.length; i++) {
//            inventory[i] = null;
//        }

//        setSightDistance(Constants.PLAYER_DEFAULT_SIGHTDISTANCE);

        player.powerups.clear();
    }
}
