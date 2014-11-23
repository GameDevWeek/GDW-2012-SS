package de.hochschuletrier.gdw.ss14.game.json;

import de.hochschuletrier.gdw.commons.jackson.JacksonMapMap;
import java.util.Map;

public class EntityJson {
    public Integer frequency;
    @JacksonMapMap(String.class)
    public Map<String, Map<String, String>> components;
}
