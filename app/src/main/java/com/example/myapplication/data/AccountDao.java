package com.example.myapplication.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapplication.model.Account;
import com.example.myapplication.util.FormatUtils;

public class AccountDao {
    private final DatabaseHelper dbHelper;

    public AccountDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /** Returns the account if the credentials match, otherwise null. */
    public Account login(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DatabaseHelper.TABLE_ACCOUNT, null,
                "username = ? AND password = ?",
                new String[]{username, FormatUtils.hashPassword(password)},
                null, null, null)) {
            if (c.moveToFirst()) {
                return new Account(
                        c.getLong(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("username")),
                        c.getString(c.getColumnIndexOrThrow("password")),
                        c.getString(c.getColumnIndexOrThrow("role")));
            }
            return null;
        }
    }

    public boolean usernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DatabaseHelper.TABLE_ACCOUNT, new String[]{"id"},
                "username = ?", new String[]{username}, null, null, null)) {
            return c.moveToFirst();
        }
    }

    /** Creates a CUSTOMER account and its Customer profile in one transaction. */
    public boolean registerCustomer(String username, String password, String fullname,
                                    String phone, String email) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues account = new ContentValues();
            account.put("username", username);
            account.put("password", FormatUtils.hashPassword(password));
            account.put("role", Account.ROLE_CUSTOMER);
            long accountId = db.insert(DatabaseHelper.TABLE_ACCOUNT, null, account);
            if (accountId == -1) {
                return false;
            }

            ContentValues customer = new ContentValues();
            customer.put("fullname", fullname);
            customer.put("phone", phone);
            customer.put("email", email);
            customer.put("accountId", accountId);
            if (db.insert(DatabaseHelper.TABLE_CUSTOMER, null, customer) == -1) {
                return false;
            }

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }
}
