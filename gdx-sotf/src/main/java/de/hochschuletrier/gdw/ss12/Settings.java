package de.hochschuletrier.gdw.ss12;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class Settings {

    private static final Preferences prefs = Gdx.app.getPreferences("Settings");

    public static final StringSetting PLAYER_NAME = new StringSetting("player_name");
    public static final StringSetting MAP_FILE = new StringSetting("map_file");
    public static final StringSetting LAST_HOST = new StringSetting("last_host");
    public static final IntegerSetting LAST_PORT = new IntegerSetting("last_port");

    public static void flush() {
        prefs.flush();
    }

    public static class StringSetting {

        private final String key;

        public StringSetting(String key) {
            this.key = key;
        }

        public void set(String value) {
            prefs.putString(key, value);
        }

        public String get() {
            return prefs.getString(key);
        }
    }

    public static class IntegerSetting {

        private final String key;

        public IntegerSetting(String key) {
            this.key = key;
        }

        public void set(int value) {
            prefs.putInteger(key, value);
        }

        public int get() {
            return prefs.getInteger(key);
        }
    }
}
