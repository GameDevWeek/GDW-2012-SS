package de.hochschuletrier.gdw.ss12.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class InputComponent extends Component implements Pool.Poolable {

    public final Vector2 moveDirection = new Vector2();
    public final Vector2 lastMoveDirection = new Vector2();
    public boolean dropItem;
    public float speed;

    @Override
    public void reset() {
        moveDirection.setZero();
        lastMoveDirection.setZero();
        dropItem = false;
        speed = 0;
    }
}
