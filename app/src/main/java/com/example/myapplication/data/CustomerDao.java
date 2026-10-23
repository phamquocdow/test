package com.example.myapplication.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapplication.model.Customer;

public class CustomerDao {
    private final DatabaseHelper dbHelper;

    public CustomerDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public Customer getByAccountId(long accountId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DatabaseHelper.TABLE_CUSTOMER, null,
                "accountId = ?", new String[]{String.valueOf(accountId)},
                null, null, null)) {
            if (c.moveToFirst()) {
                return new Customer(
                        c.getLong(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("fullname")),
                        c.getString(c.getColumnIndexOrThrow("phone")),
                        c.getString(c.getColumnIndexOrThrow("email")),
                        c.getLong(c.getColumnIndexOrThrow("accountId")));
            }
            return null;
        }
    }
}
