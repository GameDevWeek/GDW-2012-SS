package de.hochschuletrier.gdw.ss12.game;

import com.badlogic.gdx.graphics.Color;

/**
 * Enthält Balancing-Konstanten (um sie zentral zu lagern)
 * 
 * @author Robin Dick
 */
public class Constants {

    public static final int AT_THIS_NUMBER_REDUCED_ITEM_CHANCE_STARTS = 24;
    /**
     * Anzahl der Pixel mit der ein Spieler in X- oder Y-Richtung in einer
     * Sekunde bewegt werden kann
     */
    public static final float PLAYER_MOVEMENT_SPEED = 350;
    public static final float PLAYER_DEAD_MOVEMENT_SPEED = 500;
    public static final float PLAYER_MAX_SPEED = PLAYER_MOVEMENT_SPEED * 5f;
    public static final float PLAYER_MIN_SIZE = 8;
    public static final float PLAYER_MAX_SIZE = 48;
    public static final float PLAYER_DEFAULT_SIZE = 24;
    public static final float PLAYER_DEFAULT_SIGHTDISTANCE = 600;
    public static final float PLAYER_SHRINK_PIXEL_PER_SECOND = 1;
    public static final float PLAYER_GROW_FACTOR = 0.3f;
    public static final int ITEM_RADIUS = 18;
    public static final float PLAYER_RENDER_SCALE = 1.2f;
    public static final float BOT_DETECTION_RADIUS = 600;
    /**
     * Rundenkonstanten
     */
    public static final int WIN_LIMIT = 20;
    public static final int TEAMDEATHMATCH_FRAG_LIMIT = 10;
    public static final int TEAMDEATHMATCH_TIME_LIMIT = 30;
    /**
     * Minimap Constants
     */
    public static final int MINIMAP_WIDTH = 150;
    public static final int MINIMAP_HEIGHT = 150;
    public static final int MINIMAP_OFFSET_X = 30;
    public static final int MINIMAP_OFFSET_Y = 30;
    /**
     * HUD-Inventory Constants
     */
    public static final int HUD_INVENTORY_WIDTH = 40;
    public static final int HUD_INVENTORY_HEIGHT = 40;
    public static final int HUD_INVENTORY_OFFSET_X = 30;
    public static final int HUD_INVENTORY_OFFSET_Y = 30;
    /**
     * Aktive Powerups im HUD
     */
    public static final int HUD_POWERUPS_ICON_SIZE = 50;
    public static final int HUD_POWERUPS_ICON_OFFSET_X = 30;
    public static final int HUD_POWERUPS_ICON_OFFSET_Y = 200;
    /**
     * Anzeige der Pizza
     */
    public static final int HUD_PIZZA_OFFSET_Y = 30;
    /**
     * Zeit in Millisekunden bis ein nächstes Item erstellt werden könnte
     */
    public static final float TIME_TO_NEXT_ITEM_CHANCE = 0.5f;
    /**
     * Chance ein neues Item in die Welt zu spawnen in Prozent
     */
    public static final float CHANCE_FOR_NEW_ITEM = 0.5f;
    /**
     * by Ben Raus
     * Spawnrate wenn mehr als 24 Items auf der Map.
     */
    public static final float CHANCE_FOR_NEW_ITEM_REDUCED = 0.3f;
    public static final Color[] TEAM_COLOR_TABLE = {Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN};

    // PooledEngine parameters
    public static final int ENTITY_POOL_INITIAL_SIZE = 32;
    public static final int ENTITY_POOL_MAX_SIZE = 256;
    public static final int COMPONENT_POOL_INITIAL_SIZE = 32;
    public static final int COMPONENT_POOL_MAX_SIZE = 256;

    // Physix parameters
    public static final int POSITION_ITERATIONS = 3;
    public static final int VELOCITY_ITERATIONS = 8;
    public static final float STEP_SIZE = 1 / 60.0f;
    public static final int BOX2D_SCALE = 40;
}
