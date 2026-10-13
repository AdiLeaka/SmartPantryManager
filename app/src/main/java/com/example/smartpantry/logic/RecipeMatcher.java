package com.example.smartpantry.logic;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * THE CORE BUSINESS LOGIC OF THE APP - the strict-matching rule (assignment section 2.3).
 *
 * A recipe is only "suggested" if EVERY ingredient it needs is in the pantry
 * in at least the required quantity. One missing or insufficient ingredient
 * excludes the whole recipe from the main suggestions list.
 *
 * Robustness to real-world messiness:
 *  - names are compared after normalisation (case, spacing, plurals)
 *  - quantities are converted to a base unit (1 kg satisfies 500 g, 2 tbsp = 30 ml)
 *  - several pantry rows for the same ingredient are added together
 *  - if a pantry unit cannot be compared with the recipe unit (e.g. recipe wants
 *    "2 cups flour" but the pantry holds flour in grams) the ingredient counts as
 *    available as long as the pantry quantity is above zero, because converting
 *    between volume and weight depends on the ingredient.
 *
 * No Android dependencies, so it is unit tested on the JVM (see RecipeMatcherTest).
 */
public final class RecipeMatcher {

    private static final double EPSILON = 1e-6;

    private RecipeMatcher() {
    }

    /** The outcome of checking one recipe against the pantry. */
    public static final class MatchResult {
        private final Recipe recipe;
        private final List<RecipeIngredient> missing;

        MatchResult(Recipe recipe, List<RecipeIngredient> missing) {
            this.recipe = recipe;
            this.missing = missing;
        }

        public Recipe getRecipe() {
            return recipe;
        }

        /** Ingredients that are absent or present in too small a quantity. */
        public List<RecipeIngredient> getMissing() {
            return missing;
        }

        /** True only when nothing is missing (and the recipe actually lists ingredients). */
        public boolean isStrictMatch() {
            return missing.isEmpty() && !recipe.getIngredients().isEmpty();
        }
    }

    /** Checks every ingredient of one recipe against the pantry. */
    public static MatchResult evaluate(Recipe recipe, List<PantryItem> pantry) {
        List<RecipeIngredient> missing = new ArrayList<>();
        for (RecipeIngredient need : recipe.getIngredients()) {
            if (!hasIngredient(need, pantry)) {
                missing.add(need);
            }
        }
        return new MatchResult(recipe, missing);
    }

    /** True if the pantry holds at least the required quantity of this ingredient. */
    public static boolean hasIngredient(RecipeIngredient need, List<PantryItem> pantry) {
        String wanted = NameNormalizer.normalize(need.getName());
        UnitConverter.Dimension needDim = UnitConverter.dimensionOf(need.getUnit());

        double sameDimensionTotal = 0;
        boolean sameDimensionSeen = false;
        boolean presentInOtherUnit = false;

        for (PantryItem item : pantry) {
            if (item.getQuantity() <= 0) {
                continue;
            }
            if (!wanted.equals(NameNormalizer.normalize(item.getName()))) {
                continue;
            }
            UnitConverter.Dimension haveDim = UnitConverter.dimensionOf(item.getUnit());
            if (needDim != UnitConverter.Dimension.UNKNOWN && haveDim == needDim) {
                sameDimensionSeen = true;
                sameDimensionTotal += UnitConverter.toBaseUnits(item.getQuantity(), item.getUnit());
            } else {
                presentInOtherUnit = true;
            }
        }

        if (sameDimensionSeen) {
            double required = UnitConverter.toBaseUnits(need.getQuantity(), need.getUnit());
            return sameDimensionTotal + EPSILON >= required;
        }
        return presentInOtherUnit;
    }

    /** Recipes the user can cook right now: nothing missing at all. Sorted by name. */
    public static List<MatchResult> strictMatches(List<Recipe> recipes, List<PantryItem> pantry) {
        List<MatchResult> results = new ArrayList<>();
        for (Recipe recipe : recipes) {
            MatchResult result = evaluate(recipe, pantry);
            if (result.isStrictMatch()) {
                results.add(result);
            }
        }
        sortByName(results);
        return results;
    }

    /**
     * Optional "Almost There" list: recipes missing EXACTLY one ingredient.
     * Shown separately from, and never mixed into, the strict suggestions.
     */
    public static List<MatchResult> almostThere(List<Recipe> recipes, List<PantryItem> pantry) {
        List<MatchResult> results = new ArrayList<>();
        for (Recipe recipe : recipes) {
            MatchResult result = evaluate(recipe, pantry);
            if (result.getMissing().size() == 1 && recipe.getIngredients().size() > 1) {
                results.add(result);
            }
        }
        sortByName(results);
        return results;
    }

    private static void sortByName(List<MatchResult> results) {
        Collections.sort(results, new Comparator<MatchResult>() {
            @Override
            public int compare(MatchResult a, MatchResult b) {
                return a.getRecipe().getName().compareToIgnoreCase(b.getRecipe().getName());
            }
        });
    }
}
