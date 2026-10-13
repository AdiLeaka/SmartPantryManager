package com.example.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.adapter.RecipeAdapter;
import com.example.smartpantry.db.DatabaseHelper;
import com.example.smartpantry.logic.RecipeMatcher;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.util.NavHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

/**
 * Runs the strict-matching logic against the current pantry. The main list holds ONLY recipes
 * where every ingredient is available. The optional "Almost there" list (exactly one missing
 * ingredient) is a separate tab and is never mixed into the main list.
 * Matching is re-run in onResume(), so adding or removing a pantry item immediately changes
 * the suggestions the next time this screen is shown.
 */
public class SuggestedRecipesActivity extends AppCompatActivity implements RecipeAdapter.Listener {

    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private TextView txtInfo;
    private TextView txtEmpty;
    private Button btnReady;
    private Button btnAlmost;
    private boolean showAlmost = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        db = DatabaseHelper.getInstance(this);

        txtInfo = findViewById(R.id.txtInfo);
        txtEmpty = findViewById(R.id.txtEmpty);
        btnReady = findViewById(R.id.btnReady);
        btnAlmost = findViewById(R.id.btnAlmost);

        RecyclerView recycler = findViewById(R.id.recyclerRecipes);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this);
        recycler.setAdapter(adapter);

        btnReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlmost = false;
                refresh();
            }
        });
        btnAlmost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlmost = true;
                refresh();
            }
        });

        NavHelper.setup(this, (BottomNavigationView) findViewById(R.id.bottomNav), R.id.nav_suggested);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        List<PantryItem> pantry = db.getAllPantryItems();
        List<Recipe> recipes = db.getAllRecipes();

        List<RecipeMatcher.MatchResult> results = showAlmost
                ? RecipeMatcher.almostThere(recipes, pantry)
                : RecipeMatcher.strictMatches(recipes, pantry);

        adapter.setResults(results);
        txtInfo.setText(getString(showAlmost ? R.string.info_almost : R.string.info_ready, results.size()));
        txtEmpty.setText(showAlmost ? R.string.empty_almost : R.string.empty_suggestions);
        txtEmpty.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);

        // Highlight the active tab: filled = selected, outlined = not selected.
        btnReady.setAlpha(showAlmost ? 0.5f : 1f);
        btnAlmost.setAlpha(showAlmost ? 1f : 0.7f);
    }

    @Override
    public void onRecipeClicked(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
