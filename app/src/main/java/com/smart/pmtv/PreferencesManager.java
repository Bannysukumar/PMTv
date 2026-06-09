package com.smart.pmtv;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String PREF_NAME = "pmtv_preferences";
    private static final String KEY_BACKGROUND_PLAYBACK = "background_playback";
    private static final String KEY_THEME = "app_theme";
    private static final String KEY_LAST_VOLUME = "last_volume";

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_BLUE = 2;
    public static final int THEME_AMOLED = 3;
    public static final int THEME_STUDIO = 4;

    private final SharedPreferences prefs;

    public PreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setBackgroundPlaybackEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BACKGROUND_PLAYBACK, enabled).apply();
    }

    public boolean isBackgroundPlaybackEnabled() {
        return prefs.getBoolean(KEY_BACKGROUND_PLAYBACK, true); // Default true
    }

    public void setTheme(int theme) {
        prefs.edit().putInt(KEY_THEME, theme).apply();
    }

    public int getTheme() {
        return prefs.getInt(KEY_THEME, THEME_DARK); // Default Dark
    }

    public void setLastVolume(int volume) {
        prefs.edit().putInt(KEY_LAST_VOLUME, volume).apply();
    }

    public int getLastVolume(int defaultVol) {
        return prefs.getInt(KEY_LAST_VOLUME, defaultVol);
    }
}
