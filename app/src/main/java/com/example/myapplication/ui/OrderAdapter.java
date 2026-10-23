package com.example.myapplication.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.model.Order;
import com.example.myapplication.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private final boolean showCustomer;
    private final List<Order> orders = new ArrayList<>();

    public OrderAdapter(Context context, boolean showCustomer) {
        inflater = LayoutInflater.from(context);
        this.showCustomer = showCustomer;
    }

    public void setOrders(List<Order> newOrders) {
        orders.clear();
        orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return orders.size();
    }

    @Override
    public Order getItem(int position) {
        return orders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return orders.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_order, parent, false);
        }
        Order order = getItem(position);

        ((TextView) convertView.findViewById(R.id.tvOrderId)).setText("Order #" + order.getId());
        ((TextView) convertView.findViewById(R.id.tvDate)).setText(order.getOrderDate());
        ((TextView) convertView.findViewById(R.id.tvTotal)).setText(FormatUtils.money(order.getTotalAmount()));
        UiUtils.setOrderBadge(convertView.findViewById(R.id.tvStatus), order.getStatus());

        TextView customer = convertView.findViewById(R.id.tvCustomer);
        customer.setVisibility(showCustomer ? View.VISIBLE : View.GONE);
        customer.setText("Customer: " + order.getCustomerName());
        return convertView;
    }
}
