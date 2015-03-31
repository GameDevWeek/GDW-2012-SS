package de.hochschuletrier.gdw.ss12.game.components.factories;

import com.badlogic.ashley.core.Entity;
import de.hochschuletrier.gdw.commons.gdx.entityFactory.ComponentFactory;
import de.hochschuletrier.gdw.commons.utils.SafeProperties;
import de.hochschuletrier.gdw.ss12.game.GameLocal;

public abstract class SimpleComponentFactory extends ComponentFactory<EntityFactoryParam> {
    
    private final String type;
    private final Class clazz;
    private final boolean localOnly;

    public SimpleComponentFactory(String type, Class clazz, boolean localOnly) {
        this.type = type;
        this.clazz = clazz;
        this.localOnly = localOnly;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void run(Entity entity, SafeProperties meta, SafeProperties properties, EntityFactoryParam param) {
        if(!localOnly || param.game instanceof GameLocal) {
            entity.add(engine.createComponent(clazz));
        }
    }
}
