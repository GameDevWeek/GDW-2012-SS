package de.hochschuletrier.gdw.ss12.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Pool;

public class RenderTextureComponent extends Component implements Pool.Poolable {

    public Texture texture;
    public float angle;

    @Override
    public void reset() {
        texture = null;
        angle = 0;
    }
}
