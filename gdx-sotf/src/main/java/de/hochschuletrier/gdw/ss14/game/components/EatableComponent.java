package de.hochschuletrier.gdw.ss14.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import de.hochschuletrier.gdw.ss14.game.componentdata.Powerup;

public class EatableComponent extends Component implements Pool.Poolable {
    public float energy;
    public Powerup powerup;
    public String sound;

    @Override
    public void reset() {
        energy = 0;
        powerup = null;
        sound = null;
    }
}
