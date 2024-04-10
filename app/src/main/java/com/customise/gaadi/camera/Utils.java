package com.customise.gaadi.camera;

import android.content.Context;
import android.content.SharedPreferences;

public class Utils {

    private static final String prefs = "NeonPrefs";

    public static void saveStringSharedPreference(Context context, String key, String value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefs, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if ((key != null) && !key.isEmpty()) {
            editor.putString(key, value);
            editor.apply();
        }
    }

    public static String getStringSharedPreference(Context context, String key, String defaultValue) {
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(prefs, Context.MODE_PRIVATE);

        if (preferences.contains(key)) {
            return preferences.getString(key, defaultValue);
        } else {
            return defaultValue;
        }
    }
}
