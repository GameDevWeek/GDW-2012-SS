package de.hochschuletrier.gdw.ss12.game.systems.input;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixBodyComponent;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.components.InputComponent;
import de.hochschuletrier.gdw.ss12.game.components.ItemTrapComponent;
import de.hochschuletrier.gdw.ss12.game.components.LightComponent;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.PositionComponent;
import de.hochschuletrier.gdw.ss12.game.components.DropableComponent;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.systems.EntitySpawnSystem;

public class InputSystem extends IteratingSystem implements SystemGameInitializer {

    private Engine engine;
    private Game game;

    public InputSystem() {
        super(Family.all(InputComponent.class).get(), 0);
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        this.engine = engine;
    }

    @Override
    public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        this.engine = null;
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.game = game;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        InputComponent input = ComponentMappers.input.get(entity);
        if (input.speed > 0) {
            PlayerComponent player = ComponentMappers.player.get(entity);
            if (player.isDead()) {
                handleDeadMovement(entity, input, deltaTime);
            } else {
                handleMovement(entity, input);
            }
        }
        if (input.dropItem) {
            drop(entity);
            input.dropItem = false;
        }
    }

    private void handleDeadMovement(Entity entity, InputComponent input, float deltaTime) {
        if (!input.moveDirection.isZero()) {
            input.lastMoveDirection.set(input.moveDirection);

            PlayerComponent player = ComponentMappers.player.get(entity);
            player.angle = input.lastMoveDirection.angle() - 90;

            PositionComponent position = ComponentMappers.position.get(entity);
            position.x += input.moveDirection.x * input.speed * deltaTime;
            position.y += input.moveDirection.y * input.speed * deltaTime;
        }
    }

    private void handleMovement(Entity entity, InputComponent input) {
        PhysixBodyComponent physix = ComponentMappers.physixBody.get(entity);
        physix.setLinearVelocity(input.moveDirection.x * input.speed, input.moveDirection.y * input.speed);
        if (!input.moveDirection.isZero()) {
            input.lastMoveDirection.set(input.moveDirection);

            PlayerComponent player = ComponentMappers.player.get(entity);
            player.angle = input.lastMoveDirection.angle() - 90;
        }
    }

    public void drop(Entity entity) {
        PlayerComponent player = ComponentMappers.player.get(entity);
        DropableComponent dropable = ComponentMappers.dropable.get(entity);
        if (!player.isDead() && dropable != null) {
            PositionComponent position = ComponentMappers.position.get(entity);
            EntitySpawnSystem spawnSystem = engine.getSystem(EntitySpawnSystem.class);
            Entity droppedEntity = spawnSystem.createStaticEntity(dropable.item, position.x, position.y, Constants.ITEM_RADIUS);

            LightComponent light = ComponentMappers.light.get(droppedEntity);
            if (light != null) {
                light.team = player.team;
            }

            ItemTrapComponent trap = ComponentMappers.itemTrap.get(droppedEntity);
            if (trap != null) {
                trap.team = player.team;
            }

            game.playEntitySound(dropable.sound, entity, false);
            entity.remove(DropableComponent.class);
        }
    }
}
