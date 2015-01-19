package de.hochschuletrier.gdw.ss12.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import de.hochschuletrier.gdw.ss12.game.data.Team;

public class LightComponent extends Component implements Pool.Poolable {
    
    public float radius;
    public float shrinkPixelPerSecond;
    public Team team;

    @Override
    public void reset() {
        radius = 0;
        shrinkPixelPerSecond = 0;
        team = null;
    }
}
