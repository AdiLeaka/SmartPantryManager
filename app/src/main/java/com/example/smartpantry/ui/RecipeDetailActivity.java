package com.example.smartpantry.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartpantry.R;
import com.example.smartpantry.db.DatabaseHelper;
import com.example.smartpantry.logic.RecipeMatcher;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Full ingredient list and method for the recipe id passed in the Intent.
 * Ingredients the pantry cannot cover are marked so the user can see why.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        long id = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        Recipe recipe = id == -1 ? null : db.getRecipe(id);
        if (recipe == null) {
            Toast.makeText(this, R.string.toast_recipe_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(recipe.getName());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RecipeMatcher.MatchResult result = RecipeMatcher.evaluate(recipe, db.getAllPantryItems());

        TextView status = findViewById(R.id.txtStatus);
        if (result.isStrictMatch()) {
            status.setText(R.string.detail_ready);
            status.setTextColor(ContextCompat.getColor(this, R.color.success));
        } else {
            status.setText(getString(R.string.detail_missing, result.getMissing().size()));
            status.setTextColor(ContextCompat.getColor(this, R.color.warning));
        }

        LinearLayout container = findViewById(R.id.containerIngredients);
        for (RecipeIngredient ing : recipe.getIngredients()) {
            boolean missing = result.getMissing().contains(ing);
            TextView row = new TextView(this);
            row.setText((missing ? "✗  " : "✓  ") + ing.toDisplayString());
            row.setTextSize(16);
            row.setPadding(0, 6, 0, 6);
            row.setTextColor(ContextCompat.getColor(this, missing ? R.color.danger : R.color.success));
            if (missing) {
                row.setTypeface(null, Typeface.BOLD);
            }
            container.addView(row);
        }

        StringBuilder steps = new StringBuilder();
        String[] list = recipe.getStepList();
        for (int i = 0; i < list.length; i++) {
            if (i > 0) {
                steps.append("\n\n");
            }
            steps.append(i + 1).append(". ").append(list[i]);
        }
        ((TextView) findViewById(R.id.txtSteps)).setText(steps);
    }
}
