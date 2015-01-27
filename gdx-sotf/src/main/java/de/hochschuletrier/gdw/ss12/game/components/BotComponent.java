package de.hochschuletrier.gdw.ss12.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import de.hochschuletrier.gdw.ss12.game.data.IgnoreEatable;
import java.util.HashMap;

public class BotComponent extends Component implements Pool.Poolable {

    public final Vector2 oldPos = new Vector2();
    public final Vector2 velocity = new Vector2();
    public float nextBotUpdate;
    public float lastDelta;
    public Entity followEatable;
    public final HashMap<Entity, IgnoreEatable> ignoreEatables = new HashMap();

    @Override
    public void reset() {
        oldPos.setZero();
        velocity.setZero();
        nextBotUpdate = 0;
        lastDelta = 0;
        followEatable = null;
        ignoreEatables.clear();
    }
}
