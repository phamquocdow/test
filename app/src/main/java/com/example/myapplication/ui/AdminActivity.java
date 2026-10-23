package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.FruitDao;
import com.example.myapplication.model.Account;
import com.example.myapplication.model.Fruit;
import com.example.myapplication.util.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Admin home: view, search, add, update and delete fruits, and spot expired ones. */
public class AdminActivity extends AppCompatActivity {
    private FruitDao fruitDao;
    private FruitAdapter adapter;
    private EditText etSearch;
    private CheckBox cbExpiredOnly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!new SessionManager(this).hasRole(Account.ROLE_ADMIN)) {
            LoginActivity.logout(this);
            return;
        }
        setContentView(R.layout.activity_fruit_list);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Admin · Fruits");

        fruitDao = new FruitDao(this);
        adapter = new FruitAdapter(this);
        etSearch = findViewById(R.id.etSearch);
        cbExpiredOnly = findViewById(R.id.cbExpiredOnly);
        cbExpiredOnly.setVisibility(View.VISIBLE);

        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setEmptyView(findViewById(R.id.tvEmpty));
        listView.setOnItemClickListener((parent, view, position, id) -> openForm(id));
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmDelete(adapter.getItem(position));
            return true;
        });

        View fab = findViewById(R.id.fabAdd);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(v -> openForm(-1));

        UiUtils.onTextChanged(etSearch, this::loadFruits);
        cbExpiredOnly.setOnCheckedChangeListener((b, checked) -> loadFruits());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            loadFruits();
        }
    }

    private void loadFruits() {
        adapter.setFruits(fruitDao.search(UiUtils.text(etSearch), cbExpiredOnly.isChecked(), false));
        int expired = fruitDao.countExpired();
        getSupportActionBar().setSubtitle(expired == 0 ? "No expired fruits"
                : "⚠ " + expired + " expired fruit(s)");
    }

    private void openForm(long fruitId) {
        Intent intent = new Intent(this, FruitFormActivity.class);
        intent.putExtra(FruitFormActivity.EXTRA_FRUIT_ID, fruitId);
        startActivity(intent);
    }

    private void confirmDelete(Fruit fruit) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete fruit")
                .setMessage("Delete \"" + fruit.getName() + "\"?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> {
                    if (fruitDao.delete(fruit.getId())) {
                        Toast.makeText(this, "Deleted " + fruit.getName(), Toast.LENGTH_SHORT).show();
                    }
                    loadFruits();
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_orders) {
            startActivity(new Intent(this, OrderListActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            LoginActivity.logout(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
