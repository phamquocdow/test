package com.example.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myapplication.model.Account;

/** Stores the logged-in account in SharedPreferences. */
public class SessionManager {
    private static final String PREF_NAME = "fruit_shop_session";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_ACCOUNT_ID = "account_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_REMEMBER_ME = "remember_me";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveLogin(Account account, boolean rememberMe) {
        prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putLong(KEY_ACCOUNT_ID, account.getId())
                .putString(KEY_USERNAME, account.getUsername())
                .putString(KEY_ROLE, account.getRole())
                .putBoolean(KEY_REMEMBER_ME, rememberMe)
                .apply();
    }

    /** Ends the session; the username is kept so it can be pre-filled when "remember me" was on. */
    public void logout() {
        SharedPreferences.Editor editor = prefs.edit()
                .putBoolean(KEY_LOGGED_IN, false)
                .remove(KEY_ACCOUNT_ID)
                .remove(KEY_ROLE);
        if (!isRememberMe()) {
            editor.remove(KEY_USERNAME);
        }
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public boolean isRememberMe() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    public long getAccountId() {
        return prefs.getLong(KEY_ACCOUNT_ID, -1);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "");
    }

    public boolean hasRole(String role) {
        return isLoggedIn() && role.equals(getRole());
    }
}
