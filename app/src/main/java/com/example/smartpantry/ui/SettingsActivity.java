package com.example.smartpantry.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.R;
import com.example.smartpantry.db.DatabaseHelper;
import com.example.smartpantry.logic.UnitConverter;
import com.example.smartpantry.util.NavHelper;
import com.example.smartpantry.util.Prefs;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Settings: expiring-soon alerts toggle, default unit for new ingredients (both stored in
 * SharedPreferences), and a button that clears the pantry.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        final DatabaseHelper db = DatabaseHelper.getInstance(this);

        SwitchMaterial alerts = findViewById(R.id.switchAlerts);
        alerts.setChecked(Prefs.isExpiryAlertsEnabled(this));
        alerts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Prefs.setExpiryAlertsEnabled(SettingsActivity.this, isChecked);
            }
        });

        final String[] units = getResources().getStringArray(R.array.units);
        Spinner spinner = findViewById(R.id.spinnerDefaultUnit);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, units));
        String current = UnitConverter.canonicalUnit(Prefs.getDefaultUnit(this));
        for (int i = 0; i < units.length; i++) {
            if (units[i].equals(current)) {
                spinner.setSelection(i);
            }
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Prefs.setDefaultUnit(SettingsActivity.this, units[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        findViewById(R.id.btnClearPantry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle(R.string.dialog_clear_title)
                        .setMessage(R.string.dialog_clear_message)
                        .setPositiveButton(R.string.btn_yes_delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.clearPantry();
                                Toast.makeText(SettingsActivity.this, R.string.toast_pantry_cleared,
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(R.string.btn_cancel, null)
                        .show();
            }
        });

        ((TextView) findViewById(R.id.txtAbout))
                .setText(getString(R.string.settings_about, db.getRecipeCount()));

        NavHelper.setup(this, (BottomNavigationView) findViewById(R.id.bottomNav), R.id.nav_settings);
    }
}
