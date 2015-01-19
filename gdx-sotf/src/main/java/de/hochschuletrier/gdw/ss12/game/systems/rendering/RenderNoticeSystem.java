package de.hochschuletrier.gdw.ss12.game.systems.rendering;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ReflectionPool;
import de.hochschuletrier.gdw.commons.gdx.assets.AssetManagerX;
import de.hochschuletrier.gdw.commons.gdx.utils.DrawUtil;
import de.hochschuletrier.gdw.ss12.game.ComponentMappers;
import de.hochschuletrier.gdw.ss12.game.Game;
import de.hochschuletrier.gdw.ss12.game.data.NoticePosition;
import de.hochschuletrier.gdw.ss12.game.data.NoticeType;
import de.hochschuletrier.gdw.ss12.game.data.Team;
import de.hochschuletrier.gdw.ss12.game.interfaces.SystemGameInitializer;
import java.util.Iterator;

public class RenderNoticeSystem extends EntitySystem implements SystemGameInitializer {

    private final ReflectionPool<Notice> noticePool = new ReflectionPool(Notice.class);
    private final Array<Notice> notices = new Array();
    private BitmapFont font;
    private final Vector2 drawPoint = new Vector2();
    private Game game;
    private NoticePosition lastPosition;

    public RenderNoticeSystem() {
        super(0);
    }

    @Override
    public void initGame(Game game, AssetManagerX assetManager) {
        this.game = game;
        font = assetManager.getFont("quartz_50");
        for (NoticeType type : NoticeType.values()) {
            if (type.isImage) {
                type.texture = assetManager.getTexture(type.content);
            }
        }
    }

    @Override
    public void update(float deltaTime) {
        boolean changed = false;
        lastPosition = null;
        for (Iterator<Notice> iterator = notices.iterator(); iterator.hasNext();) {
            Notice notice = iterator.next();
            if (!processNotice(notice, deltaTime)) {
                iterator.remove();
                noticePool.free(notice);
                changed = true;
            }
        }
        if (changed) {
            sort();
        }
    }

    protected boolean processNotice(Notice notice, float deltaTime) {
        if (notice.delay >= 0) {
            if (deltaTime >= notice.delay) {
                deltaTime -= notice.delay;
                notice.delay = -1;
                if (notice.type.sound != null) {
                    game.playAnouncerSound(notice.type.sound);
                    if (notice.type == NoticeType.GO) {
                        game.go();
                    }
                }
            } else {
                notice.delay -= deltaTime;
            }
        }
        if (notice.delay == -1 && notice.timeLeft > 0) {
            notice.timeLeft -= deltaTime;
            if (lastPosition != notice.type.position) {
                lastPosition = notice.type.position;
                draw(notice);
            }
            return notice.timeLeft > 0;
        }
        return true;
    }

    private void draw(Notice notice) {
        if (notice.type.texture == null && notice.type.content == null) {
            return;
        }

        getPosition(notice.type.position, drawPoint);

        if (notice.type.texture == null) {
            font.setColor(Color.WHITE);
            float hw = font.getBounds(notice.type.content).width / 2.0f;
            font.draw(DrawUtil.batch, notice.type.content, drawPoint.x + notice.type.x - hw, drawPoint.y + notice.type.y);
        } else {
            float hw = notice.type.texture.getWidth() / 2.0f;
            float hh = notice.type.texture.getHeight() / 2.0f;
            DrawUtil.draw(notice.type.texture, drawPoint.x + notice.type.x - hw, drawPoint.y + notice.type.y - hh);
        }
    }

    private void getPosition(NoticePosition anchor, Vector2 outPos) {
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

    public void schedule(NoticeType type, float delay, Entity entity) {
        Entity localPlayer = game.getLocalPlayer();
        if (localPlayer == entity) {
            schedule(type, delay);
        }
    }

    public void schedule(NoticeType type, float delay, Team team) {
        Entity localPlayer = game.getLocalPlayer();
        if (ComponentMappers.player.get(localPlayer).team == team) {
            schedule(type, delay);
        }
    }

    private void schedule(NoticeType type, float delay) {
        Notice notice = noticePool.obtain();
        notice.delay = delay;
        notice.timeLeft = type.displayTime;
        notice.type = type;
        notices.add(notice);
        sort();
    }

    private void sort() {
        // Sort events by position and priority, that way we can show just one item per position
        notices.sort((Notice a, Notice b) -> {
            int diff = b.type.position.ordinal() - a.type.position.ordinal();
            if (diff != 0) {
                return diff;
            }

            return b.type.priority - a.type.priority;
        });
    }

    public static class Notice implements Pool.Poolable {

        public float delay;
        public float timeLeft;
        public NoticeType type;

        @Override
        public void reset() {
            delay = 0;
            timeLeft = 0;
            type = null;
        }

    }
}
