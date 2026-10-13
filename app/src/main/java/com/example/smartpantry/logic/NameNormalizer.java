package com.example.smartpantry.logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Turns an ingredient name into a comparable "key" so that trivial differences
 * do not break matching: "Tomatoes", "tomato " and "TOMATO" all become "tomato".
 *
 * This is deliberately a small rule-based singulariser, not full NLP. Both the
 * pantry name and the recipe name go through the same function, so the result
 * only has to be consistent, not linguistically perfect.
 */
public final class NameNormalizer {

    private static final Pattern BRACKETS = Pattern.compile("\\(.*?\\)");
    private static final Pattern NON_LETTERS = Pattern.compile("[^a-z ]");
    private static final Pattern SPACES = Pattern.compile("\\s+");

    /** Words that end in "s" but are already singular. */
    private static final Set<String> KEEP = new HashSet<>();
    /** Irregular plurals and spelling variants. */
    private static final Map<String, String> IRREGULAR = new HashMap<>();

    static {
        KEEP.add("hummus");
        KEEP.add("couscous");
        KEEP.add("asparagus");
        KEEP.add("molasses");
        KEEP.add("swiss");
        KEEP.add("citrus");

        IRREGULAR.put("leaves", "leaf");
        IRREGULAR.put("loaves", "loaf");
        IRREGULAR.put("halves", "half");
        IRREGULAR.put("chilies", "chili");
        IRREGULAR.put("chillies", "chili");
        IRREGULAR.put("chilli", "chili");
        IRREGULAR.put("chile", "chili");
        IRREGULAR.put("chiles", "chili");
    }

    private NameNormalizer() {
    }

    /** Returns the comparison key for an ingredient name. */
    public static String normalize(String name) {
        if (name == null) {
            return "";
        }
        String s = name.toLowerCase(Locale.ROOT).trim();
        s = BRACKETS.matcher(s).replaceAll(" ");
        s = NON_LETTERS.matcher(s).replaceAll(" ");
        s = SPACES.matcher(s).replaceAll(" ").trim();
        if (s.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (String word : s.split(" ")) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(singular(word));
        }
        return result.toString();
    }

    /** Reduces a single lower-case word to its singular form. */
    static String singular(String w) {
        if (IRREGULAR.containsKey(w)) {
            return IRREGULAR.get(w);
        }
        if (w.length() <= 3 || KEEP.contains(w)) {
            return w;
        }
        if (w.endsWith("ies")) {
            return w.substring(0, w.length() - 3) + "y";   // berries -> berry
        }
        if (w.endsWith("oes")) {
            return w.substring(0, w.length() - 2);          // tomatoes -> tomato
        }
        if (w.endsWith("ches") || w.endsWith("shes") || w.endsWith("sses") || w.endsWith("xes")) {
            return w.substring(0, w.length() - 2);          // radishes -> radish
        }
        if (w.endsWith("s") && !w.endsWith("ss") && !w.endsWith("us") && !w.endsWith("is")) {
            return w.substring(0, w.length() - 1);          // eggs -> egg
        }
        return w;
    }
}
