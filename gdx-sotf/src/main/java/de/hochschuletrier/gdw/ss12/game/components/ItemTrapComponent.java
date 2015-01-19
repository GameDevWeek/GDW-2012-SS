package de.hochschuletrier.gdw.ss12.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import de.hochschuletrier.gdw.ss12.game.data.Powerup;
import de.hochschuletrier.gdw.ss12.game.data.Team;

public class ItemTrapComponent extends Component implements Pool.Poolable {

    public String sound;
    public Powerup powerup;
    public Team team;

    @Override
    public void reset() {
        sound = null;
        powerup = null;
        team = null;
    }
}
