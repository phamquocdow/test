package com.example.myapplication.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapplication.model.Fruit;
import com.example.myapplication.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class FruitDao {
    private final DatabaseHelper dbHelper;

    public FruitDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Searches fruits by name or category.
     *
     * @param keyword     text to match, empty for all fruits
     * @param expiredOnly only fruits whose expiry date is before today
     * @param inStockOnly only fruits with quantity > 0
     */
    public List<Fruit> search(String keyword, boolean expiredOnly, boolean inStockOnly) {
        StringBuilder where = new StringBuilder("1 = 1");
        List<String> args = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            where.append(" AND (name LIKE ? OR category LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }
        if (expiredOnly) {
            where.append(" AND expiryDate < ?");
            args.add(DateUtils.today());
        }
        if (inStockOnly) {
            where.append(" AND quantity > 0");
        }

        List<Fruit> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DatabaseHelper.TABLE_FRUIT, null, where.toString(),
                args.toArray(new String[0]), null, null, "name COLLATE NOCASE")) {
            while (c.moveToNext()) {
                result.add(read(c));
            }
        }
        return result;
    }

    public int countExpired() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_FRUIT
                + " WHERE expiryDate < ?", new String[]{DateUtils.today()})) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    public Fruit getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DatabaseHelper.TABLE_FRUIT, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? read(c) : null;
        }
    }

    public long insert(Fruit fruit) {
        return dbHelper.getWritableDatabase().insert(DatabaseHelper.TABLE_FRUIT, null, toValues(fruit));
    }

    public boolean update(Fruit fruit) {
        return dbHelper.getWritableDatabase().update(DatabaseHelper.TABLE_FRUIT, toValues(fruit),
                "id = ?", new String[]{String.valueOf(fruit.getId())}) > 0;
    }

    public boolean delete(long id) {
        return dbHelper.getWritableDatabase().delete(DatabaseHelper.TABLE_FRUIT,
                "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    static Fruit read(Cursor c) {
        return new Fruit(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getString(c.getColumnIndexOrThrow("category")),
                c.getDouble(c.getColumnIndexOrThrow("price")),
                c.getInt(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("expiryDate")),
                c.getString(c.getColumnIndexOrThrow("description")));
    }

    private static ContentValues toValues(Fruit fruit) {
        ContentValues v = new ContentValues();
        v.put("name", fruit.getName());
        v.put("category", fruit.getCategory());
        v.put("price", fruit.getPrice());
        v.put("quantity", fruit.getQuantity());
        v.put("expiryDate", fruit.getExpiryDate());
        v.put("description", fruit.getDescription());
        return v;
    }
}
