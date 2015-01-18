package de.hochschuletrier.gdw.ss12.game.components.data;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Pool;
import java.util.ArrayList;

public class Powerup implements Pool.Poolable {

    public float expiredTime, lifetime;
    public Texture image;
    public ArrayList<Modifier> modifiers = new ArrayList();
    public boolean isTransferable;
    public PlayerEffect effect;

    @Override
    public void reset() {
        expiredTime = lifetime = 0;
        image = null;
        modifiers.clear();
        isTransferable = false;
        effect = null;
    }

    public static class Modifier implements Pool.Poolable {

        public float value;
        public float lifetime;
        public ModifierType type;

        @Override
        public void reset() {
            value = lifetime = 0;
            type = null;
        }
    }

    public static enum ModifierType {

        SPEED, SPEED_OVER_TIME, IMMUNITY, SIZE, SLIPPED, HALUCINATION
    }
}
