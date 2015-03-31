package de.hochschuletrier.gdw.ss12.game.components.factories;

import com.badlogic.ashley.core.Entity;
import de.hochschuletrier.gdw.commons.gdx.ashley.ComponentFactory;
import de.hochschuletrier.gdw.commons.utils.SafeProperties;
import de.hochschuletrier.gdw.ss12.game.components.RenderTextureComponent;

public class RenderTextureComponentFactory extends ComponentFactory<EntityFactoryParam> {

    @Override
    public String getType() {
        return "RenderTexture";
    }

    @Override
    public void run(Entity entity, SafeProperties meta, SafeProperties properties, EntityFactoryParam param) {
        RenderTextureComponent component = engine.createComponent(RenderTextureComponent.class);
        component.texture = assetManager.getTexture(properties.getString("image"));
        assert (component.texture != null);
        entity.add(component);
    }
}
