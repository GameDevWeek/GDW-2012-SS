package de.hochschuletrier.gdw.ss14.game.json;

import de.hochschuletrier.gdw.commons.jackson.JacksonList;
import de.hochschuletrier.gdw.ss14.game.componentdata.Powerup.ModifierType;
import java.util.List;

public class PowerupJson {
    public String image;
    public Boolean isTransferable;
    @JacksonList(Modifier.class)
    public List<Modifier> modifiers;
    
    
    public static class Modifier {
        public ModifierType type;
        public Float value;
        public Float lifetime;
        public String effect;
    }
}
