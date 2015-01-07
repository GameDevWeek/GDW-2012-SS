package de.hochschuletrier.gdw.ss12.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Pool;

public class DropableComponent extends Component implements Pool.Poolable {

    public String item;
    public String sound;
    public Texture texture;

    @Override
    public void reset() {
        item = null;
        sound = null;
        texture = null;
    }
}
