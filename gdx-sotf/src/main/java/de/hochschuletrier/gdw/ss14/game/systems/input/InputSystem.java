package de.hochschuletrier.gdw.ss14.game.systems.input;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import de.hochschuletrier.gdw.commons.gdx.physix.components.PhysixBodyComponent;
import de.hochschuletrier.gdw.ss14.game.ComponentMappers;
import de.hochschuletrier.gdw.ss14.game.components.InputComponent;
import de.hochschuletrier.gdw.ss14.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss14.game.components.PositionComponent;
import de.hochschuletrier.gdw.ss14.game.components.RenderComponent;
import de.hochschuletrier.gdw.ss14.game.components.UseableComponent;

public class InputSystem extends IteratingSystem {

    public InputSystem() {
        super(Family.all(InputComponent.class).get(), 0);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        InputComponent input = ComponentMappers.input.get(entity);
        if (input.speed > 0) {
            PlayerComponent player = ComponentMappers.player.get(entity);
            if(player.isDead())
                handleDeadMovement(entity, input, deltaTime);
            else
                handleMovement(entity, input);
        }
        if(input.dropItem) {
            drop(entity);
            input.dropItem = false;
        }
    }
    
    private void handleDeadMovement(Entity entity, InputComponent input, float deltaTime) {
        if (!input.moveDirection.isZero()) {
            input.lastMoveDirection.set(input.moveDirection);

            RenderComponent render = ComponentMappers.render.get(entity);
            render.angle = input.lastMoveDirection.angle() - 90;
            
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

            RenderComponent render = ComponentMappers.render.get(entity);
            render.angle = input.lastMoveDirection.angle() - 90;
        }
    }

    public void drop(Entity entity) {
        PlayerComponent player = ComponentMappers.player.get(entity);
        UseableComponent useable = ComponentMappers.useable.get(entity);
        if (!player.isDead() && useable != null) {
            String actionType = useable.actionType;
//            if ("drop".equals(actionType)) {
//                String value = useable.actionValue;
//                IEatable eatable = EntityFactory.createEatable(value, position, getTeam());
//                world.addItem(eatable);
//            } else if ("candle".equals(actionType)) {
//                ICandle candle = EntityFactory.createCandle(position, team);
//                world.addItem(candle);
//            }
//
//            GameWorld.playNetSound(useable.sound, position);
        }
    }
}
