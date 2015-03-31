package de.hochschuletrier.gdw.ss12.game.components.factories;

import com.badlogic.ashley.core.Entity;
import de.hochschuletrier.gdw.commons.gdx.ashley.ComponentFactory;
import de.hochschuletrier.gdw.commons.utils.SafeProperties;
import de.hochschuletrier.gdw.ss12.game.components.LightComponent;

public class LightComponentFactory extends ComponentFactory<EntityFactoryParam> {

    @Override
    public String getType() {
        return "Light";
    }

    @Override
    public void run(Entity entity, SafeProperties meta, SafeProperties properties, EntityFactoryParam param) {
        LightComponent component = engine.createComponent(LightComponent.class);
        component.team = param.team;
        component.radius = properties.getFloat("radius", 0);
        component.shrinkPixelPerSecond = properties.getFloat("shrinkPixelPerSecond", 0);
        entity.add(component);
    }
}
