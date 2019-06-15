package com.project.travel.story.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import com.project.travel.story.R;

import androidx.appcompat.app.AppCompatDelegate;

public class NightModeUtility {

    public void initNightMode(Context context) {
        int nightMode = retrieveNightModeFromPreferences(context);
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    public int retrieveNightModeFromPreferences(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(context.getString(R.string.current_night_mode), Configuration.UI_MODE_NIGHT_NO);
    }

    public void saveNightModeToPreferences(Context context, int nightMode) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(context.getString(R.string.current_night_mode), nightMode);
        editor.apply();
    }

}
