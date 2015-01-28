package de.hochschuletrier.gdw.ss12.game.data;

import com.badlogic.gdx.math.Vector2;

public class PlayerUpdate {

    public long netId;
    public final Vector2 position = new Vector2();
    public float speed;
    public float angle;
    public float radius;
    public int effectBits;
    public PlayerState state;
    //todo:    public final PlayerStatistic statistic = new PlayerStatistic();
}
