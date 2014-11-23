package de.hochschuletrier.gdw.ss14.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class RenderComponent extends Component implements Pool.Poolable {

    public final Array<Object> renderables = new Array();
    public float stateTime;
    public float angle;
//    public float scale;
//    public boolean paused;
//    public int layer;

    @Override
    public void reset() {
        renderables.clear();
        stateTime = 0;
        angle = 0;
//        scale = 1;
//        paused = false;
//        layer = 0;
    }
}
