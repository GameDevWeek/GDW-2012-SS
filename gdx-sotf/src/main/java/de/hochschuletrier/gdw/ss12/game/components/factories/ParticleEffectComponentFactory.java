package de.hochschuletrier.gdw.ss12.game.components.factories;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import de.hochschuletrier.gdw.commons.gdx.ashley.ComponentFactory;
import de.hochschuletrier.gdw.commons.utils.SafeProperties;
import de.hochschuletrier.gdw.ss12.game.components.ParticleEffectComponent;

public class ParticleEffectComponentFactory extends ComponentFactory<EntityFactoryParam> {

    @Override
    public String getType() {
        return "ParticleEffect";
    }

    @Override
    public void run(Entity entity, SafeProperties meta, SafeProperties properties, EntityFactoryParam param) {
        ParticleEffectComponent component = engine.createComponent(ParticleEffectComponent.class);
        component.effect = new ParticleEffect(assetManager.getParticleEffect(properties.getString("name")));
        component.effect.start();
        for (ParticleEmitter emitter : component.effect.getEmitters()) {
            emitter.setContinuous(false);
            emitter.duration = 0;
            emitter.durationTimer = 0;
        }
        component.effect.update(20); // bugfix for single particle playing at the start
        entity.add(component);
    }
}
