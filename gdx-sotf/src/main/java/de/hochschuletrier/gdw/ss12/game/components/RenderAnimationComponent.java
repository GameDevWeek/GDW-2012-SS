package de.hochschuletrier.gdw.ss12.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import de.hochschuletrier.gdw.commons.gdx.assets.AnimationExtended;

public class RenderAnimationComponent extends Component implements Pool.Poolable {

    public AnimationExtended animation;
    public float stateTime;
    public float angle;

    @Override
    public void reset() {
        animation = null;
        stateTime = 0;
        angle = 0;
    }
}
