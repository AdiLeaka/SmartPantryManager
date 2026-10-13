package com.example.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;

import com.example.smartpantry.R;
import com.example.smartpantry.adapter.PantryAdapter;
import com.example.smartpantry.db.DatabaseHelper;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.util.ExpiryUtils;
import com.example.smartpantry.util.NavHelper;
import com.example.smartpantry.util.Prefs;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Home screen. READ: lists every pantry item from SQLite in a RecyclerView.
 * Lifecycle note: the list is reloaded in onResume(), so it is refreshed every time
 * the user returns from the Add/Edit screen (or from Settings after clearing the pantry).
 */
public class PantryActivity extends AppCompatActivity implements PantryAdapter.Listener {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private TextView txtEmpty;
    private TextView txtBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        db = DatabaseHelper.getInstance(this);
        txtEmpty = findViewById(R.id.txtEmpty);
        txtBanner = findViewById(R.id.txtBanner);

        RecyclerView recycler = findViewById(R.id.recyclerPantry);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(this);
        recycler.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PantryActivity.this, AddEditIngredientActivity.class));
            }
        });

        NavHelper.setup(this, (BottomNavigationView) findViewById(R.id.bottomNav), R.id.nav_pantry);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    private void loadPantry() {
        List<PantryItem> items = db.getAllPantryItems();
        boolean alerts = Prefs.isExpiryAlertsEnabled(this);
        adapter.setItems(items, alerts);
        txtEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        int expiring = alerts ? ExpiryUtils.countExpiringSoon(items) : 0;
        if (expiring > 0) {
            txtBanner.setText(getString(R.string.banner_expiring, expiring));
            txtBanner.setVisibility(View.VISIBLE);
        } else {
            txtBanner.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEdit(PantryItem item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(final PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, item.getName()))
                .setPositiveButton(R.string.btn_yes_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.deletePantryItem(item.getId());
                        Toast.makeText(PantryActivity.this, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
                        loadPantry();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}
