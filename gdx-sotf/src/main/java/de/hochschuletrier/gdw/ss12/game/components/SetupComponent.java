package de.hochschuletrier.gdw.ss12.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import de.hochschuletrier.gdw.ss12.game.data.Team;

public class SetupComponent extends Component implements Pool.Poolable {

    public String name;
    public Team team;

    @Override
    public void reset() {
        name = null;
        team = null;
    }
}
