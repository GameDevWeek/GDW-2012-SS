package de.hochschuletrier.gdw.ss12.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import de.hochschuletrier.gdw.ss12.game.data.Powerup;

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
