package com.example.myapplication.model;

public class Order {
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_CONFIRMED = "Confirmed";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String[] STATUSES = {STATUS_PENDING, STATUS_CONFIRMED, STATUS_COMPLETED};

    private long id;
    private long customerId;
    private String orderDate; // yyyy-MM-dd HH:mm
    private double totalAmount;
    private String status;

    // Not a column of the Order table: joined from Customer for display.
    private String customerName;

    public Order(long id, long customerId, String orderDate, double totalAmount, String status) {
        this.id = id;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public long getId() { return id; }
    public long getCustomerId() { return customerId; }
    public String getOrderDate() { return orderDate; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}
