package com.example.smartpantry.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** JVM tests for the strict-matching rule (assignment section 2.3). */
public class RecipeMatcherTest {

    private static PantryItem p(String name, double qty, String unit) {
        return new PantryItem(0, name, qty, unit, null);
    }

    private static Recipe fiveIngredientRecipe() {
        Recipe r = new Recipe(1, "Test Dish", "Cook it.");
        r.addIngredient(new RecipeIngredient("Egg", 2, "pcs"));
        r.addIngredient(new RecipeIngredient("Flour", 100, "g"));
        r.addIngredient(new RecipeIngredient("Milk", 200, "ml"));
        r.addIngredient(new RecipeIngredient("Butter", 20, "g"));
        r.addIngredient(new RecipeIngredient("Sugar", 1, "tbsp"));
        return r;
    }

    private static List<PantryItem> fullPantry() {
        return new ArrayList<>(Arrays.asList(
                p("egg", 2, "pcs"), p("flour", 100, "g"), p("milk", 200, "ml"),
                p("butter", 20, "g"), p("sugar", 1, "tbsp")));
    }

    @Test
    public void allIngredientsPresent_isStrictMatch() {
        assertTrue(RecipeMatcher.evaluate(fiveIngredientRecipe(), fullPantry()).isStrictMatch());
    }

    @Test
    public void fourOfFiveIngredients_isNotSuggested() {
        List<PantryItem> pantry = fullPantry();
        pantry.remove(4); // remove sugar
        Recipe r = fiveIngredientRecipe();
        assertFalse(RecipeMatcher.evaluate(r, pantry).isStrictMatch());
        assertTrue(RecipeMatcher.strictMatches(Arrays.asList(r), pantry).isEmpty());
        // ...but it does qualify for the separate "almost there" list.
        assertEquals(1, RecipeMatcher.almostThere(Arrays.asList(r), pantry).size());
    }

    @Test
    public void strictList_neverContainsAlmostRecipes() {
        List<PantryItem> pantry = fullPantry();
        pantry.remove(0);
        Recipe r = fiveIngredientRecipe();
        assertTrue(RecipeMatcher.strictMatches(Arrays.asList(r), pantry).isEmpty());
    }

    @Test
    public void insufficientQuantity_isNotAMatch() {
        List<PantryItem> pantry = fullPantry();
        pantry.set(1, p("flour", 50, "g"));
        assertFalse(RecipeMatcher.evaluate(fiveIngredientRecipe(), pantry).isStrictMatch());
    }

    @Test
    public void emptyPantry_matchesNothing() {
        assertTrue(RecipeMatcher.strictMatches(Arrays.asList(fiveIngredientRecipe()),
                new ArrayList<PantryItem>()).isEmpty());
    }

    @Test
    public void pluralAndCaseDifferences_stillMatch() {
        Recipe r = new Recipe(2, "Tomato thing", "");
        r.addIngredient(new RecipeIngredient("Tomato", 2, "pcs"));
        r.addIngredient(new RecipeIngredient("Potato", 1, "pcs"));
        List<PantryItem> pantry = Arrays.asList(p("TOMATOES", 3, "pcs"), p(" potatoes ", 1, "pieces"));
        assertTrue(RecipeMatcher.evaluate(r, pantry).isStrictMatch());
    }

    @Test
    public void unitDifferences_areConverted() {
        Recipe r = new Recipe(3, "Flour dish", "");
        r.addIngredient(new RecipeIngredient("Flour", 500, "g"));
        r.addIngredient(new RecipeIngredient("Milk", 250, "ml"));
        List<PantryItem> pantry = Arrays.asList(p("Flour", 1, "kg"), p("Milk", 0.5, "l"));
        assertTrue(RecipeMatcher.evaluate(r, pantry).isStrictMatch());
    }

    @Test
    public void duplicatePantryRows_areAddedTogether() {
        Recipe r = new Recipe(4, "Flour dish", "");
        r.addIngredient(new RecipeIngredient("Flour", 300, "g"));
        List<PantryItem> pantry = Arrays.asList(p("Flour", 100, "g"), p("flour", 0.2, "kg"));
        assertTrue(RecipeMatcher.evaluate(r, pantry).isStrictMatch());
    }

    @Test
    public void zeroQuantityRow_countsAsMissing() {
        Recipe r = new Recipe(5, "Egg dish", "");
        r.addIngredient(new RecipeIngredient("Egg", 1, "pcs"));
        assertFalse(RecipeMatcher.evaluate(r, Arrays.asList(p("Egg", 0, "pcs"))).isStrictMatch());
    }

    @Test
    public void recipeWithNoIngredients_isNeverSuggested() {
        Recipe r = new Recipe(6, "Empty", "");
        assertFalse(RecipeMatcher.evaluate(r, fullPantry()).isStrictMatch());
    }

    @Test
    public void normaliser_handlesCommonPlurals() {
        assertEquals("tomato", NameNormalizer.normalize("Tomatoes"));
        assertEquals("berry", NameNormalizer.normalize("berries"));
        assertEquals("egg", NameNormalizer.normalize("Eggs"));
        assertEquals("couscous", NameNormalizer.normalize("couscous"));
        assertEquals("olive oil", NameNormalizer.normalize("  Olive   Oil "));
        assertEquals("potato", NameNormalizer.normalize("potatoes"));
        assertEquals("chicken breast", NameNormalizer.normalize("Chicken Breasts"));
    }
}
