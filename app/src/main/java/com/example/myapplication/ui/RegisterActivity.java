package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.AccountDao;
import com.google.android.material.appbar.MaterialToolbar;

public class RegisterActivity extends AppCompatActivity {
    static final String EXTRA_USERNAME = "username";

    private AccountDao accountDao;
    private EditText etUsername, etPassword, etConfirm, etFullname, etPhone, etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        accountDao = new AccountDao(this);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        etFullname = findViewById(R.id.etFullname);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        findViewById(R.id.btnRegister).setOnClickListener(v -> register());
    }

    private void register() {
        String username = UiUtils.text(etUsername);
        String password = etPassword.getText().toString();
        String confirm = etConfirm.getText().toString();
        String fullname = UiUtils.text(etFullname);
        String phone = UiUtils.text(etPhone);
        String email = UiUtils.text(etEmail);

        if (username.length() < 3) {
            etUsername.setError("Username must have at least 3 characters");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must have at least 6 characters");
            return;
        }
        if (!password.equals(confirm)) {
            etConfirm.setError("Passwords do not match");
            return;
        }
        if (fullname.isEmpty()) {
            etFullname.setError("Please enter your full name");
            return;
        }
        if (!phone.isEmpty() && !Patterns.PHONE.matcher(phone).matches()) {
            etPhone.setError("Invalid phone number");
            return;
        }
        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email");
            return;
        }
        if (accountDao.usernameExists(username)) {
            etUsername.setError("Username already exists");
            return;
        }

        if (!accountDao.registerCustomer(username, password, fullname, phone, email)) {
            Toast.makeText(this, "Registration failed, please try again", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Account created. Please log in.", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK, new Intent().putExtra(EXTRA_USERNAME, username));
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
