package com.example.myapplication.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.AccountDao;
import com.example.myapplication.model.Account;
import com.example.myapplication.util.SessionManager;

public class LoginActivity extends AppCompatActivity {
    private SessionManager session;
    private AccountDao accountDao;
    private EditText etUsername;
    private EditText etPassword;
    private CheckBox cbRemember;

    private final ActivityResultLauncher<Intent> registerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    etUsername.setText(result.getData().getStringExtra(RegisterActivity.EXTRA_USERNAME));
                    etPassword.setText("");
                    etPassword.requestFocus();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);

        // Auto login with the account saved in SharedPreferences.
        if (session.isLoggedIn() && session.isRememberMe()) {
            openHome(this, session.getRole());
            return;
        }

        setContentView(R.layout.activity_login);
        accountDao = new AccountDao(this);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        cbRemember = findViewById(R.id.cbRemember);

        if (session.isRememberMe()) {
            etUsername.setText(session.getUsername());
            cbRemember.setChecked(true);
        }

        findViewById(R.id.btnLogin).setOnClickListener(v -> login());
        findViewById(R.id.btnRegister).setOnClickListener(v ->
                registerLauncher.launch(new Intent(this, RegisterActivity.class)));
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login();
                return true;
            }
            return false;
        });
    }

    private void login() {
        String username = UiUtils.text(etUsername);
        String password = etPassword.getText().toString();
        if (username.isEmpty()) {
            etUsername.setError("Please enter username");
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Please enter password");
            return;
        }

        Account account = accountDao.login(username, password);
        if (account == null) {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            return;
        }
        session.saveLogin(account, cbRemember.isChecked());
        Toast.makeText(this, "Welcome, " + account.getUsername() + "!", Toast.LENGTH_SHORT).show();
        openHome(this, account.getRole());
    }

    /** Opens the home screen of the given role and clears the back stack. */
    static void openHome(Activity from, String role) {
        Class<?> target = Account.ROLE_ADMIN.equals(role) ? AdminActivity.class : CustomerActivity.class;
        Intent intent = new Intent(from, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        from.startActivity(intent);
        from.finish();
    }

    /** Clears the session and goes back to the login screen. */
    static void logout(Activity from) {
        new SessionManager(from).logout();
        Intent intent = new Intent(from, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        from.startActivity(intent);
        from.finish();
    }
}
