package de.hochschuletrier.gdw.ss12.game.components.factories;

import com.badlogic.ashley.core.Entity;
import de.hochschuletrier.gdw.commons.gdx.ashley.ComponentFactory;
import de.hochschuletrier.gdw.commons.utils.SafeProperties;
import de.hochschuletrier.gdw.ss12.game.GameLocal;
import de.hochschuletrier.gdw.ss12.game.components.DropableComponent;

public class DropableComponentFactory extends ComponentFactory<EntityFactoryParam> {

    @Override
    public String getType() {
        return "Dropable";
    }

    @Override
    public void run(Entity entity, SafeProperties meta, SafeProperties properties, EntityFactoryParam param) {
        if(param.game instanceof GameLocal) {
            DropableComponent component = engine.createComponent(DropableComponent.class);
            component.item = properties.getString("item");
            component.sound = properties.getString("sound");
            component.texture = assetManager.getTexture(properties.getString("texture"));
            entity.add(component);
        }
    }
}
