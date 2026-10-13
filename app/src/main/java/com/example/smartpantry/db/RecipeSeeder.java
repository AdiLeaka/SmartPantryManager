package com.example.smartpantry.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * Pre-loads the recipe collection the first time the app runs.
 * Each ingredient is {name, quantity, unit}. Units: g, kg, ml, l, tsp, tbsp, cup, pcs.
 */
final class RecipeSeeder {

    private RecipeSeeder() {
    }

    private static Object[] i(String name, double qty, String unit) {
        return new Object[]{name, qty, unit};
    }

    static void seed(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            DatabaseHelper.insertRecipe(db, "Scrambled Eggs on Toast",
                    new String[]{"Beat the eggs with the milk and a pinch of salt.",
                            "Melt the butter in a pan over low heat.",
                            "Pour in the eggs and stir gently until just set.",
                            "Toast the bread and serve the eggs on top."},
                    new Object[][]{i("Egg", 3, "pcs"), i("Milk", 50, "ml"), i("Butter", 10, "g"),
                            i("Bread", 2, "pcs"), i("Salt", 0.25, "tsp")});

            DatabaseHelper.insertRecipe(db, "Tomato Pasta",
                    new String[]{"Boil the pasta in salted water until al dente.",
                            "Fry the garlic and onion in olive oil until soft.",
                            "Add the chopped tomatoes and simmer for 10 minutes.",
                            "Toss the drained pasta through the sauce and serve."},
                    new Object[][]{i("Pasta", 200, "g"), i("Tomato", 4, "pcs"), i("Garlic", 2, "pcs"),
                            i("Onion", 1, "pcs"), i("Olive oil", 2, "tbsp"), i("Salt", 0.5, "tsp")});

            DatabaseHelper.insertRecipe(db, "Cheese Omelette",
                    new String[]{"Whisk the eggs with a pinch of salt.",
                            "Melt the butter in a non-stick pan.",
                            "Pour in the eggs, cook until nearly set, then add the cheese.",
                            "Fold the omelette in half and serve hot."},
                    new Object[][]{i("Egg", 3, "pcs"), i("Cheese", 50, "g"), i("Butter", 10, "g"),
                            i("Salt", 0.25, "tsp")});

            DatabaseHelper.insertRecipe(db, "Fried Rice",
                    new String[]{"Cook the rice ahead and let it cool.",
                            "Scramble the eggs in a hot pan and set aside.",
                            "Stir-fry the onion and carrot in oil for 3 minutes.",
                            "Add the rice and soy sauce, toss with the eggs and serve."},
                    new Object[][]{i("Rice", 200, "g"), i("Egg", 2, "pcs"), i("Onion", 1, "pcs"),
                            i("Carrot", 1, "pcs"), i("Soy sauce", 2, "tbsp"), i("Vegetable oil", 1, "tbsp")});

            DatabaseHelper.insertRecipe(db, "Pancakes",
                    new String[]{"Whisk the flour, sugar and a pinch of salt in a bowl.",
                            "Beat in the egg and milk to make a smooth batter.",
                            "Heat a little butter in a pan and pour in a ladle of batter.",
                            "Cook until bubbles appear, flip, and cook the other side."},
                    new Object[][]{i("Flour", 150, "g"), i("Milk", 250, "ml"), i("Egg", 1, "pcs"),
                            i("Sugar", 1, "tbsp"), i("Butter", 20, "g"), i("Salt", 0.25, "tsp")});

            DatabaseHelper.insertRecipe(db, "Garlic Butter Toast",
                    new String[]{"Mash the butter with the crushed garlic.",
                            "Spread over the bread slices.",
                            "Grill or toast until golden and crisp."},
                    new Object[][]{i("Bread", 4, "pcs"), i("Butter", 40, "g"), i("Garlic", 2, "pcs")});

            DatabaseHelper.insertRecipe(db, "Cheese Toastie",
                    new String[]{"Butter the outside of each bread slice.",
                            "Place the cheese between the slices.",
                            "Fry in a pan on medium heat for 3 minutes per side until golden."},
                    new Object[][]{i("Bread", 2, "pcs"), i("Cheese", 60, "g"), i("Butter", 10, "g")});

            DatabaseHelper.insertRecipe(db, "Mashed Potatoes",
                    new String[]{"Peel and chop the potatoes.",
                            "Boil in salted water for 15-20 minutes until soft.",
                            "Drain, then mash with the butter and milk until smooth."},
                    new Object[][]{i("Potato", 500, "g"), i("Butter", 30, "g"), i("Milk", 60, "ml"),
                            i("Salt", 0.5, "tsp")});

            DatabaseHelper.insertRecipe(db, "Vegetable Soup",
                    new String[]{"Chop the onion, carrots and potatoes.",
                            "Soften the onion in oil, then add the other vegetables.",
                            "Pour in the stock and simmer for 25 minutes.",
                            "Season with salt and serve hot."},
                    new Object[][]{i("Onion", 1, "pcs"), i("Carrot", 2, "pcs"), i("Potato", 2, "pcs"),
                            i("Vegetable stock", 750, "ml"), i("Vegetable oil", 1, "tbsp"),
                            i("Salt", 0.5, "tsp")});

            DatabaseHelper.insertRecipe(db, "Egg Fried Noodles",
                    new String[]{"Cook the noodles and drain.",
                            "Scramble the eggs in a hot wok and push to the side.",
                            "Add the noodles and soy sauce, toss together and serve."},
                    new Object[][]{i("Noodles", 200, "g"), i("Egg", 2, "pcs"), i("Soy sauce", 2, "tbsp"),
                            i("Vegetable oil", 1, "tbsp")});

            DatabaseHelper.insertRecipe(db, "Tuna Pasta",
                    new String[]{"Boil the pasta until al dente.",
                            "Warm the olive oil with the garlic in a pan.",
                            "Add the tuna and lemon juice, then toss through the pasta."},
                    new Object[][]{i("Pasta", 200, "g"), i("Tuna", 1, "pcs"), i("Garlic", 1, "pcs"),
                            i("Olive oil", 2, "tbsp"), i("Lemon", 1, "pcs")});

            DatabaseHelper.insertRecipe(db, "Porridge",
                    new String[]{"Combine the oats and milk in a pot.",
                            "Cook over medium heat, stirring, for 5 minutes.",
                            "Sweeten with honey and serve."},
                    new Object[][]{i("Oats", 50, "g"), i("Milk", 250, "ml"), i("Honey", 1, "tbsp")});

            DatabaseHelper.insertRecipe(db, "Banana Pancakes",
                    new String[]{"Mash the bananas in a bowl.",
                            "Mix in the eggs and flour until combined.",
                            "Fry spoonfuls in a lightly oiled pan until golden on both sides."},
                    new Object[][]{i("Banana", 2, "pcs"), i("Egg", 2, "pcs"), i("Flour", 60, "g"),
                            i("Vegetable oil", 1, "tbsp")});

            DatabaseHelper.insertRecipe(db, "Tomato Soup",
                    new String[]{"Soften the onion and garlic in butter.",
                            "Add the tomatoes and stock and simmer for 20 minutes.",
                            "Blend until smooth and season with salt."},
                    new Object[][]{i("Tomato", 6, "pcs"), i("Onion", 1, "pcs"), i("Garlic", 2, "pcs"),
                            i("Vegetable stock", 500, "ml"), i("Butter", 20, "g"), i("Salt", 0.5, "tsp")});

            DatabaseHelper.insertRecipe(db, "Chicken and Rice",
                    new String[]{"Season the chicken with salt and brown in oil.",
                            "Add the chopped onion and cook until soft.",
                            "Stir in the rice and stock, cover and simmer for 20 minutes."},
                    new Object[][]{i("Chicken breast", 300, "g"), i("Rice", 200, "g"), i("Onion", 1, "pcs"),
                            i("Chicken stock", 500, "ml"), i("Vegetable oil", 1, "tbsp"),
                            i("Salt", 0.5, "tsp")});

            DatabaseHelper.insertRecipe(db, "Cheesy Mashed Potato Bake",
                    new String[]{"Boil and mash the potatoes with the butter.",
                            "Spread in a baking dish and top with the cheese.",
                            "Bake at 200 C for 15 minutes until golden."},
                    new Object[][]{i("Potato", 600, "g"), i("Cheese", 100, "g"), i("Butter", 30, "g")});

            DatabaseHelper.insertRecipe(db, "French Toast",
                    new String[]{"Whisk the eggs, milk and sugar in a shallow dish.",
                            "Dip each slice of bread so it soaks up the mixture.",
                            "Fry in butter until golden on both sides."},
                    new Object[][]{i("Bread", 4, "pcs"), i("Egg", 2, "pcs"), i("Milk", 100, "ml"),
                            i("Sugar", 1, "tbsp"), i("Butter", 20, "g")});

            DatabaseHelper.insertRecipe(db, "Carrot and Potato Mash",
                    new String[]{"Peel and chop the carrots and potatoes.",
                            "Boil until tender, about 20 minutes.",
                            "Drain and mash with the butter."},
                    new Object[][]{i("Carrot", 3, "pcs"), i("Potato", 3, "pcs"), i("Butter", 25, "g")});

            DatabaseHelper.insertRecipe(db, "Peanut Butter Banana Toast",
                    new String[]{"Toast the bread.",
                            "Spread with peanut butter.",
                            "Top with sliced banana and a drizzle of honey."},
                    new Object[][]{i("Bread", 2, "pcs"), i("Peanut butter", 2, "tbsp"), i("Banana", 1, "pcs"),
                            i("Honey", 1, "tsp")});

            DatabaseHelper.insertRecipe(db, "Cheese and Tomato Pasta Bake",
                    new String[]{"Boil the pasta for 8 minutes and drain.",
                            "Mix with chopped tomatoes, garlic and olive oil in a baking dish.",
                            "Top with cheese and bake at 200 C for 15 minutes."},
                    new Object[][]{i("Pasta", 250, "g"), i("Tomato", 4, "pcs"), i("Cheese", 100, "g"),
                            i("Garlic", 2, "pcs"), i("Olive oil", 1, "tbsp")});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
