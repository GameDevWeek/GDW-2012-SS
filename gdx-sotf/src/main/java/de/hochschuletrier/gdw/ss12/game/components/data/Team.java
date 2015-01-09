package de.hochschuletrier.gdw.ss12.game.components.data;

import com.badlogic.gdx.graphics.Color;
import de.hochschuletrier.gdw.commons.gdx.assets.AnimationExtended;
import java.util.HashMap;

public class Team {

    public int id;
    public Color color;
    public String teamName;
    public int pizzaCount = 0;
    public int wins = 0;
    public int numConnectedPlayers = 0;
    public int numPlayers = 0;
    public HashMap<PlayerState, AnimationExtended> animations = new HashMap();
    public int alivePlayers;
    
    public Team(int id, String teamName, Color color) {
        this.teamName = teamName;
        this.id = id;
        this.color = color;
    }
    
    public boolean isFull() {
        return numConnectedPlayers == numPlayers;
    }
}
