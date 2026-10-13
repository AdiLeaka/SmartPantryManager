package com.example.smartpantry.model;

import com.example.smartpantry.logic.UnitConverter;

/**
 * One ingredient line inside a recipe, e.g. "200 g Pasta" or "2 x Egg".
 */
public class RecipeIngredient {

    private final String name;
    private final double quantity;
    private final String unit;

    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    /** Human readable form used on screen: "2 × Egg" or "200 g Pasta". */
    public String toDisplayString() {
        String qty = UnitConverter.formatQuantity(quantity);
        if ("pcs".equals(UnitConverter.canonicalUnit(unit))) {
            return qty + " × " + name;
        }
        return qty + " " + unit + " " + name;
    }
}
