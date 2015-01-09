package de.hochschuletrier.gdw.ss12.game.systems.rendering;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import de.hochschuletrier.gdw.commons.gdx.utils.DrawUtil;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.components.PositionComponent;
import de.hochschuletrier.gdw.ss12.game.components.RenderTextureComponent;

public class RenderItemTextureSystem extends IteratingSystem {

    public RenderItemTextureSystem() {
        super(Family.all(PositionComponent.class, RenderTextureComponent.class).get(), 0);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        RenderTextureComponent render = ComponentMappers.renderTexture.get(entity);
        PositionComponent position = ComponentMappers.position.get(entity);

        int w = render.texture.getWidth();
        int h = render.texture.getHeight();
        DrawUtil.draw(render.texture, position.x - w * 0.5f, position.y - h * 0.5f);
    }
}
