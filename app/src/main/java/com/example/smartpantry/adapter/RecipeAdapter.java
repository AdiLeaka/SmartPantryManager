package com.example.smartpantry.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.logic.RecipeMatcher;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for the Suggested Recipes screen. It displays MatchResults,
 * so the same adapter serves both the strict list and the separate "Almost there" list.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface Listener {
        void onRecipeClicked(Recipe recipe);
    }

    private final List<RecipeMatcher.MatchResult> results = new ArrayList<>();
    private final Listener listener;

    public RecipeAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setResults(List<RecipeMatcher.MatchResult> newResults) {
        results.clear();
        results.addAll(newResults);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final RecipeMatcher.MatchResult result = results.get(position);
        Context ctx = holder.itemView.getContext();
        holder.name.setText(result.getRecipe().getName());

        if (result.isStrictMatch()) {
            holder.info.setText(ctx.getString(R.string.recipe_ready_info,
                    result.getRecipe().getIngredients().size()));
            holder.info.setTextColor(ContextCompat.getColor(ctx, R.color.success));
        } else {
            StringBuilder missing = new StringBuilder();
            for (RecipeIngredient ing : result.getMissing()) {
                if (missing.length() > 0) {
                    missing.append(", ");
                }
                missing.append(ing.toDisplayString());
            }
            holder.info.setText(ctx.getString(R.string.recipe_missing_info, missing));
            holder.info.setTextColor(ContextCompat.getColor(ctx, R.color.warning));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRecipeClicked(result.getRecipe());
            }
        });
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView info;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txtRecipeName);
            info = itemView.findViewById(R.id.txtRecipeInfo);
        }
    }
}
