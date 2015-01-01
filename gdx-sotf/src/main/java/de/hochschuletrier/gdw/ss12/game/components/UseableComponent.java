package de.hochschuletrier.gdw.ss12.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Pool;

public class UseableComponent extends Component implements Pool.Poolable {

    public String actionType;
    public String actionValue;
    public String sound;
    public Texture texture;

    @Override
    public void reset() {
        actionType = null;
        actionValue = null;
        sound = null;
        texture = null;
    }
}
