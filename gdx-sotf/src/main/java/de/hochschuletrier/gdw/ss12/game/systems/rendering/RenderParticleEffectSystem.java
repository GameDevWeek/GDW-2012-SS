package de.hochschuletrier.gdw.ss12.game.systems.rendering;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import de.hochschuletrier.gdw.commons.gdx.utils.DrawUtil;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.components.ParticleEffectComponent;
import de.hochschuletrier.gdw.ss12.game.components.PositionComponent;

public class RenderParticleEffectSystem extends IteratingSystem {

    public RenderParticleEffectSystem() {
        super(Family.all(PositionComponent.class, ParticleEffectComponent.class).get(), 0);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PositionComponent position = ComponentMappers.position.get(entity);
        ParticleEffectComponent component = ComponentMappers.particleEffect.get(entity);

        component.effect.setPosition(position.x, position.y);
        component.effect.draw(DrawUtil.batch, deltaTime);
    }
}
