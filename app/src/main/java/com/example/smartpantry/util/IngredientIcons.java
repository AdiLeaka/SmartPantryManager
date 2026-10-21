package com.example.smartpantry.util;

import com.example.smartpantry.logic.NameNormalizer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Picks an emoji icon for an ingredient name, so list rows show what the ingredient is.
 * Names go through NameNormalizer first, so "Eggs", "egg" and " EGG " all give the same icon.
 * Emoji are built from code points (not typed into the source) to avoid file-encoding problems.
 */
public final class IngredientIcons {

    private static final int DEFAULT_ICON = 0x1F37D; // plate with cutlery

    /** Normalised ingredient name -> emoji code point. */
    private static final Map<String, Integer> ICONS = new LinkedHashMap<>();

    static {
        ICONS.put("egg", 0x1F95A);
        ICONS.put("milk", 0x1F95B);
        ICONS.put("butter", 0x1F9C8);
        ICONS.put("bread", 0x1F35E);
        ICONS.put("salt", 0x1F9C2);
        ICONS.put("pasta", 0x1F35D);
        ICONS.put("tomato", 0x1F345);
        ICONS.put("garlic", 0x1F9C4);
        ICONS.put("onion", 0x1F9C5);
        ICONS.put("olive oil", 0x1F376);
        ICONS.put("vegetable oil", 0x1F376);
        ICONS.put("oil", 0x1F376);
        ICONS.put("cheese", 0x1F9C0);
        ICONS.put("rice", 0x1F35A);
        ICONS.put("carrot", 0x1F955);
        ICONS.put("soy sauce", 0x1F376);
        ICONS.put("flour", 0x1F33E);
        ICONS.put("sugar", 0x1F36C);
        ICONS.put("potato", 0x1F954);
        ICONS.put("stock", 0x1F372);
        ICONS.put("noodle", 0x1F35C);
        ICONS.put("tuna", 0x1F41F);
        ICONS.put("fish", 0x1F41F);
        ICONS.put("lemon", 0x1F34B);
        ICONS.put("oat", 0x1F963);
        ICONS.put("honey", 0x1F36F);
        ICONS.put("banana", 0x1F34C);
        ICONS.put("chicken", 0x1F357);
        ICONS.put("peanut butter", 0x1F95C);
        ICONS.put("apple", 0x1F34E);
        ICONS.put("orange", 0x1F34A);
        ICONS.put("strawberry", 0x1F353);
        ICONS.put("mushroom", 0x1F344);
        ICONS.put("beef", 0x1F969);
        ICONS.put("spinach", 0x1F96C);
        ICONS.put("lettuce", 0x1F96C);
        ICONS.put("broccoli", 0x1F966);
        ICONS.put("corn", 0x1F33D);
        ICONS.put("cucumber", 0x1F952);
        ICONS.put("pepper", 0x1F336);
        ICONS.put("yogurt", 0x1F95B);
        ICONS.put("water", 0x1F4A7);
    }

    private IngredientIcons() {
    }

    /** Returns the emoji for an ingredient, or a plate icon when the ingredient is not recognised. */
    public static String forName(String ingredientName) {
        String key = NameNormalizer.normalize(ingredientName);
        Integer exact = ICONS.get(key);
        if (exact != null) {
            return emoji(exact);
        }
        // Partial match, e.g. "chicken breast" contains "chicken". The longest matching key wins,
        // so "peanut butter" beats "butter" and "olive oil" beats "oil".
        String bestKey = null;
        for (String candidate : ICONS.keySet()) {
            if (key.contains(candidate) && (bestKey == null || candidate.length() > bestKey.length())) {
                bestKey = candidate;
            }
        }
        return emoji(bestKey == null ? DEFAULT_ICON : ICONS.get(bestKey));
    }

    private static String emoji(int codePoint) {
        return new String(Character.toChars(codePoint));
    }
}