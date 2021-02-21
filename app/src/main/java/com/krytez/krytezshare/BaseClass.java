package com.krytez.krytezshare;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class BaseClass extends Application {
    SharedPreferences settingsPreferences;
    SharedPreferences.Editor settingsPreferencesEditor;
    public void onCreate() {
        super.onCreate();

        settingsPreferences=getSharedPreferences("settings",MODE_PRIVATE);
        settingsPreferencesEditor=settingsPreferences.edit();
        int theme=settingsPreferences.getInt("theme",-2); //-2 is not found
        if(theme==-2)
        {
            theme= AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            settingsPreferencesEditor.putInt("theme",theme);
            settingsPreferencesEditor.commit();
        }
        AppCompatDelegate.setDefaultNightMode(theme);
    }
}
