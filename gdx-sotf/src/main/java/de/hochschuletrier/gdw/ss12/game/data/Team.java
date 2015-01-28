package de.hochschuletrier.gdw.ss12.game.data;

import com.badlogic.gdx.graphics.Color;
import de.hochschuletrier.gdw.commons.gdx.assets.AnimationExtended;
import java.util.HashMap;

public class Team {

    public final int id;
    public final Color color;
    public final String name;
    public final HashMap<PlayerState, AnimationExtended> animations = new HashMap();
    private int pizzaCount = 0;
    private int wins = 0;
    public int numConnectedPlayers = 0;
    public int numPlayers = 0;
    public int alivePlayers;
    public boolean changed;

    public Team(int id, String teamName, Color color) {
        this.name = teamName;
        this.id = id;
        this.color = color;
    }

    public boolean isFull() {
        return numConnectedPlayers == numPlayers;
    }

    public int getPizzaCount() {
        return pizzaCount;
    }

    public void setPizzaCount(int pizzaCount) {
        this.pizzaCount = pizzaCount;
        changed = true;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
        changed = true;
    }
}
