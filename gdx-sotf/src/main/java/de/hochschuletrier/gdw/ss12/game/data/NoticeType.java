package de.hochschuletrier.gdw.ss12.game.data;

import com.badlogic.gdx.graphics.Texture;

/**
 *
 * @author Santo Pfingsten
 */
public enum NoticeType {

    // Countdown
    THREE(1, false, "Three", "countdown_three", NoticePosition.TOPMID, 0, 0, 0),
    TWO(1, false, "Two", "countdown_two", NoticePosition.TOPMID, 0, 0, 0),
    ONE(1, false, "One", "countdown_one", NoticePosition.TOPMID, 0, 0, 0),
    GO(1, false, "Go", "countdown_go", NoticePosition.TOPMID, 0, 0, 0),
    // Death messages
    DEATH(1.5f, false, "Du wurdest gefressen!", null, NoticePosition.BOTTOMMID, 3, 0, -50),
    STARVATION(1.5f, false, "Du bist verhungert! Fressen nicht vergessen!", null, NoticePosition.BOTTOMMID, 3, 0, -50),
    ENEMY_EATEN(1.5f, false, "Sehr gut, du hast einen Gegner gefressen!", null, NoticePosition.BOTTOMMID, 1, 0, -50),
    FRIENDLY_EATEN(1.5f, false, "Oh nein, du hast einen Freund gefressen!", null, NoticePosition.BOTTOMMID, 1, 0, -50),
    // Powerups
    PIZZABUFF_ACTIVATED(1.5f, false, "Dein Team hat eine ganze Pizza gefressen!\nPizzabuff aktiviert!", null, NoticePosition.CENTER, 2, 0, -100),
    PIZZABUFF_DEACTIVATED(1.5f, false, "Achtung, dein Pizzabuff verschwindet!", null, NoticePosition.CENTER, 0, 0, -50),
    // Gamestates
    ROUND_WON(1.5f, false, "Dein Team hat die Runde gewonnen!", null, NoticePosition.CENTER, 10, 0, -50),
    ROUND_LOST(1.5f, false, "Dein Team hat die Runde verloren!", null, NoticePosition.CENTER, 10, 0, -50),
    TEAM_WON(2.5f, true, "winscreen", null, NoticePosition.CENTER, 20, 0, 0), // TODO: Sound
    TEAM_LOST(2.5f, true, "losescreen", null, NoticePosition.CENTER, 20, 0, 0); // TODO: Sound

    public final float displayTime;
    public final boolean isImage;
    public final String content;
    public final String sound;
    public final NoticePosition position;
    public final int priority;
    public final int y;
    public final int x;
    public Texture texture;

    NoticeType(float displayTime, boolean isImage, String content, String sound, NoticePosition position, int priority, int x, int y) {
        this.displayTime = displayTime;
        this.isImage = isImage;
        this.content = content;
        this.sound = sound;
        this.position = position;
        this.priority = priority;
        this.x = x;
        this.y = y;
    }
}
