package de.hochschuletrier.gdw.ss12.game.systems.rendering;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.hochschuletrier.gdw.commons.gdx.assets.AnimationExtended;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.utils.DrawUtil;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import de.hochschuletrier.gdw.ss12.game.components.data.PlayerState;
import de.hochschuletrier.gdw.ss12.game.components.PlayerComponent;
import de.hochschuletrier.gdw.ss12.game.components.PositionComponent;
import de.hochschuletrier.gdw.ss12.game.components.RenderComponent;
import java.util.Comparator;

public class RenderPlayerSystem extends SortedIteratingSystem implements SystemGameInitializer {
    private BitmapFont font;
    private Color ghostFilter = new Color(0.3f, 1.0f, 0.3f, 0.5f);
    private Game game;

    public RenderPlayerSystem() {
        super(Family.all(PositionComponent.class, PlayerComponent.class, RenderComponent.class).get(), new EntityComparator(), 0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.game = game;
        font = assetManager.getFont("verdana_24");
    }
    
	@Override
	public void update(float deltaTime) {
        forceSort();
        super.update(deltaTime);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        RenderComponent render = ComponentMappers.render.get(entity);
        PositionComponent position = ComponentMappers.position.get(entity);
        PlayerComponent player = ComponentMappers.player.get(entity);

        if(!player.isSlipping())
            render.stateTime += deltaTime;
        
        if(player.isDead()) {
            if(entity != game.getLocalPlayer())
                return;
        } else {
//            GraphicsX.pushTransform();
//            for (IPlayerRenderEffect fx : renderEffects.values()) {
//                if (fx.isActive()) {
//                    if (fx instanceof AnimationRenderEffect) {
//                        curAnimation = ((AnimationRenderEffect) fx).getAnimation();
//                    } else {
//                        fx.render(this);
//                    }
//                }
//            }
//            GraphicsX.popTransform();
        }
        Entity localPlayer = game.getLocalPlayer();
        PlayerComponent localPlayerComponent = ComponentMappers.player.get(localPlayer);
        
        AnimationExtended animation;
        if(localPlayerComponent.isHalucinating()) {
            animation = player.team.animations.get(PlayerState.HALUCINATING);
        } else {
            animation = player.team.animations.get(player.state);
        }
        
        if (animation != null) {
            DrawUtil.batch.setColor( player.isDead() ? ghostFilter : Color.WHITE);
            TextureRegion keyFrame = animation.getKeyFrame(render.stateTime);
            int width = keyFrame.getRegionWidth();
            int height = keyFrame.getRegionHeight();
            float scale = Constants.PLAYER_RENDER_SCALE * (player.radius*2)/width;
            float halfWidth = width * 0.5f;
            float halfHeight = height * 0.5f;
            
            DrawUtil.batch.draw(keyFrame, position.x-halfWidth, position.y-halfHeight, halfWidth, halfHeight, width, height, scale, scale, render.angle);
            
            // render name
            if (player.name != null && !player.isDead() && !localPlayerComponent.isHalucinating()) {
                font.setColor(entity == localPlayer ? Color.WHITE : player.team.color);
                float xOffset = font.getBounds(player.name).width / 2.0f;
                font.draw(DrawUtil.batch, player.name, position.x-xOffset, position.y-50);
            }
        }
    }

    private static class EntityComparator implements Comparator<Entity> {

        @Override
        public int compare(Entity a, Entity b) {
            PlayerComponent pa = ComponentMappers.player.get(a);
            PlayerComponent pb = ComponentMappers.player.get(b);
            if (pa != null && pb != null) {
                if (pa.isDead()) {
                    return pb.isDead() ? 0 : -1;
                } else if (pb.isDead()) {
                    return 1;
                }
                return (int) (pa.radius - pb.radius);
            } else if(pa != null) {
                return 1;
            } else if(pb != null) {
                return -1;
            }
            return 0;
        }
    }
}
