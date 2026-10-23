package com.example.myapplication.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myapplication.model.Account;
import com.example.myapplication.model.Order;
import com.example.myapplication.util.DateUtils;
import com.example.myapplication.util.FormatUtils;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "fruit_shop.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_ACCOUNT = "Account";
    public static final String TABLE_FRUIT = "Fruit";
    public static final String TABLE_CUSTOMER = "Customer";
    // ORDER is an SQL keyword, so the table name has to be quoted.
    public static final String TABLE_ORDER = "\"Order\"";

    private static DatabaseHelper instance;

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
        db.execSQL("CREATE TABLE " + TABLE_ACCOUNT + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL UNIQUE, "
                + "password TEXT NOT NULL, "
                + "role TEXT NOT NULL CHECK (role IN ('" + Account.ROLE_ADMIN + "', '" + Account.ROLE_CUSTOMER + "')))");

        db.execSQL("CREATE TABLE " + TABLE_FRUIT + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "category TEXT, "
                + "price REAL NOT NULL CHECK (price >= 0), "
                + "quantity INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0), "
                + "expiryDate TEXT NOT NULL, "
                + "description TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_CUSTOMER + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "fullname TEXT NOT NULL, "
                + "phone TEXT, "
                + "email TEXT, "
                + "accountId INTEGER NOT NULL UNIQUE REFERENCES " + TABLE_ACCOUNT + "(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE " + TABLE_ORDER + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "customerId INTEGER NOT NULL REFERENCES " + TABLE_CUSTOMER + "(id) ON DELETE CASCADE, "
                + "orderDate TEXT NOT NULL, "
                + "totalAmount REAL NOT NULL, "
                + "status TEXT NOT NULL DEFAULT '" + Order.STATUS_PENDING + "')");

        seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRUIT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT);
        onCreate(db);
    }

    /** Demo data: admin/admin123, customer/123456 and a few fruits (two of them expired). */
    private void seed(SQLiteDatabase db) {
        insertAccount(db, "admin", "admin123", Account.ROLE_ADMIN);
        long customerAccountId = insertAccount(db, "customer", "123456", Account.ROLE_CUSTOMER);

        ContentValues customer = new ContentValues();
        customer.put("fullname", "Nguyen Van A");
        customer.put("phone", "0901234567");
        customer.put("email", "customer@fruitshop.com");
        customer.put("accountId", customerAccountId);
        db.insert(TABLE_CUSTOMER, null, customer);

        insertFruit(db, "Apple", "Imported", 3.50, 100, 20, "Crisp red apples from New Zealand.");
        insertFruit(db, "Banana", "Tropical", 1.20, 150, 5, "Sweet Cavendish bananas.");
        insertFruit(db, "Mango", "Tropical", 2.80, 60, -2, "Cat Chu mangoes from Dong Thap.");
        insertFruit(db, "Orange", "Citrus", 2.00, 80, 15, "Juicy Cam Sanh oranges.");
        insertFruit(db, "Strawberry", "Berries", 6.50, 40, -1, "Fresh Da Lat strawberries.");
        insertFruit(db, "Watermelon", "Melon", 4.00, 30, 10, "Seedless watermelon.");
        insertFruit(db, "Grape", "Berries", 5.20, 50, 2, "Ninh Thuan green grapes.");
        insertFruit(db, "Dragon Fruit", "Tropical", 2.50, 70, 12, "Red-flesh dragon fruit.");
    }

    private long insertAccount(SQLiteDatabase db, String username, String password, String role) {
        ContentValues v = new ContentValues();
        v.put("username", username);
        v.put("password", FormatUtils.hashPassword(password));
        v.put("role", role);
        return db.insert(TABLE_ACCOUNT, null, v);
    }

    private void insertFruit(SQLiteDatabase db, String name, String category, double price,
                             int quantity, int expiresInDays, String description) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("category", category);
        v.put("price", price);
        v.put("quantity", quantity);
        v.put("expiryDate", DateUtils.daysFromToday(expiresInDays));
        v.put("description", description);
        db.insert(TABLE_FRUIT, null, v);
    }
}
