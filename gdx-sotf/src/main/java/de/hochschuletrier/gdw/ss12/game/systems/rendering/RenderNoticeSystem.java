package de.hochschuletrier.gdw.ss12.game.systems.rendering;

import com.badlogic.ashley.core.Engine;
import de.hochschuletrier.gdw.ss12.game.components.NoticeComponent;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.utils.DrawUtil;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;

public class RenderNoticeSystem extends IteratingSystem implements SystemGameInitializer {

    private BitmapFont font;
    private final Vector2 drawPoint = new Vector2();
    private Game game;
    private Engine engine;

    public RenderNoticeSystem() {
        super(Family.all(NoticeComponent.class).get(), 0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.game = game;
        font = assetManager.getFont("quartz", 50);
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        this.engine = engine;
    }

    @Override
    public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        this.engine = null;
    }

//        // Sort events by anchor and priority, that way we can show just one item per anchor
//        Collections.sort(activeEvents, new Comparator<GameEventSchedule>() {
//            @Override
//            public int compare(GameEventSchedule a, GameEventSchedule b) {
//                int diff = b.getEvent().getAnchor().ordinal() - a.getEvent().getAnchor().ordinal();
//                if (diff != 0) {
//                    return diff;
//                }
//
//                return b.getEvent().getPriority() - a.getEvent().getPriority();
//            }
//        });
    
    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        NoticeComponent notice = ComponentMappers.notice.get(entity);
        if (notice.delay > 0) {
            if (deltaTime > notice.delay) {
                deltaTime -= notice.delay;
                notice.delay = 0;
                if(notice.type.sound != null) {
                    game.playGlobalSound(notice.type.sound, 0, 0, false);
                    //Fixme: if notice.type == GO, unpause game ?
                }
            } else {
                notice.delay -= deltaTime;
            }
        }
        if (notice.delay == 0 && notice.timeLeft > 0) {
            boolean remove = false;
            if (deltaTime > notice.timeLeft) {
                notice.timeLeft = 0;
                remove = true;
            }
//            if (lastPosition != notice.position) {
//                lastPosition = notice.position;
//                draw(notice.getEvent());
//            }
            if(remove) {
                engine.removeEntity(entity);
            }
        }
    }

    public boolean shouldDraw(NoticeComponent notice) {
        return notice.delay == 0 && notice.timeLeft > 0;
    }

    public void draw(NoticeComponent notice) {
        if (notice.image == null && notice.text == null) {
            return;
        }

        getPosition(notice.type.position, drawPoint);

        if (notice.image == null) {
            font.setColor(Color.WHITE);
            float hw = font.getBounds(notice.text).width / 2.0f;
            font.draw(DrawUtil.batch, notice.text, drawPoint.x + notice.type.x - hw, drawPoint.y + notice.type.y);
        } else {
            float hw = notice.image.getWidth() / 2.0f;
            float hh = notice.image.getHeight() / 2.0f;
            DrawUtil.draw(notice.image, drawPoint.x + notice.type.x - hw, drawPoint.y + notice.type.y - hh);
        }
    }

    public void getPosition(NoticeComponent.Position anchor, Vector2 outPos) {
        switch (anchor) {
            case TOPLEFT:
                outPos.set(0, 0);
                break;
            case TOPMID:
                outPos.set(Gdx.graphics.getWidth() / 2.0f, 0);
                break;
            case TOPRIGHT:
                outPos.set(Gdx.graphics.getWidth(), 0);
                break;
            case CENTERLEFT:
                outPos.set(0, Gdx.graphics.getHeight() / 2.0f);
                break;
            case CENTER:
                outPos.set(Gdx.graphics.getWidth() / 2.0f, Gdx.graphics.getHeight() / 2.0f);
                break;
            case CENTERRIGHT:
                outPos.set(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() / 2.0f);
                break;
            case BOTTOMLEFT:
                outPos.set(0, Gdx.graphics.getHeight());
                break;
            case BOTTOMMID:
                outPos.set(Gdx.graphics.getWidth() / 2.0f, Gdx.graphics.getHeight());
                break;
            case BOTTOMRIGHT:
                outPos.set(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                break;
        }
    }
}
