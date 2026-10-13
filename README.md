# Smart Pantry Manager

A Java Android app (Mobile App Development 700 practical assignment) that helps reduce food waste.
The user records the ingredients they already have at home, and the app suggests **only** recipes
they can cook right now - no shopping trip needed.

## Features
- Pantry management: add, edit and delete ingredients (name, quantity, unit, optional expiry date)
- Pantry list (RecyclerView + custom adapter) with expiring-soon highlighting
- 20 pre-loaded recipes (seeded into the database on first run)
- Suggested Recipes screen using the **strict-matching rule**: a recipe is listed only if *every*
  ingredient is in the pantry in at least the required quantity
- Optional separate "Almost there" tab for recipes missing exactly one ingredient
- Recipe detail screen (ingredients with have/missing marks, and method)
- Settings: expiring-soon alerts toggle, default unit, clear pantry
- Input validation, bottom navigation, empty-state messages

## Strict matching (core logic)
`logic/RecipeMatcher.java`. Matching tolerates real-world messiness:
- names are normalised (case, spaces, plurals): "Tomatoes" = "tomato"
- units are converted to a base unit: 1 kg satisfies 500 g; 2 tbsp = 30 ml
- several pantry rows of the same ingredient are added together
Unit tests: `app/src/test/.../RecipeMatcherTest.java`.

## Database choice: SQLite
Local on-device SQLite via `SQLiteOpenHelper` (`db/DatabaseHelper.java`). Chosen because the app is
single-user and offline, needs no account or server, data persists in the app's private storage, and
it matches the module's persistent-data chapter. Tables: `pantry_items`, `recipes`,
`recipe_ingredients` (foreign key to `recipes`, `ON DELETE CASCADE`).

## Project structure
```
app/src/main/java/com/example/smartpantry/
  db/       DatabaseHelper, RecipeSeeder
  model/    PantryItem, Recipe, RecipeIngredient
  logic/    RecipeMatcher, NameNormalizer, UnitConverter   (pure Java, unit tested)
  adapter/  PantryAdapter, RecipeAdapter
  ui/       PantryActivity, AddEditIngredientActivity, SuggestedRecipesActivity,
            RecipeDetailActivity, SettingsActivity
  util/     ExpiryUtils, Prefs, NavHelper
```

## Setup / run
1. Install Android Studio (Hedgehog or newer) with an Android SDK (API 34).
2. Clone this repository and choose **File > Open** on the project folder; let Gradle sync.
3. Create an emulator (API 24+) or connect a phone with USB debugging.
4. Press **Run**. Recipes are seeded automatically the first time the app starts.
5. Run unit tests with `./gradlew test` (or right-click `RecipeMatcherTest` > Run).

No Google Maps, mapping SDK or location permission is used.
