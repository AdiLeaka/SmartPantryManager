package com.example.smartpantry.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Local SQLite database (SQLiteOpenHelper). Holds the user's pantry and the recipe collection.
 *
 * Tables:
 *   pantry_items       (id, name, quantity, unit, expiry_date)           - user data, full CRUD
 *   recipes            (id, name, steps)                                 - seeded on first run
 *   recipe_ingredients (id, recipe_id -> recipes.id, name, quantity, unit)
 *
 * onCreate() runs only the first time the database file is created, which is
 * where the 20 recipes are seeded. The data lives in the app's private storage
 * so it survives closing and reopening the app.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    private static final String T_PANTRY = "pantry_items";
    private static final String T_RECIPES = "recipes";
    private static final String T_ING = "recipe_ingredients";

    private static DatabaseHelper instance;

    /** Single shared helper so all screens use one connection. */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_PANTRY + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "expiry_date TEXT)");
        db.execSQL("CREATE TABLE " + T_RECIPES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "steps TEXT NOT NULL)");
        db.execSQL("CREATE TABLE " + T_ING + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL REFERENCES " + T_RECIPES + "(id) ON DELETE CASCADE, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL)");
        RecipeSeeder.seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Recipes are re-seedable; the pantry is user data, so only the recipe tables are rebuilt.
        db.execSQL("DROP TABLE IF EXISTS " + T_ING);
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPES);
        db.execSQL("CREATE TABLE " + T_RECIPES + " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, steps TEXT NOT NULL)");
        db.execSQL("CREATE TABLE " + T_ING + " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL REFERENCES " + T_RECIPES + "(id) ON DELETE CASCADE, "
                + "name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL)");
        RecipeSeeder.seed(db);
    }

    // ---------------------------------------------------------------- Pantry CRUD

    /** CREATE: inserts a pantry item and returns its new id (or -1 on failure). */
    public long insertPantryItem(PantryItem item) {
        return getWritableDatabase().insert(T_PANTRY, null, toValues(item));
    }

    /** READ: all pantry items, alphabetical. */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        Cursor c = getReadableDatabase().query(T_PANTRY, null, null, null, null, null,
                "name COLLATE NOCASE ASC");
        try {
            while (c.moveToNext()) {
                items.add(fromCursor(c));
            }
        } finally {
            c.close();
        }
        return items;
    }

    /** READ: one pantry item, or null if it no longer exists. */
    public PantryItem getPantryItem(long id) {
        Cursor c = getReadableDatabase().query(T_PANTRY, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        try {
            return c.moveToFirst() ? fromCursor(c) : null;
        } finally {
            c.close();
        }
    }

    /** UPDATE: returns the number of rows changed (1 on success). */
    public int updatePantryItem(PantryItem item) {
        return getWritableDatabase().update(T_PANTRY, toValues(item), "id = ?",
                new String[]{String.valueOf(item.getId())});
    }

    /** DELETE: returns the number of rows removed (1 on success). */
    public int deletePantryItem(long id) {
        return getWritableDatabase().delete(T_PANTRY, "id = ?", new String[]{String.valueOf(id)});
    }

    /** Removes every pantry item (used by Settings). Recipes are untouched. */
    public void clearPantry() {
        getWritableDatabase().delete(T_PANTRY, null, null);
    }

    private ContentValues toValues(PantryItem item) {
        ContentValues v = new ContentValues();
        v.put("name", item.getName());
        v.put("quantity", item.getQuantity());
        v.put("unit", item.getUnit());
        if (item.getExpiryDate() == null) {
            v.putNull("expiry_date");
        } else {
            v.put("expiry_date", item.getExpiryDate());
        }
        return v;
    }

    private PantryItem fromCursor(Cursor c) {
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.isNull(c.getColumnIndexOrThrow("expiry_date"))
                        ? null : c.getString(c.getColumnIndexOrThrow("expiry_date")));
    }

    // -------------------------------------------------------------------- Recipes

    /** All recipes with their ingredient lists. */
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(T_RECIPES, null, null, null, null, null, "name COLLATE NOCASE ASC");
        try {
            while (c.moveToNext()) {
                Recipe r = new Recipe(c.getLong(0), c.getString(c.getColumnIndexOrThrow("name")),
                        c.getString(c.getColumnIndexOrThrow("steps")));
                loadIngredients(db, r);
                recipes.add(r);
            }
        } finally {
            c.close();
        }
        return recipes;
    }

    /** One recipe by id, or null. */
    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(T_RECIPES, null, "id = ?", new String[]{String.valueOf(id)},
                null, null, null);
        try {
            if (!c.moveToFirst()) {
                return null;
            }
            Recipe r = new Recipe(c.getLong(0), c.getString(c.getColumnIndexOrThrow("name")),
                    c.getString(c.getColumnIndexOrThrow("steps")));
            loadIngredients(db, r);
            return r;
        } finally {
            c.close();
        }
    }

    public int getRecipeCount() {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + T_RECIPES, null);
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    private void loadIngredients(SQLiteDatabase db, Recipe recipe) {
        Cursor c = db.query(T_ING, null, "recipe_id = ?",
                new String[]{String.valueOf(recipe.getId())}, null, null, "id ASC");
        try {
            while (c.moveToNext()) {
                recipe.addIngredient(new RecipeIngredient(
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getDouble(c.getColumnIndexOrThrow("quantity")),
                        c.getString(c.getColumnIndexOrThrow("unit"))));
            }
        } finally {
            c.close();
        }
    }

    /** Used by RecipeSeeder inside onCreate (same open database, so it must not call getWritableDatabase). */
    static void insertRecipe(SQLiteDatabase db, String name, String[] steps, Object[][] ingredients) {
        ContentValues rv = new ContentValues();
        rv.put("name", name);
        rv.put("steps", join(steps));
        long recipeId = db.insert(T_RECIPES, null, rv);
        for (Object[] ing : ingredients) {
            ContentValues iv = new ContentValues();
            iv.put("recipe_id", recipeId);
            iv.put("name", (String) ing[0]);
            iv.put("quantity", ((Number) ing[1]).doubleValue());
            iv.put("unit", (String) ing[2]);
            db.insert(T_ING, null, iv);
        }
    }

    private static String join(String[] steps) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < steps.length; i++) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append(steps[i]);
        }
        return sb.toString();
    }
}
