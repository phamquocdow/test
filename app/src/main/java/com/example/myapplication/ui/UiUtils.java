package com.example.myapplication.ui;

import android.content.res.ColorStateList;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.example.myapplication.R;
import com.example.myapplication.model.Fruit;
import com.example.myapplication.model.Order;
import com.example.myapplication.util.DateUtils;

final class UiUtils {
    private UiUtils() {
    }

    static String text(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    static void setBadge(TextView badge, String text, @ColorRes int color) {
        badge.setText(text);
        ViewCompat.setBackgroundTintList(badge,
                ColorStateList.valueOf(ContextCompat.getColor(badge.getContext(), color)));
    }

    /** Badge for a fruit: expired first, then expiring soon, out of stock, or fresh. */
    static void setFruitBadge(TextView badge, Fruit fruit) {
        long days = DateUtils.daysUntil(fruit.getExpiryDate());
        if (days < 0) {
            setBadge(badge, "EXPIRED", R.color.status_expired);
        } else if (days <= 3) {
            setBadge(badge, "EXPIRING SOON", R.color.status_expiring);
        } else if (!fruit.isInStock()) {
            setBadge(badge, "OUT OF STOCK", R.color.status_out_of_stock);
        } else {
            setBadge(badge, "FRESH", R.color.status_fresh);
        }
    }

    static void setOrderBadge(TextView badge, String status) {
        int color;
        if (Order.STATUS_COMPLETED.equals(status)) {
            color = R.color.order_completed;
        } else if (Order.STATUS_CONFIRMED.equals(status)) {
            color = R.color.order_confirmed;
        } else {
            color = R.color.order_pending;
        }
        setBadge(badge, status.toUpperCase(), color);
    }

    /** e.g. "Expired 2 days ago", "Expires today", "Expires in 5 days". */
    static String describeExpiry(String expiryDate) {
        long days = DateUtils.daysUntil(expiryDate);
        if (days < 0) {
            return "Expired " + (-days) + (days == -1 ? " day ago" : " days ago");
        } else if (days == 0) {
            return "Expires today";
        }
        return "Expires in " + days + (days == 1 ? " day" : " days");
    }

    /** TextWatcher that only cares about the text after it changed. */
    abstract static class AfterTextChanged implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    static void onTextChanged(EditText editText, Runnable action) {
        editText.addTextChangedListener(new AfterTextChanged() {
            @Override
            public void afterTextChanged(Editable s) {
                action.run();
            }
        });
    }
}
