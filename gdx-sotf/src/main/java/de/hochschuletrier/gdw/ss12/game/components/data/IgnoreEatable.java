package de.hochschuletrier.gdw.ss12.game.components.data;

import com.badlogic.ashley.core.Entity;

public class IgnoreEatable {

    public final Entity eatable;
    public float delay;

    public IgnoreEatable(Entity eatable, float delay) {
        this.eatable = eatable;
        this.delay = delay;
    }
}
