package com.example.myapplication.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.CustomerDao;
import com.example.myapplication.data.OrderDao;
import com.example.myapplication.model.Account;
import com.example.myapplication.model.Customer;
import com.example.myapplication.model.Order;
import com.example.myapplication.util.FormatUtils;
import com.example.myapplication.util.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * Admin: every order, filterable by status; tap an order to change its status.
 * Customer: their own order history.
 */
public class OrderListActivity extends AppCompatActivity {
    private static final String FILTER_ALL = "All";

    private OrderDao orderDao;
    private OrderAdapter adapter;
    private TextView tvSummary;
    private boolean isAdmin;
    private Customer customer;
    private String statusFilter; // null = all

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager session = new SessionManager(this);
        isAdmin = session.hasRole(Account.ROLE_ADMIN);
        if (!isAdmin) {
            customer = session.hasRole(Account.ROLE_CUSTOMER)
                    ? new CustomerDao(this).getByAccountId(session.getAccountId()) : null;
            if (customer == null) {
                LoginActivity.logout(this);
                return;
            }
        }

        setContentView(R.layout.activity_order_list);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(isAdmin ? "Order Management" : "My Orders");

        orderDao = new OrderDao(this);
        adapter = new OrderAdapter(this, isAdmin);
        tvSummary = findViewById(R.id.tvSummary);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setEmptyView(findViewById(R.id.tvEmpty));

        if (isAdmin) {
            setUpStatusFilter();
            listView.setOnItemClickListener((parent, view, position, id) ->
                    showStatusDialog(adapter.getItem(position)));
        }
        loadOrders();
    }

    private void setUpStatusFilter() {
        findViewById(R.id.filterRow).setVisibility(View.VISIBLE);
        String[] options = new String[Order.STATUSES.length + 1];
        options[0] = FILTER_ALL;
        System.arraycopy(Order.STATUSES, 0, options, 1, Order.STATUSES.length);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, options);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = findViewById(R.id.spStatus);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                statusFilter = position == 0 ? null : options[position];
                loadOrders();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadOrders() {
        List<Order> orders = isAdmin ? orderDao.getAll(statusFilter) : orderDao.getByCustomer(customer.getId());
        adapter.setOrders(orders);

        double total = 0;
        for (Order o : orders) {
            total += o.getTotalAmount();
        }
        tvSummary.setText(orders.size() + " order(s) · " + (isAdmin ? "Revenue " : "Total spent ")
                + FormatUtils.money(total)
                + (isAdmin ? "\nTap an order to update its status" : ""));
    }

    private void showStatusDialog(Order order) {
        int current = Arrays.asList(Order.STATUSES).indexOf(order.getStatus());
        new MaterialAlertDialogBuilder(this)
                .setTitle("Order #" + order.getId() + " · " + order.getCustomerName())
                .setSingleChoiceItems(Order.STATUSES, current, (dialog, which) -> {
                    String status = Order.STATUSES[which];
                    if (!status.equals(order.getStatus()) && orderDao.updateStatus(order.getId(), status)) {
                        Toast.makeText(this, "Order #" + order.getId() + " → " + status,
                                Toast.LENGTH_SHORT).show();
                        loadOrders();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
