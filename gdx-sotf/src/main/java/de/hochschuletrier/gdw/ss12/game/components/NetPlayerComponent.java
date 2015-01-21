package de.hochschuletrier.gdw.ss12.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class NetPlayerComponent extends Component implements Pool.Poolable {
    
    public long id;
    public int dropItemCount;

    @Override
    public void reset() {
        id = -1;
        dropItemCount = 0;
    }
}
