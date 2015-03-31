package de.hochschuletrier.gdw.ss12.game.components.factories;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.ashley.ComponentFactory;
import de.hochschuletrier.gdw.commons.utils.SafeProperties;
import de.hochschuletrier.gdw.ss12.game.GameLocal;
import de.hochschuletrier.gdw.ss12.game.components.EatableComponent;
import de.hochschuletrier.gdw.ss12.game.systems.PowerupSystem;

public class EatableComponentFactory extends ComponentFactory<EntityFactoryParam> {

    private PowerupSystem powerupSystem;

    @Override
    public String getType() {
        return "Eatable";
    }

    @Override
    public void init(PooledEngine engine, AssetManagerX assetManager) {
        super.init(engine, assetManager);
        powerupSystem = engine.getSystem(PowerupSystem.class);
    }

    @Override
    public void run(Entity entity, SafeProperties meta, SafeProperties properties, EntityFactoryParam param) {
        if(param.game instanceof GameLocal) {
            EatableComponent component = engine.createComponent(EatableComponent.class);
            component.energy = properties.getFloat("energy", 0);
            component.sound = properties.getString("sound");
            String powerupName = properties.getString("powerup");
            if (powerupName != null) {
                component.powerup = powerupSystem.createPowerup(powerupName);
            }
            entity.add(component);
        }
    }
}
