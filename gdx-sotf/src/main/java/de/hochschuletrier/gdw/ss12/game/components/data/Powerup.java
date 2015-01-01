package de.hochschuletrier.gdw.ss12.game.components.data;

import com.badlogic.gdx.graphics.Texture;
import java.util.ArrayList;

public class Powerup {

    public float expiredTime, lifetime;
    public Texture image;
    public ArrayList<Modifier> modifiers = new ArrayList();
    public boolean isTransferable;

    public static class Modifier {
        public float value;
        public float lifetime;
//        public String effect;
        public ModifierType type;
    }
    
    public static enum ModifierType {

        SPEED, SPEED_OVER_TIME, IMMUNITY, SIZE, SLIPPED, HALUCINATION
    }
}
