package com.example.smartpantry.logic;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Normalises unit spellings and converts quantities to a base unit so that
 * "1 kg" in the pantry can satisfy "500 g" in a recipe.
 *
 * Base units: grams (mass), millilitres (volume), pieces (count).
 * This class has no Android dependencies so it can be unit tested on the JVM.
 */
public final class UnitConverter {

    public enum Dimension {MASS, VOLUME, COUNT, UNKNOWN}

    private UnitConverter() {
    }

    /** Maps spelling variants ("grams", "Tbsp.", "cups") onto one canonical unit. */
    public static String canonicalUnit(String unit) {
        if (unit == null) {
            return "pcs";
        }
        String u = unit.trim().toLowerCase(Locale.ROOT).replace(".", "");
        switch (u) {
            case "g":
            case "gram":
            case "grams":
            case "gm":
                return "g";
            case "kg":
            case "kgs":
            case "kilogram":
            case "kilograms":
                return "kg";
            case "ml":
            case "milliliter":
            case "milliliters":
            case "millilitre":
            case "millilitres":
                return "ml";
            case "l":
            case "liter":
            case "liters":
            case "litre":
            case "litres":
                return "l";
            case "tsp":
            case "teaspoon":
            case "teaspoons":
                return "tsp";
            case "tbsp":
            case "tbs":
            case "tablespoon":
            case "tablespoons":
                return "tbsp";
            case "cup":
            case "cups":
                return "cup";
            case "":
            case "pcs":
            case "pc":
            case "piece":
            case "pieces":
            case "whole":
            case "each":
            case "unit":
            case "units":
                return "pcs";
            default:
                return u;
        }
    }

    public static Dimension dimensionOf(String unit) {
        switch (canonicalUnit(unit)) {
            case "g":
            case "kg":
                return Dimension.MASS;
            case "ml":
            case "l":
            case "tsp":
            case "tbsp":
            case "cup":
                return Dimension.VOLUME;
            case "pcs":
                return Dimension.COUNT;
            default:
                return Dimension.UNKNOWN;
        }
    }

    /** Converts a quantity to its base unit (g, ml or pcs). */
    public static double toBaseUnits(double quantity, String unit) {
        switch (canonicalUnit(unit)) {
            case "kg":
            case "l":
                return quantity * 1000.0;
            case "tsp":
                return quantity * 5.0;
            case "tbsp":
                return quantity * 15.0;
            case "cup":
                return quantity * 240.0;
            default:
                return quantity;
        }
    }

    /** Formats 2.0 as "2" and 0.25 as "0.25" (always with a dot, regardless of device locale). */
    public static String formatQuantity(double quantity) {
        DecimalFormat format = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.US));
        return format.format(quantity);
    }
}
