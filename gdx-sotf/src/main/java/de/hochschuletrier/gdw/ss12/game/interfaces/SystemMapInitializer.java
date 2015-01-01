package de.hochschuletrier.gdw.ss12.game.interfaces;

import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.ss12.game.components.data.Team;

public interface SystemMapInitializer {
    void initMap(TiledMap map, Team teams[]);
}
