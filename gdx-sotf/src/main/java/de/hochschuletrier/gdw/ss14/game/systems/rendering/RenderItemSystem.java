package de.hochschuletrier.gdw.ss14.game.systems.rendering;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.hochschuletrier.gdw.commons.gdx.assets.AnimationExtended;
import de.hochschuletrier.gdw.commons.gdx.utils.DrawUtil;
import de.hochschuletrier.gdw.ss14.game.ComponentMappers;
import de.hochschuletrier.gdw.ss14.game.components.EatableComponent;
import de.hochschuletrier.gdw.ss14.game.components.PositionComponent;
import de.hochschuletrier.gdw.ss14.game.components.RenderComponent;

public class RenderItemSystem extends IteratingSystem {

    public RenderItemSystem() {
        super(Family.all(PositionComponent.class, RenderComponent.class, EatableComponent.class).get(), 0);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        RenderComponent render = ComponentMappers.render.get(entity);
        PositionComponent position = ComponentMappers.position.get(entity);

        render.stateTime += deltaTime;
        for(Object obj: render.renderables) {
            if(obj instanceof Texture) {
                Texture texture = (Texture)obj;
                int w = texture.getWidth();
                int h = texture.getHeight();
                DrawUtil.draw(texture, position.x - w * 0.5f, position.y - h * 0.5f);
            } else if(obj instanceof AnimationExtended) {
                AnimationExtended animation = (AnimationExtended)obj;
                TextureRegion keyFrame = animation.getKeyFrame(render.stateTime);
                int w = keyFrame.getRegionWidth();
                int h = keyFrame.getRegionHeight();
                DrawUtil.batch.draw(keyFrame, position.x - w * 0.5f, position.y - h * 0.5f, w * 0.5f, h * 0.5f, w, h, 1, 1, render.angle);
            }
        }
    }
}
