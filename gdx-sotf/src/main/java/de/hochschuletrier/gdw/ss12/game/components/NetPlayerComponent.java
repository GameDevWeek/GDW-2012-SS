package de.hochschuletrier.gdw.ss12.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class NetPlayerComponent extends Component implements Pool.Poolable {

    public long id;
    public long lastSequenceId;

    @Override
    public void reset() {
        id = -1;
        lastSequenceId = -1;
    }
}
