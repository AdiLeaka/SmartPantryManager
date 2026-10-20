package com.example.smartpantry.ui;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.R;
import com.example.smartpantry.db.DatabaseHelper;
import com.example.smartpantry.logic.UnitConverter;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.util.ExpiryUtils;
import com.example.smartpantry.util.Prefs;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * CREATE and UPDATE (and DELETE in edit mode) for one pantry item.
 * If the launching Intent carries EXTRA_ITEM_ID the form is in edit mode and is pre-filled
 * from the database; otherwise it adds a new item.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "item_id";
    private static final double MAX_QTY = 100000;
    /** Letters (including accented), spaces and hyphens only, so "Tomato" passes and "123" does not. */
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}][\\p{L} '\\-]*$");

    private DatabaseHelper db;
    private long itemId = -1;
    private String expiryIso;

    private TextInputLayout layoutName;
    private TextInputLayout layoutQty;
    private MaterialAutoCompleteTextView editName;
    /** Ingredient name -> unit the recipes use, shown in the name dropdown. */
    private Map<String, String> knownIngredients;
    private TextInputEditText editQty;
    private TextInputEditText editExpiry;
    private Spinner spinnerUnit;
    private String[] units;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);
        db = DatabaseHelper.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        layoutName = findViewById(R.id.layoutName);
        layoutQty = findViewById(R.id.layoutQty);
        editName = findViewById(R.id.editName);
        editQty = findViewById(R.id.editQty);
        editExpiry = findViewById(R.id.editExpiry);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnDelete = findViewById(R.id.btnDelete);
        Button btnClearExpiry = findViewById(R.id.btnClearExpiry);

        units = getResources().getStringArray(R.array.units);
        spinnerUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, units));
        spinnerUnit.setSelection(indexOfUnit(Prefs.getDefaultUnit(this)));

        // Name field: pick from the list of recipe ingredients (or type your own).
        knownIngredients = db.getKnownIngredients();
        editName.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(knownIngredients.keySet())));
        editName.setThreshold(0);
        editName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editName.showDropDown();
            }
        });
        editName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Choosing a listed ingredient also selects the unit the recipes use for it.
                String picked = (String) parent.getItemAtPosition(position);
                spinnerUnit.setSelection(indexOfUnit(knownIngredients.get(picked)));
                editQty.requestFocus();
            }
        });

        // Intent data decides add vs edit mode.
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            PantryItem item = db.getPantryItem(itemId);
            if (item == null) {
                Toast.makeText(this, R.string.toast_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            toolbar.setTitle(R.string.title_edit);
            editName.setText(item.getName(), false);
            editQty.setText(UnitConverter.formatQuantity(item.getQuantity()));
            spinnerUnit.setSelection(indexOfUnit(item.getUnit()));
            expiryIso = item.getExpiryDate();
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            toolbar.setTitle(R.string.title_add);
        }
        showExpiry();

        editExpiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDate();
            }
        });
        btnClearExpiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expiryIso = null;
                showExpiry();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });
    }

    private int indexOfUnit(String unit) {
        String wanted = UnitConverter.canonicalUnit(unit);
        for (int i = 0; i < units.length; i++) {
            if (units[i].equals(wanted)) {
                return i;
            }
        }
        return 0;
    }

    private void showExpiry() {
        editExpiry.setText(expiryIso == null ? "" : ExpiryUtils.toDisplay(expiryIso));
    }

    private void pickDate() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                expiryIso = ExpiryUtils.toIso(year, month, day);
                showExpiry();
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    /** Validates the form; saves only if every field is valid. */
    private void save() {
        layoutName.setError(null);
        layoutQty.setError(null);

        String name = String.valueOf(editName.getText()).trim().replaceAll("\\s+", " ");
        String qtyText = String.valueOf(editQty.getText()).trim().replace(',', '.');
        boolean valid = true;

        if (name.isEmpty()) {
            layoutName.setError(getString(R.string.err_name_required));
            valid = false;
        } else if (name.length() < 2 || !NAME_PATTERN.matcher(name).matches()) {
            layoutName.setError(getString(R.string.err_name_invalid));
            valid = false;
        }

        double qty = 0;
        if (qtyText.isEmpty()) {
            layoutQty.setError(getString(R.string.err_qty_required));
            valid = false;
        } else {
            try {
                qty = Double.parseDouble(qtyText);
                if (Double.isNaN(qty) || Double.isInfinite(qty)) {
                    throw new NumberFormatException();
                }
                if (qty <= 0) {
                    layoutQty.setError(getString(R.string.err_qty_positive));
                    valid = false;
                } else if (qty > MAX_QTY) {
                    layoutQty.setError(getString(R.string.err_qty_large));
                    valid = false;
                }
            } catch (NumberFormatException e) {
                layoutQty.setError(getString(R.string.err_qty_invalid));
                valid = false;
            }
        }
        if (!valid) {
            return;
        }

        String unit = units[spinnerUnit.getSelectedItemPosition()];
        PantryItem item = new PantryItem(itemId, name, qty, unit, expiryIso);
        if (itemId == -1) {
            db.insertPantryItem(item);
            Toast.makeText(this, R.string.toast_added, Toast.LENGTH_SHORT).show();
        } else {
            db.updatePantryItem(item);
            Toast.makeText(this, R.string.toast_updated, Toast.LENGTH_SHORT).show();
        }
        finish(); // PantryActivity.onResume() reloads the list
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, String.valueOf(editName.getText())))
                .setPositiveButton(R.string.btn_yes_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.deletePantryItem(itemId);
                        Toast.makeText(AddEditIngredientActivity.this, R.string.toast_deleted,
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}