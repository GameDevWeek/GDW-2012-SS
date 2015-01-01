package de.hochschuletrier.gdw.ss14.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Pool;
import de.hochschuletrier.gdw.ss14.game.components.data.NoticeType;

/**
 *
 * @author Santo Pfingsten
 */
public class NoticeComponent extends Component implements Pool.Poolable {

    public enum Position {

        TOPLEFT, TOPMID, TOPRIGHT,
        CENTERLEFT, CENTER, CENTERRIGHT,
        BOTTOMLEFT, BOTTOMMID, BOTTOMRIGHT
    }

    public float delay;
    public float timeLeft;
    public NoticeType type;
    public String text;
    public Texture image;

    @Override
    public void reset() {
        delay = 0;
        timeLeft = 0;
        type = null;
        text = null;
        image = null;
    }

}
