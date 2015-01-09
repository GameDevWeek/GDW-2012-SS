package de.hochschuletrier.gdw.ss12.game.systems.rendering;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.hochschuletrier.gdw.commons.gdx.utils.DrawUtil;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.PositionComponent;
import de.hochschuletrier.gdw.ss12.game.components.RenderAnimationComponent;

public class RenderItemAnimationSystem extends IteratingSystem {

    public RenderItemAnimationSystem() {
        super(Family.all(PositionComponent.class, RenderAnimationComponent.class).get(), 0);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        RenderAnimationComponent render = ComponentMappers.renderAnimation.get(entity);
        PositionComponent position = ComponentMappers.position.get(entity);

        render.stateTime += deltaTime;
        TextureRegion keyFrame = render.animation.getKeyFrame(render.stateTime);
        int w = keyFrame.getRegionWidth();
        int h = keyFrame.getRegionHeight();
        DrawUtil.batch.draw(keyFrame, position.x - w * 0.5f, position.y - h * 0.5f, w * 0.5f, h * 0.5f, w, h, 1, 1, render.angle);
    }
}
