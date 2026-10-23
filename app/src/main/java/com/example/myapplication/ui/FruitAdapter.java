package com.example.myapplication.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.model.Fruit;
import com.example.myapplication.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class FruitAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private final List<Fruit> fruits = new ArrayList<>();

    public FruitAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void setFruits(List<Fruit> newFruits) {
        fruits.clear();
        fruits.addAll(newFruits);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return fruits.size();
    }

    @Override
    public Fruit getItem(int position) {
        return fruits.get(position);
    }

    @Override
    public long getItemId(int position) {
        return fruits.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder h;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_fruit, parent, false);
            h = new ViewHolder(convertView);
            convertView.setTag(h);
        } else {
            h = (ViewHolder) convertView.getTag();
        }

        Fruit fruit = getItem(position);
        h.name.setText(fruit.getName());
        h.price.setText(FormatUtils.money(fruit.getPrice()));
        h.category.setText(fruit.getCategory() == null || fruit.getCategory().isEmpty()
                ? "Uncategorized" : fruit.getCategory());
        h.stock.setText("Stock: " + fruit.getQuantity());
        h.expiry.setText("Exp: " + fruit.getExpiryDate());
        h.expiry.setTextColor(ContextCompat.getColor(convertView.getContext(),
                fruit.isExpired() ? R.color.status_expired : R.color.text_secondary));
        UiUtils.setFruitBadge(h.badge, fruit);
        return convertView;
    }

    private static class ViewHolder {
        final TextView name, price, category, stock, expiry, badge;

        ViewHolder(View v) {
            name = v.findViewById(R.id.tvName);
            price = v.findViewById(R.id.tvPrice);
            category = v.findViewById(R.id.tvCategory);
            stock = v.findViewById(R.id.tvStock);
            expiry = v.findViewById(R.id.tvExpiry);
            badge = v.findViewById(R.id.tvBadge);
        }
    }
}
