package de.hochschuletrier.gdw.ss12.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class PositionComponent extends Component implements Pool.Poolable {

    public float x;
    public float y;
    public boolean ignorePhysix;

    @Override
    public void reset() {
        x = y = 0;
        ignorePhysix = false;
    }
}
