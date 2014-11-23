package de.hochschuletrier.gdw.ss14.game.componentdata;

import com.badlogic.gdx.graphics.Color;
import de.hochschuletrier.gdw.commons.gdx.assets.AnimationExtended;
import java.util.HashMap;

public class Team {

    public int id;
    public Color color;
    public String teamname;
    public int pizzaCount = 0;
    public int wins = 0;
    public int numConnectedPlayers = 0;
    public int numSlots = 0;
    public HashMap<PlayerState, AnimationExtended> animations = new HashMap();
    
    public Team(int id, String teamname, Color color) {
        this.teamname = teamname;
        this.id = id;
        this.color = color;
    }
    
    public boolean isFull() {
        return numConnectedPlayers == numSlots;
    }
}
