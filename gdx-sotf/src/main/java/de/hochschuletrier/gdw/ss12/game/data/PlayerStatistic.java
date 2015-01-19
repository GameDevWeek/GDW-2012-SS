package de.hochschuletrier.gdw.ss12.game.data;

import java.util.HashMap;

public class PlayerStatistic {

    public static class Counter {

        public int value;
    }

    public HashMap<String, Counter> eatenEntities = new HashMap();
    public int kills, deaths;

    public void reset() {
        eatenEntities.clear();
        kills = 0;
        deaths = 0;
    }

    public void addEatenEntity(String type) {
        Counter counter = eatenEntities.get(type);
        if (counter == null) {
            counter = new Counter();
            eatenEntities.put(type, counter);
        }
        counter.value++;
    }
}
