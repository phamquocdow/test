package com.example.myapplication.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.FruitDao;
import com.example.myapplication.model.Account;
import com.example.myapplication.model.Fruit;
import com.example.myapplication.util.DateUtils;
import com.example.myapplication.util.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

/** Admin screen to add a new fruit or update / delete an existing one. */
public class FruitFormActivity extends AppCompatActivity {
    static final String EXTRA_FRUIT_ID = "fruit_id";

    private FruitDao fruitDao;
    private Fruit fruit; // null when adding
    private EditText etName, etCategory, etPrice, etQuantity, etExpiry, etDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!new SessionManager(this).hasRole(Account.ROLE_ADMIN)) {
            LoginActivity.logout(this);
            return;
        }
        setContentView(R.layout.activity_fruit_form);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fruitDao = new FruitDao(this);
        etName = findViewById(R.id.etName);
        etCategory = findViewById(R.id.etCategory);
        etPrice = findViewById(R.id.etPrice);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiry = findViewById(R.id.etExpiry);
        etDescription = findViewById(R.id.etDescription);
        etExpiry.setOnClickListener(v -> pickExpiryDate());

        long fruitId = getIntent().getLongExtra(EXTRA_FRUIT_ID, -1);
        if (fruitId != -1) {
            fruit = fruitDao.getById(fruitId);
        }

        if (fruit == null) {
            getSupportActionBar().setTitle("Add fruit");
        } else {
            getSupportActionBar().setTitle("Edit fruit");
            fillForm(fruit);
            View btnDelete = findViewById(R.id.btnDelete);
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> confirmDelete());
        }
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void fillForm(Fruit f) {
        etName.setText(f.getName());
        etCategory.setText(f.getCategory());
        etPrice.setText(String.format(Locale.US, "%.2f", f.getPrice()));
        etQuantity.setText(String.valueOf(f.getQuantity()));
        etExpiry.setText(f.getExpiryDate());
        etDescription.setText(f.getDescription());
        showExpiryHint();
    }

    private void pickExpiryDate() {
        int[] ymd = DateUtils.toYmd(UiUtils.text(etExpiry));
        new DatePickerDialog(this, (view, year, month, day) -> {
            etExpiry.setText(DateUtils.format(year, month, day));
            etExpiry.setError(null);
            showExpiryHint();
        }, ymd[0], ymd[1], ymd[2]).show();
    }

    /** Warns the admin right in the form when the chosen date is already past. */
    private void showExpiryHint() {
        String date = UiUtils.text(etExpiry);
        if (DateUtils.isExpired(date)) {
            Toast.makeText(this, "Note: this fruit is expired (" + UiUtils.describeExpiry(date) + ")",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void save() {
        String name = UiUtils.text(etName);
        String priceText = UiUtils.text(etPrice);
        String quantityText = UiUtils.text(etQuantity);
        String expiry = UiUtils.text(etExpiry);

        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }
        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid price");
            return;
        }
        if (price < 0) {
            etPrice.setError("Price can't be negative");
            return;
        }
        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            etQuantity.setError("Invalid quantity");
            return;
        }
        if (quantity < 0) {
            etQuantity.setError("Quantity can't be negative");
            return;
        }
        if (!DateUtils.isValidDate(expiry)) {
            etExpiry.setError("Please pick an expiry date");
            return;
        }

        Fruit target = fruit != null ? fruit : new Fruit();
        target.setName(name);
        target.setCategory(UiUtils.text(etCategory));
        target.setPrice(price);
        target.setQuantity(quantity);
        target.setExpiryDate(expiry);
        target.setDescription(UiUtils.text(etDescription));

        boolean ok = fruit == null ? fruitDao.insert(target) != -1 : fruitDao.update(target);
        if (!ok) {
            Toast.makeText(this, "Could not save fruit", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, fruit == null ? "Fruit added" : "Fruit updated", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete fruit")
                .setMessage("Delete \"" + fruit.getName() + "\"?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> {
                    fruitDao.delete(fruit.getId());
                    Toast.makeText(this, "Fruit deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
