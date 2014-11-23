package de.hochschuletrier.gdw.ss14.game.interfaces;

import de.hochschuletrier.gdw.commons.tiled.TiledMap;
import de.hochschuletrier.gdw.ss14.game.componentdata.Team;

public interface SystemMapInitializer {
    void initMap(TiledMap map, Team teams[]);
}
