package com.example.smartpantry.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Small wrapper around SharedPreferences for the Settings screen.
 */
public final class Prefs {

    private static final String FILE = "smart_pantry_prefs";
    private static final String KEY_ALERTS = "expiry_alerts";
    private static final String KEY_DEFAULT_UNIT = "default_unit";

    private Prefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static boolean isExpiryAlertsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ALERTS, true);
    }

    public static void setExpiryAlertsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_ALERTS, enabled).apply();
    }

    public static String getDefaultUnit(Context context) {
        return prefs(context).getString(KEY_DEFAULT_UNIT, "g");
    }

    public static void setDefaultUnit(Context context, String unit) {
        prefs(context).edit().putString(KEY_DEFAULT_UNIT, unit).apply();
    }
}
