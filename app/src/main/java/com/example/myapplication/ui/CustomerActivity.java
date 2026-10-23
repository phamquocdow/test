package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.CustomerDao;
import com.example.myapplication.data.FruitDao;
import com.example.myapplication.data.OrderDao;
import com.example.myapplication.model.Account;
import com.example.myapplication.model.Customer;
import com.example.myapplication.model.Fruit;
import com.example.myapplication.util.FormatUtils;
import com.example.myapplication.util.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

/** Customer home: browse / search available fruits, check expiry dates and place orders. */
public class CustomerActivity extends AppCompatActivity {
    private FruitDao fruitDao;
    private OrderDao orderDao;
    private Customer customer;
    private FruitAdapter adapter;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager session = new SessionManager(this);
        customer = session.hasRole(Account.ROLE_CUSTOMER)
                ? new CustomerDao(this).getByAccountId(session.getAccountId()) : null;
        if (customer == null) {
            LoginActivity.logout(this);
            return;
        }
        setContentView(R.layout.activity_fruit_list);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Fruit Shop");
        getSupportActionBar().setSubtitle("Hi, " + customer.getFullname());

        fruitDao = new FruitDao(this);
        orderDao = new OrderDao(this);
        adapter = new FruitAdapter(this);
        etSearch = findViewById(R.id.etSearch);

        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setEmptyView(findViewById(R.id.tvEmpty));
        listView.setOnItemClickListener((parent, view, position, id) ->
                showOrderDialog(adapter.getItem(position)));

        UiUtils.onTextChanged(etSearch, this::loadFruits);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            loadFruits();
        }
    }

    /** Available fruits = fruits that are in stock (expired ones are shown, but flagged). */
    private void loadFruits() {
        adapter.setFruits(fruitDao.search(UiUtils.text(etSearch), false, true));
    }

    private void showOrderDialog(Fruit fruit) {
        View view = getLayoutInflater().inflate(R.layout.dialog_place_order, null);
        TextView tvDetails = view.findViewById(R.id.tvDetails);
        TextView tvWarning = view.findViewById(R.id.tvWarning);
        TextInputLayout tilQuantity = view.findViewById(R.id.tilQuantity);
        EditText etQuantity = view.findViewById(R.id.etQuantity);
        TextView tvTotal = view.findViewById(R.id.tvTotal);

        String description = fruit.getDescription() == null ? "" : fruit.getDescription();
        tvDetails.setText("Category: " + fruit.getCategory()
                + "\nPrice: " + FormatUtils.money(fruit.getPrice())
                + "\nIn stock: " + fruit.getQuantity()
                + "\nExpiry date: " + fruit.getExpiryDate() + " (" + UiUtils.describeExpiry(fruit.getExpiryDate()) + ")"
                + (description.isEmpty() ? "" : "\n\n" + description));

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(fruit.getName())
                .setView(view)
                .setNegativeButton("Close", null);

        if (fruit.isExpired()) {
            tvWarning.setVisibility(View.VISIBLE);
            tvWarning.setText("This fruit has expired and can't be ordered.");
            tilQuantity.setVisibility(View.GONE);
            tvTotal.setVisibility(View.GONE);
            builder.show();
            return;
        }

        Runnable updateTotal = () -> {
            int qty = parseQuantity(etQuantity);
            tvTotal.setText("Total: " + FormatUtils.money(fruit.getPrice() * Math.max(qty, 0)));
        };
        updateTotal.run();
        UiUtils.onTextChanged(etQuantity, () -> {
            tilQuantity.setError(null);
            updateTotal.run();
        });

        AlertDialog dialog = builder.setPositiveButton("Place order", null).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int qty = parseQuantity(etQuantity);
            if (qty <= 0) {
                tilQuantity.setError("Enter a quantity greater than 0");
                return;
            }
            try {
                long orderId = orderDao.placeOrder(customer.getId(), fruit.getId(), qty);
                Toast.makeText(this, "Order #" + orderId + " placed (Pending)", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadFruits();
            } catch (OrderDao.OrderException e) {
                tilQuantity.setError(e.getMessage());
            }
        }));
        dialog.show();
    }

    private static int parseQuantity(EditText et) {
        try {
            return Integer.parseInt(UiUtils.text(et));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_customer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_my_orders) {
            startActivity(new Intent(this, OrderListActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            LoginActivity.logout(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
