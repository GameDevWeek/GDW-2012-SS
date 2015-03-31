package de.hochschuletrier.gdw.ss12.game.components.factories;

import com.badlogic.ashley.core.Entity;
import de.hochschuletrier.gdw.commons.gdx.ashley.ComponentFactory;
import de.hochschuletrier.gdw.commons.utils.SafeProperties;
import de.hochschuletrier.gdw.ss12.game.components.RenderAnimationComponent;

public class RenderAnimationComponentFactory extends ComponentFactory<EntityFactoryParam> {

    @Override
    public String getType() {
        return "RenderAnimation";
    }

    @Override
    public void run(Entity entity, SafeProperties meta, SafeProperties properties, EntityFactoryParam param) {
        RenderAnimationComponent component = engine.createComponent(RenderAnimationComponent.class);
        component.animation = assetManager.getAnimation(properties.getString("animation"));
        assert (component.animation != null);
        entity.add(component);
    }
}
