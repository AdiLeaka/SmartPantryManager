package com.example.smartpantry.util;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.smartpantry.R;
import com.example.smartpantry.ui.PantryActivity;
import com.example.smartpantry.ui.SettingsActivity;
import com.example.smartpantry.ui.SuggestedRecipesActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

/**
 * Wires the bottom navigation bar shared by the three top-level screens.
 * Each tab is its own Activity, reached with an explicit Intent.
 */
public final class NavHelper {

    private NavHelper() {
    }

    public static void setup(final Activity activity, BottomNavigationView nav, int selectedId) {
        nav.setSelectedItemId(selectedId);
        nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int id = item.getItemId();
                if (id == selectedId(activity)) {
                    return true;
                }
                Class<?> target;
                if (id == R.id.nav_pantry) {
                    target = PantryActivity.class;
                } else if (id == R.id.nav_suggested) {
                    target = SuggestedRecipesActivity.class;
                } else {
                    target = SettingsActivity.class;
                }
                Intent intent = new Intent(activity, target);
                // Reuse the existing screen instead of stacking duplicates.
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                return true;
            }
        });
    }

    private static int selectedId(Activity activity) {
        if (activity instanceof PantryActivity) {
            return R.id.nav_pantry;
        }
        if (activity instanceof SuggestedRecipesActivity) {
            return R.id.nav_suggested;
        }
        return R.id.nav_settings;
    }
}
