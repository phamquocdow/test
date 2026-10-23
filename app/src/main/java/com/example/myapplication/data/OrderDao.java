package com.example.myapplication.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapplication.model.Fruit;
import com.example.myapplication.model.Order;
import com.example.myapplication.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class OrderDao {
    private final DatabaseHelper dbHelper;

    public OrderDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /** Thrown when an order can't be placed (expired fruit, not enough stock...). */
    public static class OrderException extends Exception {
        public OrderException(String message) {
            super(message);
        }
    }

    /**
     * Places a Pending order for {@code quantity} units of a fruit and reduces its stock.
     * Expired fruits are rejected. Returns the new order id.
     */
    public long placeOrder(long customerId, long fruitId, int quantity) throws OrderException {
        if (quantity <= 0) {
            throw new OrderException("Quantity must be greater than 0.");
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            Fruit fruit;
            try (Cursor c = db.query(DatabaseHelper.TABLE_FRUIT, null, "id = ?",
                    new String[]{String.valueOf(fruitId)}, null, null, null)) {
                if (!c.moveToFirst()) {
                    throw new OrderException("This fruit no longer exists.");
                }
                fruit = FruitDao.read(c);
            }
            if (fruit.isExpired()) {
                throw new OrderException(fruit.getName() + " expired on " + fruit.getExpiryDate()
                        + " and can't be ordered.");
            }
            if (fruit.getQuantity() < quantity) {
                throw new OrderException("Only " + fruit.getQuantity() + " " + fruit.getName()
                        + " left in stock.");
            }

            ContentValues stock = new ContentValues();
            stock.put("quantity", fruit.getQuantity() - quantity);
            db.update(DatabaseHelper.TABLE_FRUIT, stock, "id = ?", new String[]{String.valueOf(fruitId)});

            ContentValues order = new ContentValues();
            order.put("customerId", customerId);
            order.put("orderDate", DateUtils.now());
            order.put("totalAmount", fruit.getPrice() * quantity);
            order.put("status", Order.STATUS_PENDING);
            long orderId = db.insert(DatabaseHelper.TABLE_ORDER, null, order);
            if (orderId == -1) {
                throw new OrderException("Could not save the order. Please try again.");
            }

            db.setTransactionSuccessful();
            return orderId;
        } finally {
            db.endTransaction();
        }
    }

    public List<Order> getByCustomer(long customerId) {
        return query("o.customerId = ?", new String[]{String.valueOf(customerId)});
    }

    /** All orders, or only those with the given status when it is not null. */
    public List<Order> getAll(String status) {
        if (status == null) {
            return query(null, null);
        }
        return query("o.status = ?", new String[]{status});
    }

    public boolean updateStatus(long orderId, String status) {
        ContentValues v = new ContentValues();
        v.put("status", status);
        return dbHelper.getWritableDatabase().update(DatabaseHelper.TABLE_ORDER, v,
                "id = ?", new String[]{String.valueOf(orderId)}) > 0;
    }

    private List<Order> query(String where, String[] args) {
        String sql = "SELECT o.id, o.customerId, o.orderDate, o.totalAmount, o.status, c.fullname"
                + " FROM " + DatabaseHelper.TABLE_ORDER + " o"
                + " JOIN " + DatabaseHelper.TABLE_CUSTOMER + " c ON c.id = o.customerId"
                + (where == null ? "" : " WHERE " + where)
                + " ORDER BY o.id DESC";
        List<Order> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(sql, args)) {
            while (c.moveToNext()) {
                Order order = new Order(c.getLong(0), c.getLong(1), c.getString(2),
                        c.getDouble(3), c.getString(4));
                order.setCustomerName(c.getString(5));
                result.add(order);
            }
        }
        return result;
    }
}
