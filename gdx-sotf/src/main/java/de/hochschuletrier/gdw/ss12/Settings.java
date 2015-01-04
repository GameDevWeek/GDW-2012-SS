package de.hochschuletrier.gdw.ss12;

import de.hochschuletrier.gdw.commons.gdx.settings.BooleanSetting;
import de.hochschuletrier.gdw.commons.gdx.settings.IntegerSetting;
import de.hochschuletrier.gdw.commons.gdx.settings.FloatSetting;
import de.hochschuletrier.gdw.commons.gdx.settings.StringSetting;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import de.hochschuletrier.gdw.commons.gdx.settings.SettingsUtil;

public class Settings {

    private static final Preferences prefs = Gdx.app.getPreferences(Settings.class.getName() + ".xml");

    public static final StringSetting PLAYER_NAME = new StringSetting(prefs, "player_name", "Spieler");
    public static final StringSetting MAP_FILE = new StringSetting(prefs, "map_file", null);
    public static final StringSetting LAST_HOST = new StringSetting(prefs, "last_host", "localhost");
    public static final IntegerSetting LAST_PORT = new IntegerSetting(prefs, "last_port", 49999);

    public static final BooleanSetting FULLSCREEN = new BooleanSetting(prefs, "fullscreen", false);
    public static final FloatSetting SOUND_VOLUME = new FloatSetting(prefs, "sound_volume", 1.0f);
    public static final BooleanSetting SOUND_MUTE = new BooleanSetting(prefs, "sound_mute", false);
    public static final FloatSetting MUSIC_VOLUME = new FloatSetting(prefs, "music_volume", 1.0f);
    public static final BooleanSetting MUSIC_MUTE = new BooleanSetting(prefs, "music_mute", false);

    public static void flush() {
        prefs.flush();
    }

    public static void reset() {
        SettingsUtil.reset(Settings.class);
    }
}
