package de.hochschuletrier.gdw.ss14.game.componentdata;

import com.badlogic.ashley.core.Entity;

public class IgnoreEatable {

    public final Entity eatable;
    public float delay;

    public IgnoreEatable(Entity eatable, float delay) {
        this.eatable = eatable;
        this.delay = delay;
    }
}
