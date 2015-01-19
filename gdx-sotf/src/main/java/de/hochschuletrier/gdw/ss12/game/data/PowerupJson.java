package de.hochschuletrier.gdw.ss12.game.data;

import de.hochschuletrier.gdw.commons.jackson.JacksonList;
import de.hochschuletrier.gdw.ss12.game.data.Powerup.ModifierType;
import java.util.List;

public class PowerupJson {
    public String image;
    public PlayerEffect effect;
    public Boolean isTransferable;
    @JacksonList(Modifier.class)
    public List<Modifier> modifiers;
    
    
    public static class Modifier {
        public ModifierType type;
        public Float value;
        public Float lifetime;
    }
}
