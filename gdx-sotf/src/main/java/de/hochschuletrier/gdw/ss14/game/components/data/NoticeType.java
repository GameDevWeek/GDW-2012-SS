package de.hochschuletrier.gdw.ss14.game.components.data;

import de.hochschuletrier.gdw.ss14.game.components.NoticeComponent.Position;

/**
 *
 * @author Santo Pfingsten
 */
public enum NoticeType {

    // Countdown
    THREE(1, false, "Three", "countdown_three", Position.TOPMID, 0, 0, 0),
    TWO(1, false, "Two", "countdown_two", Position.TOPMID, 0, 0, 0),
    ONE(1, false, "One", "countdown_one", Position.TOPMID, 0, 0, 0),
    GO(1, false, "Go", "countdown_go", Position.TOPMID, 0, 0, 0),
    // Death messages
    DEATH(1.5f, false, "Du wurdest gefressen!", null, Position.BOTTOMMID, 3, 0, -50),
    STARVATION(1.5f, false, "Du bist verhungert! Fressen nicht vergessen!", null, Position.BOTTOMMID, 3, 0, -50),
    ENEMY_EATEN(1.5f, false, "Sehr gut, du hast einen Gegner gefressen!", null, Position.BOTTOMMID, 1, 0, -50),
    FRIENDLY_EATEN(1.5f, false, "Oh nein, du hast einen Freund gefressen!", null, Position.BOTTOMMID, 1, 0, -50),
    // Powerups
    PIZZABUFF_ACTIVATED(1.5f, false, "Dein Team hat eine ganze Pizza gefressen!\nPizzabuff aktiviert!", null, Position.CENTER, 2, 0, -100),
    PIZZABUFF_DEATIVATED(1.5f, false, "Achtung, dein Pizzabuff verschwindet!", null, Position.CENTER, 0, 0, -50),
    // Gamestates
    ROUND_WON(1.5f, false, "Dein Team hat die Runde gewonnen!", null, Position.CENTER, 10, 0, -50),
    ROUND_LOST(1.5f, false, "Dein Team hat die Runde verloren!", null, Position.CENTER, 10, 0, -50),
    TEAM_WON(2.5f, true, "winscreen", null, Position.CENTER, 20, 0, 0), // TODO: Sound
    TEAM_LOST(2.5f, true, "losescreen", null, Position.CENTER, 20, 0, 0); // TODO: Sound

    public final float displayTime;
    public final boolean image;
    public final String content;
    public final String sound;
    public final Position position;
    public final int priority;
    public final int y;
    public final int x;

    NoticeType(float displayTime, boolean image, String content, String sound, Position position, int priority, int x, int y) {
        this.displayTime = displayTime;
        this.image = image;
        this.content = content;
        this.sound = sound;
        this.position = position;
        this.priority = priority;
        this.x = x;
        this.y = y;
    }
}
