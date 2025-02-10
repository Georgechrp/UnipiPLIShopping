package com.unipi.george.unipiplishopping.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String PREFERENCES_NAME = "UserSettings";

    private SharedPreferences sharedPreferences;

    public PreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public String getName() {
        return sharedPreferences.getString("fullName", "");
    }

    public void setName(String name) {
        sharedPreferences.edit().putString("fullName", name).apply();
    }

    public boolean isDarkThemeSelected() {
        return sharedPreferences.getBoolean("isDarkTheme", false);
    }

    public void setDarkThemeSelected(boolean isDarkTheme) {
        sharedPreferences.edit().putBoolean("isDarkTheme", isDarkTheme).apply();
    }

    public int getFontSize() {
        return sharedPreferences.getInt("fontSize", 16);
    }

    public void setFontSize(int fontSize) {
        sharedPreferences.edit().putInt("fontSize", fontSize).apply();
    }
}