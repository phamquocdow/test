package com.example.myapplication.model;

public class Customer {
    private long id;
    private String fullname;
    private String phone;
    private String email;
    private long accountId;

    public Customer(long id, String fullname, String phone, String email, long accountId) {
        this.id = id;
        this.fullname = fullname;
        this.phone = phone;
        this.email = email;
        this.accountId = accountId;
    }

    public long getId() { return id; }
    public String getFullname() { return fullname; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public long getAccountId() { return accountId; }
}
