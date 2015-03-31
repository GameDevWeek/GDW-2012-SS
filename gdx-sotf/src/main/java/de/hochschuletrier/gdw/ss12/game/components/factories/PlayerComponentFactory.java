package de.hochschuletrier.gdw.ss12.game.components.factories;

import com.badlogic.ashley.core.Entity;
import de.hochschuletrier.gdw.commons.gdx.entityFactory.ComponentFactory;
import de.hochschuletrier.gdw.commons.utils.SafeProperties;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.data.PlayerState;

public class PlayerComponentFactory extends ComponentFactory<EntityFactoryParam> {

    @Override
    public String getType() {
        return "Player";
    }

    @Override
    public void run(Entity entity, SafeProperties meta, SafeProperties properties, EntityFactoryParam param) {
        PlayerComponent component = engine.createComponent(PlayerComponent.class);
        component.team = param.team;
        component.state = PlayerState.ALIVE;
        component.name = "<New Player>";
        component.radius = properties.getFloat("radius");
        component.startPosition.set(param.x, param.y);
        entity.add(component);
    }
}
